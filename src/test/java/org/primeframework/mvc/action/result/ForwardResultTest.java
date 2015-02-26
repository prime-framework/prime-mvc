/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * This class tests the forward result.
 *
 * @author Brian Pontarelli
 */
public class ForwardResultTest {
  @Test
  public void fullyQualified() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ExecuteMethodConfiguration methodConfiguration = createStrictMock(ExecuteMethodConfiguration.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, methodConfiguration, "/foo/bar", null, null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "/foo/bar.ftl", map);
    replay(service);

    Forward forward = new ForwardResult.ForwardImpl("/foo/bar.ftl", null);
    ForwardResult forwardResult = new ForwardResult(store, null, null, service, response, map);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void relative() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ExecuteMethodConfiguration methodConfiguration = createStrictMock(ExecuteMethodConfiguration.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, methodConfiguration, "/action", "", null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "/WEB-INF/templates/bar.ftl", map);
    replay(service);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null);
    ForwardResult forwardResult = new ForwardResult(store, null, null, service, response, map);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void relativeNested() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ExecuteMethodConfiguration methodConfiguration = createStrictMock(ExecuteMethodConfiguration.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, methodConfiguration, "/action/nested", "", null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "/WEB-INF/templates/action/bar.ftl", map);
    replay(service);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null);
    ForwardResult forwardResult = new ForwardResult(store, null, null, service, response, map);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void status() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(300);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ExecuteMethodConfiguration methodConfiguration = createStrictMock(ExecuteMethodConfiguration.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, methodConfiguration, "/action", null, null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "/WEB-INF/templates/bar.ftl", map);
    replay(service);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null, "text/html; charset=UTF-8", 300);
    ForwardResult forwardResult = new ForwardResult(store, null, null, service, response, map);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void expansions() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/xml; charset=UTF-8");
    response.setStatus(300);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${contentType}", action, false)).andReturn("text/xml; charset=UTF-8");
    expect(ee.expand("${page}", action, false)).andReturn("bar.ftl");
    replay(ee);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ExecuteMethodConfiguration methodConfiguration = createStrictMock(ExecuteMethodConfiguration.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, methodConfiguration, "/action", "", null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "/WEB-INF/templates/bar.ftl", map);
    replay(service);

    Forward forward = new ForwardResult.ForwardImpl("${page}", null, "${contentType}", 300);
    ForwardResult forwardResult = new ForwardResult(store, ee, null, service, response, map);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void contentType() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/javascript; charset=UTF-8");
    response.setStatus(200);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ExecuteMethodConfiguration methodConfiguration = createStrictMock(ExecuteMethodConfiguration.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, methodConfiguration, "/action", null, null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "/WEB-INF/templates/bar.ftl", map);
    replay(service);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null, "text/javascript; charset=UTF-8", 200);
    ForwardResult forwardResult = new ForwardResult(store, null, null, service, response, map);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void search() throws IOException, ServletException {
    PrintWriter writer = new PrintWriter(new PrintWriter(System.out));

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(same(writer), eq("/WEB-INF/templates/action.ftl"), isA(FreeMarkerMap.class));
    replay(service);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    ResourceLocator locator = createStrictMock(ResourceLocator.class);
    expect(locator.locate("/WEB-INF/templates")).andReturn("/WEB-INF/templates/action.ftl");
    replay(locator);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ExecuteMethodConfiguration methodConfiguration = createStrictMock(ExecuteMethodConfiguration.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, methodConfiguration, "/action", "js", null));
    replay(store);

    Forward forward = new ForwardResult.ForwardImpl("", "failure");
    ForwardResult forwardResult = new ForwardResult(store, null, locator, service, response, map);
    forwardResult.execute(forward);

    verify(response, service, map, locator, store);
  }
}
