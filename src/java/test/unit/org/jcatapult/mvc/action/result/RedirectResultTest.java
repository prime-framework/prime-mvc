/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.action.result;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;
import org.jcatapult.mvc.action.DefaultActionInvocation;
import org.jcatapult.mvc.action.result.annotation.Redirect;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;
import org.junit.Test;

/**
 * <p>
 * This class tests the redirect result.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class RedirectResultTest {
    @Test
    public void testFullyQualified() throws IOException, ServletException {
        ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
        replay(ee);

        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("");
        replay(request);

        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        response.setStatus(301);
        response.sendRedirect("http://www.google.com");
        replay(response);

        Redirect redirect = new RedirectImpl("success", "http://www.google.com", true, false);
        RedirectResult forwardResult = new RedirectResult(ee, response, request);
        forwardResult.execute(redirect, new DefaultActionInvocation(null, "/foo", "", null));

        verify(response);
    }

    @Test
    public void testRelative() throws IOException, ServletException {
        ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
        replay(ee);

        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("");
        replay(request);

        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        response.setStatus(302);
        response.sendRedirect("/foo/bar.jsp");
        replay(response);

        Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
        RedirectResult forwardResult = new RedirectResult(ee, response, request);
        forwardResult.execute(redirect, new DefaultActionInvocation(null, "foo", "", null));

        verify(response);
    }

    @Test
    public void testRelativeContext() throws IOException, ServletException {
        ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
        replay(ee);

        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("/context-path");
        replay(request);

        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        response.setStatus(302);
        response.sendRedirect("/context-path/foo/bar.jsp");
        replay(response);

        Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
        RedirectResult forwardResult = new RedirectResult(ee, response, request);
        forwardResult.execute(redirect, new DefaultActionInvocation(null, "foo", "", null));

        verify(response);
    }

    @Test
    public void testRelativeContextNoSlash() throws IOException, ServletException {
        ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
        replay(ee);

        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("/context-path");
        replay(request);

        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        response.setStatus(302);
        response.sendRedirect("foo/bar.jsp");
        replay(response);

        Redirect redirect = new RedirectImpl("success", "foo/bar.jsp", false, false);
        RedirectResult forwardResult = new RedirectResult(ee, response, request);
        forwardResult.execute(redirect, new DefaultActionInvocation(null, "foo", "", null));

        verify(response);
    }

    @Test
    public void testExpand() throws IOException, ServletException {
        Object action = new Object();
        ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
        expect(ee.getValue(eq("foo"), same(action), isA(Map.class))).andReturn("result");
        replay(ee);

        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("");
        replay(request);

        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        response.setStatus(302);
        response.sendRedirect("result");
        replay(response);

        Redirect redirect = new RedirectImpl("success", "${foo}", false, false);
        RedirectResult forwardResult = new RedirectResult(ee, response, request);
        forwardResult.execute(redirect, new DefaultActionInvocation(action, "foo", "", null));

        verify(response);
    }

    @Test
    public void testExpandEncode() throws IOException, ServletException {
        Object action = new Object();
        ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
        expect(ee.getValue(eq("foo"), same(action), isA(Map.class))).andReturn("/result");
        replay(ee);

        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("");
        replay(request);

        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        response.setStatus(302);
        response.sendRedirect("%2Fresult");
        replay(response);

        Redirect redirect = new RedirectImpl("success", "${foo}", false, true);
        RedirectResult forwardResult = new RedirectResult(ee, response, request);
        forwardResult.execute(redirect, new DefaultActionInvocation(action, "foo", "", null));

        verify(response);
    }

    public class RedirectImpl implements Redirect {
        private final String code;
        private final String uri;
        private final boolean perm;
        private final boolean encode;

        public RedirectImpl(String code, String uri, boolean perm, boolean encode) {
            this.code = code;
            this.uri = uri;
            this.perm = perm;
            this.encode = encode;
        }

        public String code() {
            return code;
        }

        public String uri() {
            return uri;
        }

        public boolean perm() {
            return perm;
        }

        public boolean encodeVariables() {
            return encode;
        }

        public Class<? extends Annotation> annotationType() {
            return Redirect.class;
        }
    }
}