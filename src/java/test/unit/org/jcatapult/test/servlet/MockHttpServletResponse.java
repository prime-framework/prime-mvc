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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * This class is a mock servlet response.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class MockHttpServletResponse implements HttpServletResponse {
    protected List<Cookie> cookies = new ArrayList<Cookie>();
    protected MockServletOutputStream stream = new MockServletOutputStream();
    protected Map<String, Object> headers = new HashMap<String, Object>();
    protected int code;
    protected String message;
    protected String redirect;
    protected String encoding;
    protected String contentType;
    protected int length;
    protected int size;
    protected boolean reset;
    protected boolean flushed;
    protected Locale locale;
    protected boolean committed;

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public String encodeURL(String s) {
        throw new UnsupportedOperationException("Not used in this MVC");
    }

    public String encodeRedirectURL(String s) {
        throw new UnsupportedOperationException("Not used in this MVC");
    }

    public String encodeUrl(String s) {
        throw new UnsupportedOperationException("Not used in this MVC");
    }

    public String encodeRedirectUrl(String s) {
        throw new UnsupportedOperationException("Not used in this MVC");
    }

    public void sendError(int code, String message) throws IOException {
        this.code = code;
        this.message = message;
    }

    public void sendError(int code) throws IOException {
        this.code = code;
    }

    public void sendRedirect(String url) throws IOException {
        this.redirect = url;
    }

    public void setDateHeader(String name, long date) {
        headers.put(name, date);
    }

    public void addDateHeader(String name, long date) {
        headers.put(name, date);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setIntHeader(String name, int value) {
        headers.put(name, value);
    }

    public void addIntHeader(String name, int value) {
        headers.put(name, value);
    }

    public void setStatus(int code) {
        this.code = code;
    }

    public void setStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCharacterEncoding() {
        return encoding;
    }

    public String getContentType() {
        return contentType;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return stream;
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(stream);
    }

    public void setCharacterEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setContentLength(int length) {
        this.length = length;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setBufferSize(int size) {
        this.size = size;
    }

    public int getBufferSize() {
        return size;
    }

    public void flushBuffer() throws IOException {
        this.flushed = true;
    }

    public void resetBuffer() {
        this.reset = true;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    public void reset() {
        this.reset = true;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public MockServletOutputStream getStream() {
        return stream;
    }

    public void setStream(MockServletOutputStream stream) {
        this.stream = stream;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public boolean isFlushed() {
        return flushed;
    }

    public void setFlushed(boolean flushed) {
        this.flushed = flushed;
    }
}