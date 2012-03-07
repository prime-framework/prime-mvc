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

import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the default result invocation provider.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationProviderTest {
  @Test
  public void actionLess() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/foo/bar", null, null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup();
    assertNotNull(invocation);
    assertNull(invocation.resultCode());
    assertEquals("/foo/bar", invocation.uri());
    assertNull(((Forward) invocation.annotation()).code());
    assertEquals("/WEB-INF/templates/foo/bar.ftl", ((Forward) invocation.annotation()).page());

    verify(context);
  }

  @Test
  public void actionLessWithExtension() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/foo/bar", "ajax", null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup();
    assertNotNull(invocation);
    assertNull(invocation.resultCode());
    assertEquals("/foo/bar", invocation.uri());
    assertNull(((Forward) invocation.annotation()).code());
    assertEquals("/WEB-INF/templates/foo/bar.ftl", ((Forward) invocation.annotation()).page());

    verify(context);
  }

  @Test
  public void actionLessRedirectForIndexPage() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/foo/bar", null, null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup();
    assertNotNull(invocation);
    assertNull(invocation.resultCode());
    assertEquals("/foo/bar", invocation.uri());
    assertNull(((Redirect) invocation.annotation()).code());
    assertEquals("/foo/bar/", ((Redirect) invocation.annotation()).uri());

    verify(context);
  }

  @Test
  public void actionLessRedirectForIndexPageWithExtension() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/foo/bar", "ajax", null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup();
    assertNotNull(invocation);
    assertNull(invocation.resultCode());
    assertEquals("/foo/bar", invocation.uri());
    assertNull(((Redirect) invocation.annotation()).code());
    assertEquals("/foo/bar/", ((Redirect) invocation.annotation()).uri());

    verify(context);
  }

  @Test
  public void actionLessIndexPage() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/foo/bar/", null, null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup();
    assertNotNull(invocation);
    assertNull(invocation.resultCode());
    assertEquals("/foo/bar/", invocation.uri());
    assertNull(((Forward) invocation.annotation()).code());
    assertEquals("/WEB-INF/templates/foo/bar/index.ftl", ((Forward) invocation.annotation()).page());

    verify(context);
  }

  @Test
  public void actionAnnotation() {
    ServletContext context = createStrictMock(ServletContext.class);
    replay(context);

    TestAction action = new TestAction();

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar", null, null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup("success");
    assertNotNull(invocation);
    assertEquals("success", invocation.resultCode());
    assertEquals("/foo/bar", invocation.uri());
    assertEquals("success", ((Forward) invocation.annotation()).code());
    assertEquals("foo.jsp", ((Forward) invocation.annotation()).page());

    verify(context);
  }

  @Test
  public void actionNoAnnotation() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar-error.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-error.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    TestAction action = new TestAction();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar", null, null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup("error");
    assertNotNull(invocation);
    assertEquals("error", invocation.resultCode());
    assertEquals("/foo/bar", invocation.uri());
    assertEquals("error", ((Forward) invocation.annotation()).code());
    assertEquals("/WEB-INF/templates/foo/bar-error.ftl", ((Forward) invocation.annotation()).page());

    verify(context);
  }

  @Test
  public void actionNoAnnotationWithExtension() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax-error.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax-error.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-ajax.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-error.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar-error.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    TestAction action = new TestAction();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar", "ajax", null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup("error");
    assertNotNull(invocation);
    assertEquals("error", invocation.resultCode());
    assertEquals("/foo/bar", invocation.uri());
    assertEquals("error", ((Forward) invocation.annotation()).code());
    assertEquals("/WEB-INF/templates/foo/bar-error.ftl", ((Forward) invocation.annotation()).page());

    verify(context);
  }

  @Test
  public void testActionNoAnnotationIndexPage() throws MalformedURLException {
    ServletContext context = createStrictMock(ServletContext.class);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.jsp")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
    replay(context);

    TestAction action = new TestAction();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar/", "", null));
    replay(store);

    DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(store, new ForwardResult(store, null, null, context, null, null, null, Locale.CANADA));
    ResultInvocation invocation = provider.lookup("error");
    assertNotNull(invocation);
    assertEquals("error", invocation.resultCode());
    assertEquals("/foo/bar/", invocation.uri());
    assertEquals("error", ((Forward) invocation.annotation()).code());
    assertEquals("/WEB-INF/templates/foo/bar/index.ftl", ((Forward) invocation.annotation()).page());

    verify(context);
  }
}