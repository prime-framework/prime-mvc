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
 *
 */
package org.primeframework.test.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.java.lang.ClassPath;
import net.java.util.IteratorEnumeration;

/**
 * <p>
 * This is a mock servlet context.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class MockServletContext implements ServletContext {
    private static final String WEB_INF_LIB = "/WEB-INF/lib";
    protected final Map<String, Object> attributes = new HashMap<String, Object>();
    protected File webDir;
    protected ClassPath classPath;

    public MockServletContext() {
        try {
            classPath = ClassPath.getCurrentClassPath();
        } catch (IOException e) {
            throw new RuntimeException("Unable to determine current classpath");
        }
    }

    public MockServletContext(File webDir) {
        this();
        this.webDir = webDir;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return new IteratorEnumeration(attributes.keySet().iterator());
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public ServletContext getContext(String s) {
        return null;
    }

  @Override
  public String getContextPath() {
    return null;
  }

  public int getMajorVersion() {
        return 0;
    }

    public int getMinorVersion() {
        return 0;
    }

    public String getMimeType(String s) {
        return null;
    }

    public Set getResourcePaths(String path) {
        if (path.equals(WEB_INF_LIB)) {
            Set<String> finalPaths = new HashSet<String>();
            Set<String> urls = new HashSet(classPath.getNames());
            for (String url : urls) {
                int index = url.lastIndexOf("/");
                if (index >= 0 && index != url.length() - 1) {
                    finalPaths.add(WEB_INF_LIB + "/" + url.substring(index + 1));
                } else if (index != url.length() - 1) { // Only if it is a file not a directory
                    finalPaths.add(WEB_INF_LIB + "/" + url);
                }
            }

            return finalPaths;
        } else {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            Set<String> urls = new HashSet<String>();
            File f = new File(webDir, path);
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (File file : files) {
                    try {
                        urls.add(file.toURI().toURL().toExternalForm());
                    } catch (MalformedURLException e) {
                        // Ignore
                    }
                }
            }

            return urls;
        }
    }

    public URL getResource(String path) throws MalformedURLException {
        if (path.startsWith(WEB_INF_LIB)) {
            String jarFile = path.substring(WEB_INF_LIB.length());
            List<String> entries = classPath.getNames();
            for (String entry : entries) {
                if (entry.endsWith(jarFile)) {
                    return new File(entry).toURI().toURL();
                }
            }

            return null;
        } else {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            File f = new File(webDir, path);
            if (f.isFile()) {
                return f.toURI().toURL();
            }

            return null;
        }
    }

    public InputStream getResourceAsStream(String path) {
        try {
            URL url = getResource(path);
            if (url != null) {
                return url.openStream();
            }
        } catch (Exception e) {
        }

        return null;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    public RequestDispatcher getNamedDispatcher(String s) {
        return null;
    }

    public Servlet getServlet(String s) throws ServletException {
        return null;
    }

    public Enumeration getServlets() {
        return null;
    }

    public Enumeration getServletNames() {
        return null;
    }

    public void log(String s) {
    }

    public void log(Exception e, String s) {
    }

    public void log(String s, Throwable throwable) {
    }

    public String getRealPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        File f = new File(webDir, path);
        if (f.isFile()) {
            return f.getAbsolutePath();
        }

        return null;
    }

    public String getServerInfo() {
        return null;
    }

    public String getInitParameter(String s) {
        return null;
    }

    public Enumeration getInitParameterNames() {
        return null;
    }

    public String getServletContextName() {
        return null;
    }
}