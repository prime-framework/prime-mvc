/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Locale;

import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * <p> This class tests the forward result. </p>
 *
 * @author Brian Pontarelli
 */
public class ForwardResultTest {
  @Test
  public void fullyQualified() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    replay(response);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    RequestDispatcher dispatcher = createStrictMock(RequestDispatcher.class);
    dispatcher.forward(request, response);
    replay(dispatcher);

    expect(request.getRequestDispatcher("/foo/bar.jsp")).andReturn(dispatcher);
    replay(request);

    ServletContext context = createStrictMock(ServletContext.class);
    replay(context);

    Forward forward = new ForwardResult.ForwardImpl("/foo/bar.jsp", null);
    ForwardResult forwardResult = new ForwardResult(Locale.CANADA, context, request, response, null, null, null);
    forwardResult.execute(forward, new DefaultActionInvocation(null, "/foo/bar", null, null));

    verify(context, dispatcher, request, response);
  }

  @Test
  public void relative() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    replay(response);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    RequestDispatcher dispatcher = createStrictMock(RequestDispatcher.class);
    dispatcher.forward(request, response);
    replay(dispatcher);

    expect(request.getRequestDispatcher("/WEB-INF/content/bar.jsp")).andReturn(dispatcher);
    replay(request);

    ServletContext context = createStrictMock(ServletContext.class);
    replay(context);

    Forward forward = new ForwardResult.ForwardImpl("bar.jsp", null);
    ForwardResult forwardResult = new ForwardResult(Locale.GERMAN, context, request, response, null, null, null);
    forwardResult.execute(forward, new DefaultActionInvocation(null, "/action", null, null));

    verify(context, dispatcher, request, response);
  }

  @Test
  public void status() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(300);
    replay(response);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    RequestDispatcher dispatcher = createStrictMock(RequestDispatcher.class);
    dispatcher.forward(request, response);
    replay(dispatcher);

    expect(request.getRequestDispatcher("/WEB-INF/content/bar.jsp")).andReturn(dispatcher);
    replay(request);

    ServletContext context = createStrictMock(ServletContext.class);
    replay(context);

    Forward forward = new ForwardResult.ForwardImpl("bar.jsp", null, "text/html; charset=UTF-8", 300);
    ForwardResult forwardResult = new ForwardResult(Locale.GERMAN, context, request, response, null, null, null);
    forwardResult.execute(forward, new DefaultActionInvocation(null, "/action", null, null));

    verify(context, dispatcher, request);
  }

  @Test
  public void expansions() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/xml; charset=UTF-8");
    response.setStatus(300);
    replay(response);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    RequestDispatcher dispatcher = createStrictMock(RequestDispatcher.class);
    dispatcher.forward(isA(HttpServletRequest.class), same(response));
    replay(dispatcher);

    expect(request.getRequestDispatcher("/WEB-INF/content/bar.jsp")).andReturn(dispatcher);
    replay(request);

    ServletContext context = createStrictMock(ServletContext.class);
    replay(context);

    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${contentType}", action)).andReturn("text/xml; charset=UTF-8");
    expect(ee.expand("${page}", action)).andReturn("bar.jsp");
    replay(ee);

    Forward forward = new ForwardResult.ForwardImpl("${page}", null, "${contentType}", 300);
    ForwardResult forwardResult = new ForwardResult(Locale.GERMAN, context, request, response, ee, null, null);
    forwardResult.execute(forward, new DefaultActionInvocation(action, "/action", null, null));

    verify(context, dispatcher, request);
  }

  @Test
  public void contentType() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/javascript; charset=UTF-8");
    response.setStatus(200);
    replay(response);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    RequestDispatcher dispatcher = createStrictMock(RequestDispatcher.class);
    dispatcher.forward(request, response);
    replay(dispatcher);

    expect(request.getRequestDispatcher("/WEB-INF/content/bar.jsp")).andReturn(dispatcher);
    replay(request);

    ServletContext context = createStrictMock(ServletContext.class);
    replay(context);

    Forward forward = new ForwardResult.ForwardImpl("bar.jsp", null, "text/javascript; charset=UTF-8", 200);
    ForwardResult forwardResult = new ForwardResult(Locale.GERMAN, context, request, response, null, null, null);
    forwardResult.execute(forward, new DefaultActionInvocation(null, "/action", null, null));

    verify(context, dispatcher, request);
  }

  @Test
  public void search() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new PrintWriter(System.out));
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(null);
    replay(request);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(same(writer), eq("/WEB-INF/content/action.ftl"), isA(FreeMarkerMap.class), same(Locale.GERMAN));
    replay(service);

    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/content/action-js-failure.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/content/action-js-failure.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/content/action-js.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/content/action-js.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/content/action-failure.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/content/action-failure.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/content/action.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/content/action.ftl")).andReturn(new URL("http://localhost"));
    replay(context);

    Forward forward = new ForwardResult.ForwardImpl("", "failure");
    ForwardResult forwardResult = new ForwardResult(Locale.GERMAN, context, request, response, null, service, null);
    forwardResult.execute(forward, new DefaultActionInvocation(null, "/action", "js", null));

    verify(context, service, request);
  }
}
