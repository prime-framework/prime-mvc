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

import org.apache.commons.io.FileUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.primeframework.mock.servlet.MockServletInputStream;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.servlet.MockWorkflowChain;
import org.primeframework.mvc.util.MapBuilder;
import org.primeframework.mvc.util.RequestKeys;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the request body workflow.
 *
 * @author Brian Pontarelli
 */
public class RequestBodyWorkflowTest {
  @Test
  public void containerDrainedBody() throws IOException, ServletException {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    expect(request.getInputStream()).andReturn(new MockServletInputStream(new byte[0]));
    expect(request.getContentLength()).andReturn(0);
    expect(request.getCharacterEncoding()).andReturn("UTF-8");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    // This is the assert since the request would be unwrapped if the body contained parameters
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(request);
    workflow.perform(chain);

    verify(request, chain);
  }

  @Test
  public void multipleFiles() throws IOException, ServletException {
    String body = FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/servlet/http-test-body-multiple-files.txt"));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap<>());
    EasyMock.expect(request.getContentType()).andReturn("multipart/form-data, boundary=AaB03x").times(2);
    EasyMock.expect(request.getInputStream()).andReturn(new MockServletInputStream(body.getBytes()));
    EasyMock.expect(request.getCharacterEncoding()).andReturn("UTF-8");
    EasyMock.expect(request.getContentLength()).andReturn(body.length());
    final Capture<Map<String, List<FileInfo>>> capture = new Capture<>();
    request.setAttribute(eq(RequestKeys.FILE_ATTRIBUTE), capture(capture));
    EasyMock.replay(request);

    final AtomicBoolean run = new AtomicBoolean(false);
    MockWorkflowChain chain = new MockWorkflowChain(() -> {
      Map<String, List<FileInfo>> files = capture.getValue();
      assertEquals(files.size(), 1);
      try {
        assertEquals(FileUtils.readFileToString(files.get("userfiles").get(0).file), "test");
        assertEquals(FileUtils.readFileToString(files.get("userfiles").get(1).file), "test2");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      run.set(true);
    });

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);
    assertTrue(run.get());

    assertEquals(wrapper.getParameter("field1"), "value1");
    assertEquals(wrapper.getParameter("field2"), "value2");

    EasyMock.verify(request);
  }

  @Test
  public void noContentType() throws IOException, ServletException {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(null);
    expect(request.getContentType()).andReturn(null);
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    RequestBodyWorkflow workflow = new RequestBodyWorkflow(request);
    workflow.perform(chain);

    verify(request, chain);
  }

  @Test
  public void parse() throws IOException, ServletException {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    String body = "param1=value1&param2=value2&param+space+key=param+space+value&param%2Bencoded%2Bkey=param%2Bencoded%2Bvalue";
    expect(request.getInputStream()).andReturn(new MockServletInputStream(body.getBytes()));
    expect(request.getContentLength()).andReturn(body.getBytes().length);
    expect(request.getCharacterEncoding()).andReturn("UTF-8");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);

    @SuppressWarnings("unchecked")
    Map<String, String[]> actual = wrapper.getParameterMap();
    Map<String, String[]> expected =
        MapBuilder.<String, String[]>map()
                  .put("param1", new String[]{"value1"})
                  .put("param2", new String[]{"value2"})
                  .put("param space key", new String[]{"param space value"})
                  .put("param+encoded+key", new String[]{"param+encoded+value"})
                  .done();
    assertParameterMapsEquals(actual, expected);

