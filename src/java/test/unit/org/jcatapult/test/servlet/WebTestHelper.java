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
 *
 */
package org.jcatapult.test.servlet;

import java.io.File;
import java.util.Locale;

import javax.servlet.http.HttpServletRequestWrapper;

import org.jcatapult.servlet.ServletObjectsHolder;

/**
 * <p>
 * This class is a test helper for web testing. This is useful for
 * unit tests that are NOT extending {@link org.jcatapult.test.JCatapultBaseTest}
 * and need to setup the web objects like HttpServletRequest. If you
 * are using the JCatapultBaseTest, that class handles creation of the
 * web objects and provides methods that can be overridden.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class WebTestHelper {
    public static MockHttpServletRequest request;
    public static MockHttpServletResponse response;
    public static MockServletContext context;
    private static MockHttpSession session;

    /**
     * Creates the request, response and context.
     */
    public static void setUp() {
        WebTestHelper.context = makeContext();
        WebTestHelper.session = makeSession(context);
        WebTestHelper.request = makeRequest(session);
        WebTestHelper.response = makeResponse();

        ServletObjectsHolder.setServletContext(context);
        ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(request));
        ServletObjectsHolder.setServletResponse(response);
    }

    /**
     * Constructs a request whose URI is /test, Locale is US, is a GET and encoded using UTF-8.
     *
     * @param   session The MockHttpSession.
     * @return  The mock request.
     */
    public static MockHttpServletRequest makeRequest(MockHttpSession session) {
        return new MockHttpServletRequest("/test", Locale.US, false, "UTF-8", session);
    }

    /**
     * Constructs a mock response.
     *
     * @return  The mock response.
     */
    public static MockHttpServletResponse makeResponse() {
        return new MockHttpServletResponse();
    }

    /**
     * Constructs a mock session.
     *
     * @param   context The MockServletContext.
     * @return  The mock session.
     */
    public static MockHttpSession makeSession(MockServletContext context) {
        return new MockHttpSession(context);
    }

    /**
     * Constructs a mock servlet context and determines if project that is using this class is a
     * webapp or a module based on if the {@code web} or {@code src/web/test} directory exists
     * (respectively). If neither directory exists, the context will not be able to resolve the
     * real path and lookup resources.
     *
     * @return  The mock context.
     */
    public static MockServletContext makeContext() {
        File webDir = new File("web");
        if (!webDir.isDirectory()) {
            webDir = new File("src/web/test");
            if (!webDir.isDirectory()) {
                webDir = null;
            }
        }

        return new MockServletContext(webDir);
    }
}