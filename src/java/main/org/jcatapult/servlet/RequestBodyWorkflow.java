/*
 * Copyright (c) 2001-2010, JCatapult.org, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jcatapult.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import net.java.util.IteratorEnumeration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jcatapult.servlet.multipart.FileInfo;

/**
 * <p>
 * This workflow handles providing access to parameters inside the request body when the container
 * doesn't parse them. Some containers don't parse request bodies for parameters when the method is
 * not POST. They do this because that is the defined behavior in the Servlet specification under
 * section SRV 3.1.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class RequestBodyWorkflow implements Workflow {
    private final HttpServletRequest request;

    @Inject
    public RequestBodyWorkflow(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
        // Let the container parse out the GET and POST parameters by calling the getParameterMap method
        // After this call, if the method is GET with any content-type or POST with the content-type as
        // x-www-form-urlencoded the InputStream will be empty
        Map<String, String[]> parameters = request.getParameterMap();

        String contentType = request.getContentType();
        Map<String, List<String>> parsedParameters = null;
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            FilesAndParameters filesAndParameters = handleFiles();
            request.setAttribute(RequestKeys.FILE_ATTRIBUTE, filesAndParameters.files);
            parsedParameters = filesAndParameters.parameters;
        } else if (contentType != null && contentType.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            parsedParameters = parse(request.getInputStream(), request.getCharacterEncoding());
        }

        if (parsedParameters != null && parsedParameters.size() > 0) {
            HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) request;
            HttpServletRequest previous = (HttpServletRequest) wrapper.getRequest();
            HttpServletRequest newRequest = new ParameterHttpServletRequestWrapper(previous, combine(parameters, parsedParameters));
            wrapper.setRequest(newRequest);
        }

        workflowChain.continueWorkflow();
    }

    /**
     * Handles parsing the multi-part body to pull out the files and the parameters.
     *
     * @return   The files and the parameters.
     */
    private FilesAndParameters handleFiles() {
        FilesAndParameters filesAndParameters = new FilesAndParameters();
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                String name = item.getFieldName();
                if (item.isFormField()) {
                    String value = item.getString();
                    List<String> list = filesAndParameters.parameters.get(name);
                    if (list == null) {
                        list = new ArrayList<String>();
                        filesAndParameters.parameters.put(name, list);
                    }

                    list.add(value);
                } else {
                    String fileName = item.getName();

                    // Handle lame ass IE issues with file names
                    if (fileName.contains(":\\")) {
                        int index = fileName.lastIndexOf("\\");
                        fileName = fileName.substring(index + 1);
                    }

                    String contentType = item.getContentType();
                    File file = File.createTempFile("jcatapult", "fileupload");
                    item.write(file);

                    // Handle when the user doesn't provide a file at all
                    if (file.length() == 0 || fileName == null || contentType == null) {
                        continue;
                    }

                    List<FileInfo> list = filesAndParameters.files.get(name);
                    if (list == null) {
                        list = new ArrayList<FileInfo>();
                        filesAndParameters.files.put(name, list);
                    }

                    list.add(new FileInfo(file, fileName, contentType));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to handle file uploads", e);
        }

        return filesAndParameters;
    }

    /**
     * Parses the HTTP request body for URL encoded parameters.
     *
     * @param   inputStream The input stream to read from.
     * @param   encoding The encoding header.
     * @return  The parameter map.
     * @throws  IOException If the read failed.
     */
    private Map<String, List<String>> parse(InputStream inputStream, String encoding) throws IOException {
        if (encoding == null) {
            encoding = "UTF-8";
        }

        Map<String, List<String>> parsedParameters = new HashMap<String, List<String>>();
        byte[] str = new byte[1024];
        int length = 0;
        int c;
        String key = null;
        while ((c = inputStream.read()) != -1) {
            if (c == '=') {
                if (length == 0 || key != null) {
                    throw new IOException("Invalid HTTP URLEncoded request body");
                }

                key = URLDecoder.decode(new String(str, 0, length, encoding), "UTF-8");
                length = 0;
            } else if (c == '&') {
                addParam(parsedParameters, key, str, length, encoding);
                key = null;
                length = 0;
            } else {
                str[length++] = (byte) c;
                if (length == str.length) {
                    byte[] newStr = new byte[str.length * 2];
                    System.arraycopy(str, 0, newStr, 0, str.length);
                    str = newStr;
                }
            }
        }

        // If the key is null, the input stream was empty
        if (key != null) {
            addParam(parsedParameters, key, str, length, encoding);
        }

        return parsedParameters;
    }

    private void addParam(Map<String, List<String>> parsedParameters, String key, byte[] str, int length, String encoding)
    throws IOException {
        if (key == null) {
            throw new IOException("Invalid HTTP URLEncoded request body");
        }

        String value;
        if (length == 0) {
            value = "";
        } else {
            value = URLDecoder.decode(new String(str, 0, length, encoding), "UTF-8");
        }

        List<String> list = parsedParameters.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            parsedParameters.put(key, list);
        }

        list.add(value);
    }

    private Map<String, String[]> combine(Map<String, String[]> original, Map<String, List<String>> parsed) {
        Map<String, String[]> map = new HashMap<String, String[]>();
        for (String key : original.keySet()) {
            String[] originalValues = original.get(key);
            List<String> parsedValues = parsed.remove(key);

            String[] newValues = new String[originalValues.length + (parsedValues == null ? 0 : parsedValues.size())];
            System.arraycopy(originalValues, 0, newValues, 0, originalValues.length);

            if (parsedValues != null && parsedValues.size() > 0) {
                int index = originalValues.length;
                for (String parsedValue : parsedValues) {
                    newValues[index++] = parsedValue;
                }
            }

            map.put(key, newValues);
        }

        for (String key : parsed.keySet()) {
            List<String> parsedValues = parsed.get(key);
            map.put(key, parsedValues.toArray(new String[parsedValues.size()]));
        }

        return map;
    }

    private static class FilesAndParameters {
        public final Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
        public final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    }

    private static class ParameterHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> parameters;

        public ParameterHttpServletRequestWrapper(HttpServletRequest previous, Map<String, String[]> parameters) {
            super(previous);
            this.parameters = Collections.unmodifiableMap(parameters);
        }

        @Override
        public String getParameter(String s) {
            return (parameters.get(s) != null) ? parameters.get(s)[0] : null;
        }

        @Override
        public Map getParameterMap() {
            return parameters;
        }

        @Override
        public Enumeration getParameterNames() {
            return new IteratorEnumeration(parameters.keySet().iterator());
        }

        @Override
        public String[] getParameterValues(String s) {
            return parameters.get(s);
        }
    }
}
