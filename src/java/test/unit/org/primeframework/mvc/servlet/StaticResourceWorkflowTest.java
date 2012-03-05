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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.easymock.EasyMock;
import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.workflow.StaticResourceWorkflow;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p> This tests the static resource workflow. </p>
 *
 * @author Brian Pontarelli
 */
public class StaticResourceWorkflowTest {
  /**
   * Tests that a new request returns the byte stream.
   *
   * @throws IOException      Never.
   * @throws ServletException Never.
   */
  @Test
  public void testNewRequest() throws IOException, ServletException {
    PrimeMVCConfiguration configuration = makeConfiguration();

    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.expect(context.getResource("/component/2.1.1/test.jpg")).andReturn(null);
    EasyMock.replay(context);

    HttpServletRequest req = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(req.getRequestURI()).andReturn("/component/2.1.1/test.jpg");
    EasyMock.expect(req.getContextPath()).andReturn("");
    EasyMock.expect(req.getDateHeader("If-Modified-Since")).andReturn(0l);
    EasyMock.replay(req);

    final StringBuilder build = new StringBuilder();
    ServletOutputStream sos = new ServletOutputStream() {
      public void write(int b) throws IOException {
        build.appendCodePoint(b);
      }
    };

    HttpServletResponse res = EasyMock.createStrictMock(HttpServletResponse.class);
    res.setContentType("image/jpeg");
    res.setDateHeader(EasyMock.eq("Date"), EasyMock.geq(System.currentTimeMillis()));
    res.setDateHeader("Expires", Long.MAX_VALUE);
    res.setDateHeader("Retry-After", Long.MAX_VALUE);
    res.setHeader("Cache-Control", "public");
    res.setDateHeader("Last-Modified", 0);
    EasyMock.expect(res.getOutputStream()).andReturn(sos);
    EasyMock.replay(res);

    WorkflowChain wc = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration);
    srw.perform(wc);
    EasyMock.verify(configuration, req, res, wc);

    assertEquals("Test\n", build.toString());
  }

  /**
   * Tests that a new request with a context path returns the byte stream.
   *
   * @throws IOException      Never.
   * @throws ServletException Never.
   */
  @Test
  public void testNewRequestContext() throws IOException, ServletException {
    PrimeMVCConfiguration configuration = makeConfiguration();

    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.expect(context.getResource("/component/2.1.1/test.jpg")).andReturn(null);
    EasyMock.replay(context);

    HttpServletRequest req = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(req.getRequestURI()).andReturn("/context-path/component/2.1.1/test.jpg");
    EasyMock.expect(req.getContextPath()).andReturn("/context-path");
    EasyMock.expect(req.getDateHeader("If-Modified-Since")).andReturn(0l);
    EasyMock.replay(req);

    final StringBuilder build = new StringBuilder();
    ServletOutputStream sos = new ServletOutputStream() {
      public void write(int b) throws IOException {
        build.appendCodePoint(b);
      }
    };

    HttpServletResponse res = EasyMock.createStrictMock(HttpServletResponse.class);
    res.setContentType("image/jpeg");
    res.setDateHeader(EasyMock.eq("Date"), EasyMock.geq(System.currentTimeMillis()));
    res.setDateHeader("Expires", Long.MAX_VALUE);
    res.setDateHeader("Retry-After", Long.MAX_VALUE);
    res.setHeader("Cache-Control", "public");
    res.setDateHeader("Last-Modified", 0);
    EasyMock.expect(res.getOutputStream()).andReturn(sos);
    EasyMock.replay(res);

    WorkflowChain wc = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration);
    srw.perform(wc);
    EasyMock.verify(configuration, req, res, wc);

    assertEquals("Test\n", build.toString());
  }

  /**
   * Tests that a cache request returns never expire.
   *
   * @throws IOException      Never.
   * @throws ServletException Never.
   */
  @Test
  public void testCacheRequest() throws IOException, ServletException {
    PrimeMVCConfiguration configuration = makeConfiguration();

    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.expect(context.getResource("/component/2.1.1/test.jpg")).andReturn(null);
    EasyMock.replay(context);

    HttpServletRequest req = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(req.getRequestURI()).andReturn("/component/2.1.1/test.jpg");
    EasyMock.expect(req.getContextPath()).andReturn("");
    EasyMock.expect(req.getDateHeader("If-Modified-Since")).andReturn(1l);
    EasyMock.replay(req);

    HttpServletResponse res = EasyMock.createStrictMock(HttpServletResponse.class);
    res.setDateHeader("Expires", Long.MAX_VALUE);
    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    EasyMock.replay(res);

    WorkflowChain wc = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration);
    srw.perform(wc);
    EasyMock.verify(configuration, req, res, wc);
  }

  /**
   * Tests that a bad request returns 404.
   *
   * @throws IOException      Never.
   * @throws ServletException Never.
   */
  @Test
  public void testBadRequest() throws IOException, ServletException {
    PrimeMVCConfiguration configuration = makeConfiguration();

    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.expect(context.getResource("/component/2.1.1/bad.jpg")).andReturn(null);
    EasyMock.replay(context);

    HttpServletRequest req = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(req.getRequestURI()).andReturn("/component/2.1.1/bad.jpg");
    EasyMock.expect(req.getContextPath()).andReturn("");
    EasyMock.expect(req.getDateHeader("If-Modified-Since")).andReturn(0l);
    EasyMock.replay(req);

    HttpServletResponse res = EasyMock.createStrictMock(HttpServletResponse.class);
    EasyMock.replay(res);

    WorkflowChain wc = EasyMock.createStrictMock(WorkflowChain.class);
    wc.continueWorkflow();
    EasyMock.replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration);
    srw.perform(wc);
    EasyMock.verify(configuration, req, res, wc);
  }

  /**
   * Tests that a normal request goes through to the chain.
   *
   * @throws IOException      Never.
   * @throws ServletException Never.
   */
  @Test
  public void testNormal() throws IOException, ServletException {
    PrimeMVCConfiguration configuration = makeConfiguration();

    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.replay(context);

    HttpServletRequest req = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(req.getRequestURI()).andReturn("/foo/bar");
    EasyMock.expect(req.getContextPath()).andReturn("");
    EasyMock.replay(req);

    HttpServletResponse res = EasyMock.createStrictMock(HttpServletResponse.class);
    EasyMock.replay(res);

    WorkflowChain wc = EasyMock.createStrictMock(WorkflowChain.class);
    wc.continueWorkflow();
    EasyMock.replay(wc);

    StaticResourceWorkflow srw = new StaticResourceWorkflow(context, req, res, configuration);
    srw.perform(wc);
    EasyMock.verify(configuration, req, res, wc);
  }

  private PrimeMVCConfiguration makeConfiguration() {
    PrimeMVCConfiguration configuration = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    String[] prefixes = new String[]{"/module", "/component", "/static", "/jcatapult"};
    EasyMock.expect(configuration.staticResourcePrefixes()).andReturn(prefixes);
    EasyMock.expect(configuration.staticResourcesEnabled()).andReturn(true);
    EasyMock.replay(configuration);
    return configuration;
  }
}