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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * <p>
 * This class tests the default result invocation provider.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultResultInvocationProviderTest {
    @Test
    public void testActionLess() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(ai);
        assertNotNull(invocation);
        assertNull(invocation.resultCode());
        assertEquals("/foo/bar", invocation.uri());
        assertNull(((Forward) invocation.annotation()).code());
        assertEquals("/WEB-INF/content/foo/bar.ftl", ((Forward) invocation.annotation()).page());

        EasyMock.verify(context);
    }

    @Test
    public void testActionLessWithExtension() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax.ftl")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", "ajax", null);
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(ai);
        assertNotNull(invocation);
        assertNull(invocation.resultCode());
        assertEquals("/foo/bar", invocation.uri());
        assertNull(((Forward) invocation.annotation()).code());
        assertEquals("/WEB-INF/content/foo/bar.ftl", ((Forward) invocation.annotation()).page());

        EasyMock.verify(context);
    }

    @Test
    public void testActionLessRedirectForIndexPage() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.ftl")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(ai);
        assertNotNull(invocation);
        assertNull(invocation.resultCode());
        assertEquals("/foo/bar", invocation.uri());
        assertNull(((Redirect) invocation.annotation()).code());
        assertEquals("/foo/bar/", ((Redirect) invocation.annotation()).uri());

        EasyMock.verify(context);
    }

    @Test
    public void testActionLessRedirectForIndexPageWithExtension() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax.ftl")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar.ftl")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", "ajax", null);
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(ai);
        assertNotNull(invocation);
        assertNull(invocation.resultCode());
        assertEquals("/foo/bar", invocation.uri());
        assertNull(((Redirect) invocation.annotation()).code());
        assertEquals("/foo/bar/", ((Redirect) invocation.annotation()).uri());

        EasyMock.verify(context);
    }

    @Test
    public void testActionLessIndexPage() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar/", null, null);
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(ai);
        assertNotNull(invocation);
        assertNull(invocation.resultCode());
        assertEquals("/foo/bar/", invocation.uri());
        assertNull(((Forward) invocation.annotation()).code());
        assertEquals("/WEB-INF/content/foo/bar/index.ftl", ((Forward) invocation.annotation()).page());

        EasyMock.verify(context);
    }

    @Test
    public void testActionAnnotation() {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.replay(context);

        TestAction action = new TestAction();
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(new DefaultActionInvocation(action, "/foo/bar", null, null), "success");
        assertNotNull(invocation);
        assertEquals("success", invocation.resultCode());
        assertEquals("/foo/bar", invocation.uri());
        assertEquals("success", ((Forward) invocation.annotation()).code());
        assertEquals("foo.jsp", ((Forward) invocation.annotation()).page());

        EasyMock.verify(context);
    }

    @Test
    public void testActionNoAnnotation() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-error.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-error.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        TestAction action = new TestAction();
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(new DefaultActionInvocation(action, "/foo/bar", null, null), "error");
        assertNotNull(invocation);
        assertEquals("error", invocation.resultCode());
        assertEquals("/foo/bar", invocation.uri());
        assertEquals("error", ((Forward) invocation.annotation()).code());
        assertEquals("/WEB-INF/content/foo/bar-error.ftl", ((Forward) invocation.annotation()).page());

        EasyMock.verify(context);
    }

    @Test
    public void testActionNoAnnotationWithExtension() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax-error.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax-error.ftl")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-ajax.ftl")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-error.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar-error.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        TestAction action = new TestAction();
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(new DefaultActionInvocation(action, "/foo/bar", "ajax", null), "error");
        assertNotNull(invocation);
        assertEquals("error", invocation.resultCode());
        assertEquals("/foo/bar", invocation.uri());
        assertEquals("error", ((Forward) invocation.annotation()).code());
        assertEquals("/WEB-INF/content/foo/bar-error.ftl", ((Forward) invocation.annotation()).page());

        EasyMock.verify(context);
    }

    @Test
    public void testActionNoAnnotationIndexPage() throws MalformedURLException {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.jsp")).andReturn(null);
        EasyMock.expect(context.getResource("/WEB-INF/content/foo/bar/index.ftl")).andReturn(new URL("http://google.com"));
        EasyMock.replay(context);

        TestAction action = new TestAction();
        DefaultResultInvocationProvider provider = new DefaultResultInvocationProvider(new ForwardResult(Locale.CANADA, context, null, null, null, null, null));
        ResultInvocation invocation = provider.lookup(new DefaultActionInvocation(action, "/foo/bar/", null, null), "error");
        assertNotNull(invocation);
        assertEquals("error", invocation.resultCode());
        assertEquals("/foo/bar/", invocation.uri());
        assertEquals("error", ((Forward) invocation.annotation()).code());
        assertEquals("/WEB-INF/content/foo/bar/index.ftl", ((Forward) invocation.annotation()).page());

        EasyMock.verify(context);
    }
}