    verify(request, chain);
  }

  @Test
  public void parseCombine() throws IOException, ServletException {
    Map<String, String[]> oldParams = new HashMap<>();
    oldParams.put("param1", new String[]{"oldvalue1", "oldvalue2"});
    oldParams.put("param2", new String[]{"oldvalue3"});

    String body = "param1=value1&param1=value2&param2=value3";
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(oldParams);
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    expect(request.getInputStream()).andReturn(new MockServletInputStream(body.getBytes()));
    expect(request.getContentLength()).andReturn(body.getBytes().length);
    expect(request.getCharacterEncoding()).andReturn("UTF-8");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);

    @SuppressWarnings("unchecked")
    Map<String, String[]> actual = wrapper.getParameterMap();
    Map<String, String[]> expected =
        MapBuilder.<String, String[]>map()
                  .put("param1", new String[]{"oldvalue1", "oldvalue2", "value1", "value2"})
                  .put("param2", new String[]{"oldvalue3", "value3"})
                  .done();
    assertParameterMapsEquals(actual, expected);

    verify(request, chain);
  }

  @Test
  public void parseMultiple() throws IOException, ServletException {
    String body = "param1=value1&param1=value2&param2=value3";
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    expect(request.getInputStream()).andReturn(new MockServletInputStream(body.getBytes()));
    expect(request.getContentLength()).andReturn(body.getBytes().length);
    expect(request.getCharacterEncoding()).andReturn("UTF-8");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);

    @SuppressWarnings("unchecked")
    Map<String, String[]> actual = wrapper.getParameterMap();
    Map<String, String[]> expected =
        MapBuilder.<String, String[]>map()
                  .put("param1", new String[]{"value1", "value2"})
                  .put("param2", new String[]{"value3"})
                  .done();
    assertParameterMapsEquals(actual, expected);

    verify(request, chain);
  }

  @Test
  public void singleFiles() throws IOException, ServletException {
    String body = FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/servlet/http-test-body-single-file.txt"));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap<>());
    EasyMock.expect(request.getContentType()).andReturn("multipart/form-data, boundary=AaB03x").times(2);
    EasyMock.expect(request.getInputStream()).andReturn(new MockServletInputStream(body.getBytes()));
    EasyMock.expect(request.getCharacterEncoding()).andReturn("UTF-8");
    EasyMock.expect(request.getContentLength()).andReturn(body.length());
    final Capture<Map<String, List<FileInfo>>> capture = new Capture<>();
    request.setAttribute(eq(RequestKeys.FILE_ATTRIBUTE), capture(capture));
    EasyMock.replay(request);

    final AtomicBoolean run = new AtomicBoolean(false);
    MockWorkflowChain chain = new MockWorkflowChain(() -> {
      Map<String, List<FileInfo>> files = capture.getValue();
      assertEquals(files.size(), 1);
      try {
        assertEquals(FileUtils.readFileToString(files.get("userfile").get(0).file), "test");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      run.set(true);
    });

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);
    assertTrue(run.get());

    assertEquals(wrapper.getParameter("field1"), "value1");
    assertEquals(wrapper.getParameter("field2"), "value2");

    EasyMock.verify(request);
  }

  @Test
  public void wrongContentType() throws IOException, ServletException {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(null);
    expect(request.getContentType()).andReturn("text/xml");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    RequestBodyWorkflow workflow = new RequestBodyWorkflow(request);
    workflow.perform(chain);

    verify(request, chain);
  }

  private void assertParameterMapsEquals(Map<String, String[]> actual, Map<String, String[]> expected) {
    assertEquals(actual.size(), expected.size(), "Maps have different sizes. Key difference is: " + keyDiff(actual, expected) + "\nActual:   " + actual + "\nExpected: " + expected);
    assertEquals(actual.keySet(), expected.keySet(), "Maps have different keys");
    for (String key : actual.keySet()) {
      assertEquals(actual.get(key), expected.get(key), "Maps have different arrays for key [" + key + "]\nActual:   " + asList(actual.get(key)) + "\nExpected: " + asList(expected.get(key)));
    }
  }

  private String keyDiff(Map<String, String[]> actual, Map<String, String[]> expected) {
    Set<String> finalSet = new HashSet<>();
    finalSet.addAll(actual.keySet());
    finalSet.removeAll(expected.keySet());
    finalSet.addAll(expected.keySet());
    finalSet.removeAll(actual.keySet());
    return finalSet.toString();
  }
}
