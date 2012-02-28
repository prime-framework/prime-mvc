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
package org.primeframework.test.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import static java.util.Arrays.*;
import net.java.util.IteratorEnumeration;
import org.primeframework.servlet.multipart.FileInfo;

/**
 * <p>
 * This class is a mock servlet request.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class MockHttpServletRequest implements HttpServletRequest {
    protected final Map<String, Object> attributes = new HashMap<String, Object>();
    protected final Map<String, List<String>> headers = new HashMap<String, List<String>>();
    protected final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    protected final Map<String, FileInfo> files = new HashMap<String, FileInfo>();
    protected final MockServletContext context;

    protected MockHttpSession session;
    protected String contentType = null;

    protected String uri;

    protected Vector<Locale> locales = new Vector<Locale>(asList(Locale.getDefault()));
    protected Method method;
    protected String encoding;
    protected String remoteAddr = "127.0.0.1";
    protected String remoteHost;
    protected int remotePort = 10000;
    protected String scheme = "HTTP";
    protected String serverName = "localhost";
    protected String localName = "localhost";

    protected int serverPort = 10000;
    protected ServletInputStream inputStream;
    protected BufferedReader reader;
    protected boolean inputStreamRetrieved;
    protected boolean readerRetrieved;

    protected MockRequestDispatcher dispatcher;
    protected String contextPath = "";
    protected List<Cookie> cookies = new ArrayList<Cookie>();
    protected String pathInfo = "";
    protected String pathTranslated;
    protected String remoteUser;
    protected String servletPath = "";

    public MockHttpServletRequest(String uri, MockServletContext context) {
        this.uri = uri;
        this.context = context;
    }

    public MockHttpServletRequest(String uri, MockHttpSession session) {
        this.uri = uri;
        this.session = session;
        this.context = session.context;
    }

    public MockHttpServletRequest(String uri, Locale locale, boolean post, String encoding,
            MockServletContext context) {
        this.uri = uri;
        this.locales.add(locale);
        this.method = post ? Method.POST : Method.GET;
        this.encoding = encoding;
        this.context = context;
        this.session = new MockHttpSession(context);

        if (post) {
            contentType = "application/x-www-form-urlencoded";
        }
    }

    public MockHttpServletRequest(String uri, Locale locale, boolean post, String encoding,
            MockHttpSession session) {
        this.uri = uri;
        this.locales.add(locale);
        this.method = post ? Method.POST : Method.GET;
        this.encoding = encoding;
        this.session = session;
        this.context = session.context;

        if (post) {
            contentType = "application/x-www-form-urlencoded";
        }
    }

    public MockHttpServletRequest(Map<String, List<String>> parameters, String uri, String encoding,
            Locale locale, boolean post, MockHttpSession session) {
        this.parameters.putAll(parameters);
        this.uri = uri;
        this.encoding = encoding;
        this.locales.add(locale);
        this.method = post ? Method.POST : Method.GET;
        this.session = session;
        this.context = session.context;

        if (post) {
            contentType = "application/x-www-form-urlencoded";
        }
    }

    public MockHttpServletRequest(Map<String, List<String>> parameters, String uri, String encoding,
            Locale locale, boolean post, MockServletContext context) {
        this.parameters.putAll(parameters);
        this.uri = uri;
        this.encoding = encoding;
        this.locales.add(locale);
        this.method = post ? Method.POST : Method.GET;
        this.context = context;
        this.session = new MockHttpSession(context);

        if (post) {
            contentType = "application/x-www-form-urlencoded";
        }
    }


    //-------------------------------------------------------------------------
    //  javax.servlet.ServletRequest methods
    //-------------------------------------------------------------------------

    /**
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * The attribute names.
     */
    public Enumeration getAttributeNames() {
        return new IteratorEnumeration(attributes.keySet().iterator());
    }

    /**
     * Returns the encoding which defaults to null unless it is set
     */
    public String getCharacterEncoding() {
        return encoding;
    }

    /**
     * This should set a new character encoding
     */
    public void setCharacterEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return  If the input stream or the reader are setup, this will return the length of those using
     *          the available method.
     */
    public int getContentLength() {
        if (inputStream != null) {
            try {
                return inputStream.available();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return -1;
    }

    /**
     * @return  The content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return  The input stream.
     * @throws  IOException If the reader was already retrieved.
     */
    public ServletInputStream getInputStream() throws IOException {
        if (readerRetrieved) {
            throw new IOException("Reader has already been retrieved.");
        }

        if (files.size() > 0 && inputStream == null) {
            inputStream = new MultipartInputStream(parameters, files);
        } else if (inputStream == null) {
            inputStream = new MockServletInputStream();
        }

        inputStreamRetrieved = true;
        return inputStream;
    }

    public String getLocalAddr() {
        return null;
    }

    /**
     * @return  The system default locale if nothing was added or setup in the constructor or using
     *          the setter methods. If there are multiple locales setup, this returns the first one.
     */
    public Locale getLocale() {
        if (locales.isEmpty()) {
            return Locale.getDefault();
        }

        return locales.get(0);
    }

    /**
     * @return  The request locales.
     */
    public Enumeration getLocales() {
        return locales.elements();
    }

    /**
     * @return  The local name.
     */
    public String getLocalName() {
        return localName;
    }

    public int getLocalPort() {
        return 0;
    }

    /**
     * @return  The parameter or null.
     */
    public String getParameter(String name) {
        if (!hasParameters()) {
            return null;
        }

        List<String> list = parameters.get(name);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public Map getParameterMap() {
        if (!hasParameters()) {
            return Collections.emptyMap();
        }

        Map<String, String[]> params = new HashMap<String, String[]>();
        for (String key : parameters.keySet()) {
            params.put(key, parameters.get(key).toArray(new String[parameters.get(key).size()]));
        }

        return params;
    }

    public Enumeration getParameterNames() {
        if (!hasParameters()) {
            return new IteratorEnumeration(Collections.<Object>emptyList().iterator());
        }

        return new IteratorEnumeration(parameters.keySet().iterator());
    }

    public String[] getParameterValues(String name) {
        if (!hasParameters()) {
            return null;
        }

        List<String> list = parameters.get(name);
        if (list != null) {
            return list.toArray(new String[list.size()]);
        }

        return null;
    }

    /**
     * @return  Always HTTP/1.0
     */
    public String getProtocol() {
        return "HTTP/1.0";
    }

    /**
     * @return  The reader.
     * @throws  IOException If the input stream was already retrieved.
     */
    public BufferedReader getReader() throws IOException {
        if (inputStreamRetrieved) {
            throw new IOException("InputStream already retrieved.");
        }

        if (inputStream == null) {
            inputStream = new MockServletInputStream();
        }

        if (reader == null) {
            if (encoding != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
            } else {
                reader = new BufferedReader(new InputStreamReader(inputStream));
            }
        }

        readerRetrieved = true;
        return reader;
    }

    /**
     * @deprecated
     */
    public String getRealPath(String url) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return  The remote address.
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * @return  The remote host.
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * @return  The remote port.
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * The mock request dispatcher for the given path.
     *
     * @param   thePath The path.
     * @return  The request dispatcher.
     */
    public RequestDispatcher getRequestDispatcher(String thePath) {
        if (thePath == null) {
            return null;
        }

        String fullPath;

        // The spec says that the path can be relative, in which case it will
        // be relative to the request. So for relative paths, we need to take
        // into account the simulated URL (ServletURL).
        if (thePath.startsWith("/")) {

            fullPath = thePath;

        } else {

            String pI = getPathInfo();
            if (pI == null) {
                fullPath = catPath(getServletPath(), thePath);
            } else {
                fullPath = catPath(getServletPath() + pI, thePath);
            }

            if (fullPath == null) {
                return null;
            }
        }

        dispatcher = new MockRequestDispatcher(fullPath);
        return dispatcher;
    }

    /**
     * @return  The scheme, which defaults to HTTP.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @return  The server name, which defaults to localhost.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @return  The server port, which defaults to 80.
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * @return  True if the scheme is HTTPS, false otherwise.
     */
    public boolean isSecure() {
        return scheme.equals("HTTPS");
    }

    /**
     * Removes the attribute with the name given.
     *
     * @param   name The name of the attribute.
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Sets the attribute given.
     *
     * @param   name The name of the attribute.
     * @param   value The attribute value.
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }


    //-------------------------------------------------------------------------
    //  javax.servlet.http.HttpServletRequest methods
    //-------------------------------------------------------------------------


    /**
     * Local clients don't authenticate
     */
    public String getAuthType() {
        return null;
    }

    /**
     * @return  The context path, which defaults to empty String.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * @return  Any cookies setup.
     */
    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    /**
     * @param   name The name of the header.
     * @return  The date header (if it exists), or -1.
     */
    public long getDateHeader(String name) {
        List<String> values = headers.get(name);
        if (values == null || values.size() == 0) {
            return -1;
        }

        return Long.parseLong(values.get(0));
    }

    /**
     * @param   name The name of the header.
     * @return  The header or null.
     */
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        if (values == null || values.size() == 0) {
            return null;
        }

        return values.get(0);
    }

    /**
     * @return  The header names.
     */
    public Enumeration getHeaderNames() {
        return new IteratorEnumeration(headers.keySet().iterator());
    }

    /**
     * @param   name The name of the headers.
     * @return  The headers, never null.
     */
    public Enumeration getHeaders(String name) {
        List<String> values = headers.get(name);
        if (values == null || values.size() == 0) {
            return new IteratorEnumeration(Collections.emptyList().iterator());
        }

       return new IteratorEnumeration(values.iterator());
    }

    /**
     * @param   name The name of the header.
     * @return  The header or -1.
     */
    public int getIntHeader(String name) {
        List<String> values = headers.get(name);
        if (values == null || values.size() == 0) {
            return -1;
        }

        return Integer.parseInt(values.get(0));
    }

    /**
     * @return  GET or POST, depending on the constructor or post flag setup.
     */
    public String getMethod() {
        return method.toString();
    }

    /**
     * @return  The path info.
     */
    public String getPathInfo() {
        return pathInfo;
    }

    /**
     * @return  The path translated.
     */
    public String getPathTranslated() {
        return pathTranslated;
    }

    /**
     * @return  The query string.
     */
    public String getQueryString() {
        StringBuilder build = new StringBuilder();
        for (String key : parameters.keySet()) {
            List<String> list = parameters.get(key);
            for (String value : list) {
                if (build.length() > 0) {
                    build.append("&");
                }

                try {
                    build.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return build.toString();
    }

    /**
     * @return  The remote user.
     */
    public String getRemoteUser() {
        return remoteUser;
    }

    /**
     * @return  Nothing, not implemented
     */
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return  The request URI.
     */
    public String getRequestURI() {
        return uri;
    }

    /**
     */
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return  The servlet path, which defaults to the empty string.
     */
    public String getServletPath() {
        return servletPath;
    }

    /**
     * @return  The session.
     */
    public HttpSession getSession() {
        if (session == null) {
            session = new MockHttpSession(context);
        }
        return session;
    }

    /**
     * @return  The session.
     */
    public HttpSession getSession(boolean create) {
        if (session == null && create) {
            session = new MockHttpSession(context);
        }

        return session;
    }

    /**
     * @return  Nothing, this isn't implemented.
     */
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return  Nothing, this isn't implemented.
     */
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return  Nothing, this isn't implemented.
     */
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return  Nothing, this isn't implemented.
     */
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return  Nothing, this isn't implemented.
     */
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    //                            Helper methods
    //-------------------------------------------------------------------------


    /**
     * Will concatenate 2 paths, normalising it. For example :
     * ( /a/b/c + d = /a/b/d, /a/b/c + ../d = /a/d ). Code borrowed from
     * Tomcat 3.2.2 !
     *
     * @param theLookupPath the first part of the path
     * @param thePath the part to add to the lookup path
     * @return the concatenated thePath or null if an error occurs
     */
    String catPath(String theLookupPath, String thePath) {
        // Cut off the last slash and everything beyond
        int index = theLookupPath.lastIndexOf("/");
        if (index == -1) {
            return thePath;
        }

        theLookupPath = theLookupPath.substring(0, index);

        // Deal with .. by chopping dirs off the lookup thePath
        while (thePath.startsWith("../")) {
            index = theLookupPath.lastIndexOf("/");
            if (theLookupPath.length() > 0) {
                theLookupPath = theLookupPath.substring(0, index);
            } else {
                // More ..'s than dirs, return null
                return null;
            }

            index = thePath.indexOf("../") + 3;
            thePath = thePath.substring(index);
        }

        return theLookupPath + "/" + thePath;
    }


    //-------------------------------------------------------------------------
    //                          Modification Methods
    //-------------------------------------------------------------------------

    /**
     * Adds a request locale.
     *
     * @param   locale The locale.
     */
    public void addLocale(Locale locale) {
        this.locales.add(locale);
    }

    /**
     * @return  The request locales vector (changes effect the internal Vector).
     */
    public Vector<Locale> getLocalesVector() {
        return locales;
    }

    /**
     * Clears all the request locales.
     */
    public void clearLocales() {
        locales.clear();
    }

    /**
     * Sets the input stream.
     *
     * @param   inputStream The input stream.
     */
    public void setInputStream(ServletInputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Sets the reader.
     *
     * @param   reader The reader.
     */
    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Sets the content type of the request.
     *
     * @param   contentType The new content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the remote address.
     *
     * @param   remoteAddr The remote address.
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    /**
     * Sets the remote host.
     *
     * @param   remoteHost The remote host.
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    /**
     * Sets the scheme, which defaults to HTTP.
     *
     * @param   scheme The scheme.
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the server name, which defaults to localhost.
     *
     * @param   serverName The server name.
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Allows a header to be added.
     *
     * @param   name The header name.
     * @param   value The header value.
     */
    public void addHeader(String name, String value) {
        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }

        values.add(value);
    }

    /**
     * Sets the request parameter with the given name to the given value
     *
     * @param   name The name of the parameter.
     * @param   value The value of the parameter.
     */
    public void setParameter(String name, String value) {
        List<String> list = parameters.get(name);
        if (list == null) {
            list = new ArrayList<String>();
            parameters.put(name, list);
        }

        list.add(value);
    }

    /**
     * Removes all the values of the request parameter with the given name
     *
     * @param   name The name of the parameter.
     */
    public void removeParameter(String name) {
        parameters.remove(name);
    }

    /**
     * Clears all the parameters
     */
    public void clearParameters() {
        parameters.clear();
    }

    /**
     * Sets the request parameter with the given name to the given values
     *
     * @param   name The name of the parameter.
     * @param   values The values of the parameter.
     */
    public void setParameters(String name, String... values) {
        parameters.put(name, asList(values));
    }

    /**
     * @return  The parameter map.
     */
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    /**
     * Clears all the attributes
     */
    public void clearAttributes() {
        attributes.clear();
    }

    /**
     * @return  The RequestDispatcher if one was created from this Request
     */
    public MockRequestDispatcher getRequestDispatcher() {
        return dispatcher;
    }

    /**
     * Modifies the request URI.
     *
     * @param   uri The request URI.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Sets the request to a POST method.
     *
     * @param   post True for a POST.
     */
    public void setPost(boolean post) {
        this.method = post ? Method.POST : Method.GET;

        if (post && contentType == null) {
            contentType = "application/x-www-form-urlencoded";
        }
    }

    /**
     * Sets the method of the request.
     *
     * @param   method The method.
     */
    public void setMethod(Method method) {
        this.method = method;
        if (method == Method.POST && contentType == null) {
            contentType = "application/x-www-form-urlencoded";
        }
    }

    /**
     * Modifies the encoding.
     *
     * @param   encoding The encoding.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets a new session.
     *
     * @param   session The new session.
     */
    public void setSession(MockHttpSession session) {
        this.session = session;
    }

    /**
     * Sets the remote port.
     *
     * @param   remotePort The remote port.
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * Sets the local name.
     *
     * @param   localName The local name.
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Sets the server port.
     *
     * @param   serverPort The server port.
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Sets the context path.
     *
     * @param   contextPath The context path.
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * @return  The list of cookies.
     */
    public List<Cookie> getCookiesList() {
        return cookies;
    }

    /**
     * Adds a cookie.
     *
     * @param   cookie The cookie.
     */
    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    /**
     * Clears all the cookies.
     */
    public void clearCookies() {
        this.cookies.clear();
    }

    /**
     * Sets the path info.
     *
     * @param   pathInfo The path info.
     */
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    /**
     * Sets the path translated.
     *
     * @param   pathTranslated The path translated.
     */
    public void setPathTranslated(String pathTranslated) {
        this.pathTranslated = pathTranslated;
    }

    /**
     * Sets the remote user.
     *
     * @param   remoteUser The remote user.
     */
    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    /**
     * Sets the servlet path.
     *
     * @param   servletPath The servlet path.
     */
    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    /**
     * Adds a file to the HTTP request body. This must be called if the content type is not set and the InputStream hasn't
     * been set or retrieved.
     *
     * @param   key The name of the form field.
     * @param   file The file to add.
     * @param   contentType The content type of the file.
     */
    public void addFile(String key, File file, String contentType) {
        if (contentType == null || file == null) {
            throw new IllegalArgumentException("The FileInfo must have a file and a contentType");
        }
        if (inputStreamRetrieved) {
            throw new IllegalStateException("InputStream retrieved already. Can't add a file to the HTTP request");
        }
        if (readerRetrieved) {
            throw new IllegalStateException("Reader retrieved already. Can't add a file to the HTTP request");
        }
        if (this.contentType != null) {
            throw new IllegalStateException("Content-Type set already. Can't add a file to the HTTP request");
        }

        this.contentType = "multipart/form-data, boundary=jcatapultmultipartuploadLKAlskld09309djoid";
        this.files.put(key, new FileInfo(file, key, contentType));
    }

    /**
     * Simulated how the container drains the InputStream when parameters are retrieved and the content-type is form
     * encoded.
     *
     * @return  True if the parameters are still good, false if there is an input stream to be used.
     */
    private boolean hasParameters() {
        if (method == Method.POST && contentType != null && contentType.equals("application/x-www-form-urlencoded")) {
            inputStream = new MockServletInputStream(new byte[0]);
            return true;
        }

        return files.isEmpty();
    }

    
    public static enum Method {
        GET,
        POST,
        PUT,
        HEAD,
        OPTIONS,
        DELETE,
        TRACE,
        CONNECT
    }
}
