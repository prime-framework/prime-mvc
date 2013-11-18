/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.parameter.RequestBodyWorkflow.JSONHandler.Names.Name;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.util.IteratorEnumeration;
import org.primeframework.mvc.util.RequestKeys;
import org.primeframework.mvc.workflow.Workflow;
import org.primeframework.mvc.workflow.WorkflowChain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

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
      } else if (contentType.startsWith("application/json")) {
        parsedParameters = parseJSON(request.getInputStream(), request.getContentLength(), request.getCharacterEncoding());
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
          File file = File.createTempFile("prime", "fileupload");
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

  private Map<String, List<String>> parseJSON(InputStream inputStream, int contentLength, String characterEncoding)
  throws IOException {
    if (contentLength == 0) {
      return null;
    }

    if (characterEncoding == null) {
      characterEncoding = "UTF-8";
    }

    JSONHandler handler = new JSONHandler();
    JSONParser parser = new JSONParser();
    try {
      parser.parse(new InputStreamReader(inputStream, characterEncoding), handler);
    } catch (ParseException e) {
      throw new IOException(e);
    }

    return handler.parameters;
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

  public class JSONHandler implements ContentHandler {
    public final Names names = new Names();

    public final Map<String, List<String>> parameters = new HashMap<String, List<String>>();

    public List<String> simpleArray;

    public String currentKey;

    @Override
    public boolean endArray() throws ParseException, IOException {
      if (names.current().isSimpleArray()) {
        parameters.put(currentKey, simpleArray);
        simpleArray = null;
      }

      return true;
    }

    @Override
    public void endJSON() throws ParseException, IOException {
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
      Name current = names.current();
      if (current != null) {
        current.incrementIndexIfApplicable();
      }

      return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
      names.pop();
      currentKey = names.toString();
      return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
      if (names.current().isStartOfArray()) {
        if (simpleArray == null) {
          names.current().markAsSimpleArray();
          simpleArray = new ArrayList<String>();
        }

        simpleArray.add(value.toString());
      } else {
        String key = names.toString();
        parameters.put(key, asList(value.toString()));
        names.current().incrementIndexIfApplicable();
      }

      return true;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
      if (names.current().isSimpleArray() || names.current().isArray()) {
        throw new IOException("Embedded JSON arrays inside a primitive array are not allowed (i.e. [\"foo\", [1, 2], \"bar\"] or [[1, 2], [3, 4]])");
      }

      names.current().startArray();
      return true;
    }

    @Override
    public void startJSON() throws ParseException, IOException {
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
      return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
      if (names.current() != null && names.current().isSimpleArray()) {
        throw new IOException("Embedded JSON objects inside a primitive array is not allowed (i.e. [\"foo\", {\"age\": 1}, \"bar\"])");
      }

      names.push(key);
      currentKey = names.toString();
      return true;
    }

    public class Names {
      public final Deque<Name> names = new LinkedList<Name>();

      public Name current() {
        return names.peekLast();
      }

      public Name pop() {
        return names.removeLast();
      }

      public void push(String name) {
        names.addLast(new Name(name));
      }

      public String toString() {
        return StringUtils.join(names, '.');
      }

      public class Name {
        public final String name;

        public Index index;

        public boolean simpleArray;

        public Name(String name) {
          this.name = name;
        }

        public void incrementIndexIfApplicable() {
          if (index != null) {
            index.value++;
          }
        }

        public boolean isArray() {
          return index != null;
        }

        public boolean isSimpleArray() {
          return simpleArray;
        }

        public boolean isStartOfArray() {
          return index != null && index.value == 0;
        }

        public void markAsSimpleArray() {
          simpleArray = true;
        }

        public void startArray() {
          index = new Index();
        }

        public String toString() {
          if (index == null) {
            return name;
          }

          return name + "[" + index.value + "]";
        }

        public class Index {
          public int value;
        }
      }
    }
  }
}
