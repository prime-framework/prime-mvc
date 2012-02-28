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
package org.primeframework.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.primeframework.config.PrimeMVCConfiguration;

import com.google.inject.Inject;

/**
 * <p>
 * This class handles static resources via the JCatapult workflow chain.
 * In order to handle resources that will be coming from JAR files and the
 * web application directories, this first checks the incoming request URI
 * against a pre-defined set of prefixes. This set of prefixes is controlled
 * using the configuration parameter named <code>jcatapult.static-resource.prefixes</code>
 * which is a string array configuration (meaning you can specify it using a
 * comma-seperated list or multiple configuration values with the same
 * name). The default list of prefixes is:
 * </p>
 *
 * <pre>
 * /module
 * /component
 * /jcatapult
 * /static
 * </pre>
 *
 * <p>
 * This workflow can also be turned off completely using the configuration
 * parameter named <code>jcatapult.static-resource.enabled<code>.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class StaticResourceWorkflow implements Workflow {
    private static final Logger logger = Logger.getLogger(StaticResourceWorkflow.class.getName());
    private final ServletContext context;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final String[] staticPrefixes;
    private final boolean enabled;

    @Inject
    public StaticResourceWorkflow(ServletContext context, HttpServletRequest request,
            HttpServletResponse response, PrimeMVCConfiguration configuration) {
        this.context = context;
        this.request = request;
        this.response = response;
        this.staticPrefixes = configuration.staticResourcePrefixes();
        this.enabled = configuration.staticResourcesEnabled();
    }

    /**
     * Checks for static resource request and if it is one, locates and sends back the static resource.
     * If it isn't one, it passes control down the chain.
     *
     * @param   workflowChain The workflow chain to use if the request is not a static resource.
     * @throws  IOException If the request is a static resource and sending it failed or if the
     *          chain throws an IOException.
     * @throws  ServletException If the chain throws.
     */
    public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
        String uri = ServletTools.getRequestURI(request);
        boolean handled = false;
        if (enabled) {
            // Ensure that this is a request for a resource like foo.jpg
            int dot = uri.lastIndexOf('.');
            int slash = dot >= 0 ? uri.indexOf('/', dot) : 1;
            if (slash == -1 && !uri.endsWith(".class")) {
                for (String staticPrefix : staticPrefixes) {
                    if (uri.startsWith(staticPrefix)) {
                        handled = findStaticResource(uri, request, response);
                    }
                }
            }
        }

        if (!handled) {
            workflowChain.continueWorkflow();
        }
    }

    /**
     * Locate a static resource and copy directly to the response, setting the appropriate caching headers.
     *
     * @param   uri The resource uri.
     * @param   request The request
     * @param   response The response
     * @return  True if the resource was found in the classpath and if it was successfully written
     *          back to the output stream. Otherwise, this returns false if the resource doesn't
     *          exist in the classpath.
     * @throws  IOException If anything goes wrong
     */
    protected boolean findStaticResource(String uri, HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        if (context.getResource(uri) != null) {
            // It is in the webapp, just return false and let the container deal with it
            return false;
        }

        // check for if-modified-since, prior to any other headers
        long ifModifiedSince = 0;
        try {
            ifModifiedSince = request.getDateHeader("If-Modified-Since");
        } catch (Exception e) {
            logger.warning("Invalid If-Modified-Since header value [" +
                request.getHeader("If-Modified-Since") + "], ignoring");
        }

        if (ifModifiedSince > 0) {
            // not modified, content is not sent - only basic headers and status SC_NOT_MODIFIED
            response.setDateHeader("Expires", Long.MAX_VALUE);
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }

        InputStream is = findInputStream(uri);
        if (is == null) {
            return false;
        }

        // Set the content-type header
        String contentType = getContentType(uri);
        if (contentType != null) {
            response.setContentType(contentType);
        }

        // Components manage caching based on version numbers, so we can cache forever
        Calendar now = Calendar.getInstance();
        response.setDateHeader("Date", now.getTimeInMillis());
        response.setDateHeader("Expires", Long.MAX_VALUE);
        response.setDateHeader("Retry-After", Long.MAX_VALUE);
        response.setHeader("Cache-Control", "public");
        response.setDateHeader("Last-Modified", 0);

        try {
            ServletOutputStream sos = response.getOutputStream();

            // Then output the file
            byte[] b = new byte[8192];
            int len;
            do {
                len = is.read(b);
                if (len > 0) {
                    sos.write(b, 0, len);
                }
            } while (len != -1);

            sos.flush();
        } finally {
            is.close();
        }

        return true;
    }

    /**
     * Look for a static resource in the classpath.
     *
     * @param   uri The resource URI.
     * @return  The inputstream of the resource.
     * @throws  IOException If there is a problem locating the resource.
     */
    protected InputStream findInputStream(String uri) throws IOException {
        uri = URLDecoder.decode(uri, "UTF-8");
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(uri);
    }

    /**
     * Determine the content type for the resource name.
     *
     * @param   name The resource name.
     * @return  The mime type.
     */
    protected String getContentType(String name) {
        // NOT using the code provided activation.jar to avoid adding yet another dependency
        // this is generally OK, since these are the main files we server up
        if (name.endsWith(".js")) {
            return "text/javascript";
        } else if (name.endsWith(".css")) {
            return "text/css";
        } else if (name.endsWith(".html")) {
            return "text/html";
        } else if (name.endsWith(".txt")) {
            return "text/plain";
        } else if (name.endsWith(".gif")) {
            return "image/gif";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (name.endsWith(".png")) {
            return "image/png";
        } else {
            return null;
        }
    }
}