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
package org.primeframework.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Toolkit for Servlet related tasks
 *
 * User: jhumphrey
 * Date: Apr 23, 2008
 */
public class ServletTools {

    private static final Logger logger = Logger.getLogger(ServletTools.class.getName());

    /**
     * Returns the base url in the following format:
     *
     * protocol://host[:port]/
     *
     * The protocol, host and port are extracted from the ServletRequest using the following methods:
     *
     * protocol = ServletRequest.getScheme()
     * host = ServletRequest.getServerName()
     * port = ServletRequest.getServerPost()
     *
     * Below are some examples of base URLs that can be returned:
     *
     * <strong>http://www.jcatapult.org/</strong>
     * <strong>https://www.jcatapult.org:8080/</strong>
     *
     * @param request the ServletRequest
     * @return a URL in the format of protocol://host[:port]/
     */
    public static URL getBaseUrl(ServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        URL baseUrl = null;
        try {
            if (serverPort > 0) {
                baseUrl = new URL(scheme, serverName, serverPort, "/");
            } else {
                baseUrl = new URL(scheme, serverName, "/");
            }
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return baseUrl;
    }

    /**
     * The request URI without the context path.
     *
     * @param   request The request.
     * @return  The uri minus the context path.
     */
    public static String getRequestURI(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context.length() > 0) {
            return uri.substring(context.length());
        }

        return uri;
    }
}
