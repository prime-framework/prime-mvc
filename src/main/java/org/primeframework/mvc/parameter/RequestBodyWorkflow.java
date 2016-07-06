/*
 * Copyright (c) 2012-2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.util.IteratorEnumeration;
import org.primeframework.mvc.util.RequestKeys;
import org.primeframework.mvc.workflow.Workflow;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * This workflow handles providing access to parameters inside the request body when the container doesn't parse them.
 * Some containers don't parse request bodies for parameters when the method is not POST. They do this because that is
 * the defined behavior in the Servlet specification under section SRV 3.1.
 *
 * @author Brian Pontarelli
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

    Map<String, List<String>> parsedParameters = null;
    String contentType = request.getContentType();
    if (contentType != null) {
      contentType = contentType.toLowerCase();
      if (contentType.startsWith("multipart/")) {
        FilesAndParameters filesAndParameters = handleFiles();
        request.setAttribute(RequestKeys.FILE_ATTRIBUTE, filesAndParameters.files);
        parsedParameters = filesAndParameters.parameters;
      } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
        parsedParameters = parse(request.getInputStream(), request.getContentLength(), request.getCharacterEncoding());
      }
    }

    if (parsedParameters != null && parsedParameters.size() > 0) {
      HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) request;
      HttpServletRequest previous = (HttpServletRequest) wrapper.getRequest();
      HttpServletRequest newRequest = new ParameterHttpServletRequestWrapper(previous, combine(parameters, parsedParameters));
      wrapper.setRequest(newRequest);
    }

    workflowChain.continueWorkflow();
  }

  private void addParam(Map<String, List<String>> parsedParameters, String key, byte[] str, int start, int length,
                        String encoding, boolean decode)
      throws IOException {
    if (key == null) {
      throw new IOException("Invalid HTTP URLEncoded request body");
    }

    String value = toParameterString(encoding, str, start, length, decode);
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

  /**
   * Handles parsing the multi-part body to pull out the files and the parameters.
   *
   * @return The files and the parameters.
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
            list = new ArrayList<>();
            filesAndParameters.parameters.put(name, list);
          }

          list.add(value);
        } else {
          String fileName = item.getName();
          if (fileName == null) {
            continue;
          }

          // Handle lame ass IE issues with file names
          if (fileName.contains(":\\")) {
            int index = fileName.lastIndexOf("\\");
            fileName = fileName.substring(index + 1);
          }

          String contentType = item.getContentType();
          File file = File.createTempFile("prime", "fileupload");
          item.write(file);

          // Handle when the user doesn't provide a file at all
          if (file.length() == 0 || contentType == null) {
            continue;
          }

          List<FileInfo> list = filesAndParameters.files.get(name);
          if (list == null) {
            list = new ArrayList<>();
            filesAndParameters.files.put(name, list);
          }

          list.add(new FileInfo(file, fileName, contentType));
        }
      }
    } catch (Exception e) {
      throw new PrimeException("Unable to handle file uploads", e);
    }

    return filesAndParameters;
  }

  /**
   * Parses the HTTP request body for URL encoded parameters.
   *
   * @param inputStream   The input stream to read from.
   * @param contentLength The estimated length of the content.
   * @param encoding      The encoding header.
   * @return The parameter map.
   * @throws IOException If the read failed.
   */
  private Map<String, List<String>> parse(InputStream inputStream, int contentLength, String encoding)
      throws IOException {
    if (contentLength == 0) {
      return null;
    }

    if (encoding == null) {
      encoding = "UTF-8";
    }

    if (contentLength < 0) {
      contentLength = 1024;
    }

    byte[] readBuffer = new byte[contentLength];

    int read;
    int length = 0;
    while ((read = inputStream.read(readBuffer, length, readBuffer.length - length)) >= 0) {
      length += read;
      if (length == readBuffer.length) {
        byte[] expandedBuffer = new byte[readBuffer.length + 1024];
        System.arraycopy(readBuffer, 0, expandedBuffer, 0, length);
        readBuffer = expandedBuffer;
      }
    }

    if (length == 0) {
      return null;
    }

    Map<String, List<String>> parsedParameters = new HashMap<String, List<String>>();
    int start = 0;
    int index = 0;
    String key = null;
    boolean decode = false;
    for (; index < length; index++) {
      byte c = readBuffer[index];
      switch (c) {
        case '=':
          key = toParameterString(encoding, readBuffer, start, index - start, decode);
          start = index + 1;
          decode = false;
          break;
        case '&':
          addParam(parsedParameters, key, readBuffer, start, index - start, encoding, decode);
          key = null;
          start = index + 1;
          decode = false;
          break;
        case '+':
        case '%':
          decode = true;
      }
    }

    addParam(parsedParameters, key, readBuffer, start, index - start, encoding, decode);
    return parsedParameters;
  }

  private String toParameterString(String encoding, byte[] readBuffer, int start, int length, boolean decode)
      throws UnsupportedEncodingException {
    if (length == 0) {
      return "";
    }

    String key = new String(readBuffer, start, length, encoding);
    if (decode) {
      key = URLDecoder.decode(key, encoding);
    }
    return key;
  }

  private static class FilesAndParameters {
    public final Map<String, List<FileInfo>> files = new HashMap<>();

    public final Map<String, List<String>> parameters = new HashMap<>();
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
