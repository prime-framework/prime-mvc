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
package org.primeframework.mvc.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easymock.EasyMock;
import org.easymock.Capture;
import org.primeframework.mock.servlet.FileInfo;
import org.primeframework.mock.servlet.MockServletInputStream;
import org.testng.annotations.Test;

import net.java.io.FileTools;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * <p> This class tests the request body workflow. </p>
 *
 * @author Brian Pontarelli
 */
public class RequestBodyWorkflowTest {
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

  @Test
  public void containerDrainedBody() throws IOException, ServletException {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    expect(request.getInputStream()).andReturn(new MockServletInputStream(new byte[0]));
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
  public void parse() throws IOException, ServletException {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    expect(request.getInputStream()).andReturn(new MockServletInputStream("param1=value1&param2=value2".getBytes()));
    expect(request.getCharacterEncoding()).andReturn("UTF-8");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);

    Map<String, String[]> params = wrapper.getParameterMap();
    assertEquals(params.size(), 2);
    assertEquals(params.get("param1").length, 1);
    assertEquals(params.get("param1")[0], "value1");
    assertEquals(params.get("param2").length, 1);
    assertEquals(params.get("param2")[0], "value2");

    verify(request, chain);
  }

  @Test
  public void parseMultiple() throws IOException, ServletException {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    expect(request.getInputStream()).andReturn(new MockServletInputStream("param1=value1&param1=value2&param2=value3".getBytes()));
    expect(request.getCharacterEncoding()).andReturn("UTF-8");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);

    Map<String, String[]> params = wrapper.getParameterMap();
    assertEquals(params.size(), 2);
    assertEquals(params.get("param1").length, 2);
    assertEquals(params.get("param1")[0], "value1");
    assertEquals(params.get("param1")[1], "value2");
    assertEquals(params.get("param2").length, 1);
    assertEquals(params.get("param2")[0], "value3");

    verify(request, chain);
  }

  @Test
  public void parseCombine() throws IOException, ServletException {
    Map<String, String[]> oldParams = new HashMap<String, String[]>();
    oldParams.put("param1", new String[]{"oldvalue1", "oldvalue2"});
    oldParams.put("param2", new String[]{"oldvalue3"});

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(oldParams);
    expect(request.getContentType()).andReturn("application/x-www-form-urlencoded");
    expect(request.getInputStream()).andReturn(new MockServletInputStream("param1=value1&param1=value2&param2=value3".getBytes()));
    expect(request.getCharacterEncoding()).andReturn("UTF-8");
    replay(request);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);

    Map<String, String[]> params = wrapper.getParameterMap();
    assertEquals(params.size(), 2);
    assertEquals(params.get("param1").length, 4);
    assertEquals(params.get("param1")[0], "oldvalue1");
    assertEquals(params.get("param1")[1], "oldvalue2");
    assertEquals(params.get("param1")[2], "value1");
    assertEquals(params.get("param1")[3], "value2");
    assertEquals(params.get("param2").length, 2);
    assertEquals(params.get("param2")[0], "oldvalue3");
    assertEquals(params.get("param2")[1], "value3");

    verify(request, chain);
  }

  @Test
  public void singleFiles() throws IOException, ServletException {
    String body = FileTools.read("src/java/test/unit/org/primeframework/mvc/servlet/http-test-body-single-file.txt").toString();

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getContentType()).andReturn("multipart/form-data, boundary=AaB03x").times(2);
    EasyMock.expect(request.getInputStream()).andReturn(new MockServletInputStream(body.getBytes()));
    EasyMock.expect(request.getCharacterEncoding()).andReturn("UTF-8");
    EasyMock.expect(request.getContentLength()).andReturn(body.length());
    final Capture<Map<String, List<FileInfo>>> capture = new Capture<Map<String, List<FileInfo>>>();
    request.setAttribute(eq(RequestKeys.FILE_ATTRIBUTE), capture(capture));
    EasyMock.replay(request);

    final AtomicBoolean run = new AtomicBoolean(false);
    MockWorkflowChain chain = new MockWorkflowChain(new Runnable() {
      @Override
      public void run() {
        Map<String, List<FileInfo>> files = capture.getValue();
        assertEquals(1, files.size());
        try {
          assertEquals(FileTools.read(files.get("userfile").get(0).file).toString(), "test");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        run.set(true);
      }
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
  public void multipleFiles() throws IOException, ServletException {
    String body = FileTools.read("src/java/test/unit/org/primeframework/mvc/servlet/http-test-body-multiple-files.txt").toString();

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getContentType()).andReturn("multipart/form-data, boundary=AaB03x").times(2);
    EasyMock.expect(request.getInputStream()).andReturn(new MockServletInputStream(body.getBytes()));
    EasyMock.expect(request.getCharacterEncoding()).andReturn("UTF-8");
    EasyMock.expect(request.getContentLength()).andReturn(body.length());
    final Capture<Map<String, List<FileInfo>>> capture = new Capture<Map<String, List<FileInfo>>>();
    request.setAttribute(eq(RequestKeys.FILE_ATTRIBUTE), capture(capture));
    EasyMock.replay(request);

    final AtomicBoolean run = new AtomicBoolean(false);
    MockWorkflowChain chain = new MockWorkflowChain(new Runnable() {
      @Override
      public void run() {
        Map<String, List<FileInfo>> files = capture.getValue();
        assertEquals(1, files.size());
        try {
          assertEquals(FileTools.read(files.get("userfiles").get(0).file).toString(), "test");
          assertEquals(FileTools.read(files.get("userfiles").get(1).file).toString(), "test2");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        run.set(true);
      }
    });

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    RequestBodyWorkflow workflow = new RequestBodyWorkflow(wrapper);
    workflow.perform(chain);
    assertTrue(run.get());

    assertEquals(wrapper.getParameter("field1"), "value1");
    assertEquals(wrapper.getParameter("field2"), "value2");

    EasyMock.verify(request);
  }
}
