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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.workflow.StaticResourceWorkflow;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static java.util.Collections.EMPTY_SET;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.geq;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This tests the static resource workflow.
 *
 * @author Brian Pontarelli
 */
public class StaticResourceWorkflowTest {
  @Test
  public void badRequest() throws IOException, ServletException {
    MVCConfiguration configuration = makeConfiguration();

    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/static/2.1.1/bad.jpg")).andReturn(null);
    replay(context);

    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
    expect(req.getRequestURI()).andReturn("/static/2.1.1/bad.jpg");
    expect(req.getContextPath()).andReturn("");
    expect(req.getDateHeader("If-Modified-Since")).andReturn(0l);
    replay(req);

    HttpServletResponse res = createStrictMock(HttpServletResponse.class);
    replay(res);

    WorkflowChain wc = createStrictMock(WorkflowChain.class);
    wc.continueWorkflow();
    replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration, EMPTY_SET);
    srw.perform(wc);
    verify(configuration, req, res, wc);
  }

  @Test
  public void cacheRequest() throws IOException, ServletException {
    MVCConfiguration configuration = makeConfiguration();

    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/static/2.1.1/test.jpg")).andReturn(null);
    replay(context);

    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
    expect(req.getRequestURI()).andReturn("/static/2.1.1/test.jpg");
    expect(req.getContextPath()).andReturn("");
    expect(req.getDateHeader("If-Modified-Since")).andReturn(1l);
    replay(req);

    HttpServletResponse res = createStrictMock(HttpServletResponse.class);
    res.setDateHeader("Expires", Long.MAX_VALUE);
    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    replay(res);

    WorkflowChain wc = createStrictMock(WorkflowChain.class);
    replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration, EMPTY_SET);
    srw.perform(wc);
    verify(configuration, req, res, wc);
  }

  @Test
  public void newRequest() throws IOException, ServletException {
    MVCConfiguration configuration = makeConfiguration();

    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/static/2.1.1/test.jpg")).andReturn(null);
    replay(context);

    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
    expect(req.getRequestURI()).andReturn("/static/2.1.1/test.jpg");
    expect(req.getContextPath()).andReturn("");
    expect(req.getDateHeader("If-Modified-Since")).andReturn(0l);
    replay(req);

    final StringBuilder build = new StringBuilder();
    ServletOutputStream sos = new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
      }

      public void write(int b) throws IOException {
        build.appendCodePoint(b);
      }
    };

    HttpServletResponse res = createStrictMock(HttpServletResponse.class);
    res.setContentType("image/jpeg");
    res.setDateHeader(eq("Date"), geq(System.currentTimeMillis()));
    res.setDateHeader("Expires", Long.MAX_VALUE);
    res.setDateHeader("Retry-After", Long.MAX_VALUE);
    res.setHeader("Cache-Control", "public");
    res.setDateHeader("Last-Modified", 0);
    expect(res.getOutputStream()).andReturn(sos);
    replay(res);

    WorkflowChain wc = createStrictMock(WorkflowChain.class);
    replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration, EMPTY_SET);
    srw.perform(wc);
    verify(configuration, req, res, wc);

    assertEquals(build.toString(), "Test\n");
  }

  @Test
  public void newRequestContext() throws IOException, ServletException {
    MVCConfiguration configuration = makeConfiguration();

    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/static/2.1.1/test.jpg")).andReturn(null);
    replay(context);

    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
    expect(req.getRequestURI()).andReturn("/context-path/static/2.1.1/test.jpg");
    expect(req.getContextPath()).andReturn("/context-path");
    expect(req.getDateHeader("If-Modified-Since")).andReturn(0l);
    replay(req);

    final StringBuilder build = new StringBuilder();
    ServletOutputStream sos = new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
      }

      public void write(int b) throws IOException {
        build.appendCodePoint(b);
      }
    };

    HttpServletResponse res = createStrictMock(HttpServletResponse.class);
    res.setContentType("image/jpeg");
    res.setDateHeader(eq("Date"), geq(System.currentTimeMillis()));
    res.setDateHeader("Expires", Long.MAX_VALUE);
    res.setDateHeader("Retry-After", Long.MAX_VALUE);
    res.setHeader("Cache-Control", "public");
    res.setDateHeader("Last-Modified", 0);
    expect(res.getOutputStream()).andReturn(sos);
    replay(res);

    WorkflowChain wc = createStrictMock(WorkflowChain.class);
    replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration, EMPTY_SET);
    srw.perform(wc);
    verify(configuration, req, res, wc);

    assertEquals(build.toString(), "Test\n");
  }

  @Test
  public void normal() throws IOException, ServletException {
    MVCConfiguration configuration = makeConfiguration();

    ServletContext context = createStrictMock(ServletContext.class);
    replay(context);

    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
    expect(req.getRequestURI()).andReturn("/foo/bar");
    expect(req.getContextPath()).andReturn("");
    replay(req);

    HttpServletResponse res = createStrictMock(HttpServletResponse.class);
    replay(res);

    WorkflowChain wc = createStrictMock(WorkflowChain.class);
    wc.continueWorkflow();
    replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration, EMPTY_SET);
    srw.perform(wc);
    verify(configuration, req, res, wc);
  }

  private MVCConfiguration makeConfiguration() {
    MVCConfiguration configuration = createStrictMock(MVCConfiguration.class);
    String[] prefixes = new String[]{"/static"};
    expect(configuration.staticResourcePrefixes()).andReturn(prefixes);
    expect(configuration.staticResourcesEnabled()).andReturn(true);
    replay(configuration);
    return configuration;
  }
}