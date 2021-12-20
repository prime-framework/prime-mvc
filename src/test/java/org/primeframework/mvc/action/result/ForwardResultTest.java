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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;

/**
 * This class tests the forward result.
 *
 * @author Brian Pontarelli
 */
public class ForwardResultTest extends PrimeBaseTest {
  @Test
  public void contentType() throws Exception {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HTTPResponse response = createStrictMock(HTTPResponse.class);
    response.setContentType("text/javascript; charset=UTF-8");
    response.setStatus(200);
    response.setHeader("Cache-Control", "no-cache");
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, new ExecuteMethodConfiguration(HTTPMethod.GET, null, null), "/action", null, null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "templates/bar.ftl", map);
    replay(service);

    ResourceLocator locator = createStrictMock(ResourceLocator.class);
    expect(locator.locate("templates")).andReturn("templates/action.ftl");
    replay(locator);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null, "text/javascript; charset=UTF-8", 200);
    ForwardResult forwardResult = new ForwardResult(store, null, locator, service, response, map, configuration);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void expansions() throws Exception {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HTTPResponse response = createStrictMock(HTTPResponse.class);
    response.setContentType("text/xml; charset=UTF-8");
    response.setStatus(300);
    response.setHeader("Cache-Control", "no-cache");
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${contentType}", action, false)).andReturn("text/xml; charset=UTF-8");
    expect(ee.expand("templates/${page}", action, false)).andReturn("templates/bar.ftl");
    replay(ee);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.GET, null, null), "/action", "", null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "templates/bar.ftl", map);
    replay(service);

    Forward forward = new ForwardResult.ForwardImpl("${page}", null, "${contentType}", 300);
    ForwardResult forwardResult = new ForwardResult(store, ee, null, service, response, map, configuration);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test(dataProvider = "httpMethod")
  public void fullyQualified(HTTPMethod httpMethod) throws Exception {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HTTPResponse response = createStrictMock(HTTPResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    response.setHeader("Cache-Control", "no-cache");
    if (httpMethod == HTTPMethod.GET) {
      expect(response.getWriter()).andReturn(writer);
    }
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, new ExecuteMethodConfiguration(httpMethod, null, null), "/foo/bar", null, null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "templates/foo/bar.ftl", map);
    replay(service);

    MVCConfiguration configuration = createStrictMock(MVCConfiguration.class);
    expect(configuration.templateDirectory()).andReturn("templates").anyTimes();
    replay(configuration);

    ResourceLocator locator = createStrictMock(ResourceLocator.class);
    expect(locator.locate("templates")).andReturn("templates/action.ftl");
    replay(locator);

    Forward forward = new ForwardResult.ForwardImpl("/foo/bar.ftl", null);
    ForwardResult forwardResult = new ForwardResult(store, null, locator, service, response, map, configuration);
    forwardResult.execute(forward);

    if (httpMethod == HTTPMethod.GET) {
      verify(response, store, map, service);
    }
  }

  @DataProvider(name = "httpMethod")
  public Object[][] httpMethod() {
    return new Object[][]{{HTTPMethod.GET}, {HTTPMethod.HEAD}};
  }

  @Test
  public void relative() throws Exception {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HTTPResponse response = createStrictMock(HTTPResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    response.setHeader("Cache-Control", "no-cache");
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, new ExecuteMethodConfiguration(HTTPMethod.GET, null, null), "/action", "", null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "templates/bar.ftl", map);
    replay(service);

    ResourceLocator locator = createStrictMock(ResourceLocator.class);
    expect(locator.locate("templates")).andReturn("templates/action.ftl");
    replay(locator);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null);
    ForwardResult forwardResult = new ForwardResult(store, null, locator, service, response, map, configuration);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void relativeNested() throws Exception {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HTTPResponse response = createStrictMock(HTTPResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    response.setHeader("Cache-Control", "no-cache");
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, new ExecuteMethodConfiguration(HTTPMethod.GET, null, null), "/action/nested", "", null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "templates/action/bar.ftl", map);
    replay(service);

    ResourceLocator locator = createStrictMock(ResourceLocator.class);
    expect(locator.locate("templates")).andReturn("templates/action.ftl");
    replay(locator);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null);
    ForwardResult forwardResult = new ForwardResult(store, null, locator, service, response, map, configuration);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }

  @Test
  public void search() throws Exception {
    PrintWriter writer = new PrintWriter(new PrintWriter(System.out));

    HTTPResponse response = createStrictMock(HTTPResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(200);
    response.setHeader("Cache-Control", "no-cache");
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(same(writer), eq("templates/action.ftl"), isA(FreeMarkerMap.class));
    replay(service);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    ResourceLocator locator = createStrictMock(ResourceLocator.class);
    expect(locator.locate("templates")).andReturn("templates/action.ftl").times(2);
    replay(locator);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, new ExecuteMethodConfiguration(HTTPMethod.GET, null, null), "/action", "js", null));
    replay(store);

    Forward forward = new ForwardResult.ForwardImpl("", "failure");
    ForwardResult forwardResult = new ForwardResult(store, null, locator, service, response, map, configuration);
    forwardResult.execute(forward);

    verify(response, service, map, locator, store);
  }

  @Test
  public void status() throws Exception {
    PrintWriter writer = new PrintWriter(new StringWriter());

    HTTPResponse response = createStrictMock(HTTPResponse.class);
    response.setContentType("text/html; charset=UTF-8");
    response.setStatus(300);
    response.setHeader("Cache-Control", "no-cache");
    expect(response.getWriter()).andReturn(writer);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, new ExecuteMethodConfiguration(HTTPMethod.GET, null, null), "/action", null, null));
    replay(store);

    FreeMarkerMap map = createStrictMock(FreeMarkerMap.class);
    replay(map);

    FreeMarkerService service = createStrictMock(FreeMarkerService.class);
    service.render(writer, "templates/bar.ftl", map);
    replay(service);

    ResourceLocator locator = createStrictMock(ResourceLocator.class);
    expect(locator.locate("templates")).andReturn("templates/action.ftl");
    replay(locator);

    Forward forward = new ForwardResult.ForwardImpl("bar.ftl", null, "text/html; charset=UTF-8", 300);
    ForwardResult forwardResult = new ForwardResult(store, null, locator, service, response, map, configuration);
    forwardResult.execute(forward);

    verify(response, store, map, service);
  }
}
