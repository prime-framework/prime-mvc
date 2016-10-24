/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security.saved;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.util.IteratorEnumeration;

/**
 * This class allows the Saved Request handling to mock out a previous requests parameters.
 *
 * @author Brian Pontarelli
 */
public class SavedRequestHttpServletRequest extends HttpServletRequestWrapper {
  private final SavedHttpRequest savedRequest;

  /**
   * Constructs a new request facade.
   *
   * @param request      The request to wrap.
   * @param savedRequest The saved request.
   */
  public SavedRequestHttpServletRequest(HttpServletRequest request, SavedHttpRequest savedRequest) {
    super(request);
    this.savedRequest = savedRequest;
  }

  @Override
  public String getMethod() {
    return savedRequest.method.toString();
  }

  @Override
  public String getParameter(String key) {
    if (savedRequest.parameters != null && savedRequest.parameters.containsKey(key) && savedRequest.parameters.get(key) != null) {
      return savedRequest.parameters.get(key)[0];
    }

    return super.getParameter(key);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> complete = new HashMap<>();
    if (savedRequest.parameters != null) {
      complete.putAll(savedRequest.parameters);
    }

    complete.putAll(super.getParameterMap());

    return complete;
  }

  @Override
  public Enumeration<String> getParameterNames() {
    Set<String> names = new HashSet<>();
    if (savedRequest.parameters != null) {
      names.addAll(savedRequest.parameters.keySet());
    }

    names.addAll(super.getParameterMap().keySet());

    return new IteratorEnumeration<>(names.iterator());
  }

  @Override
  public String[] getParameterValues(String key) {
    if (savedRequest.parameters != null && savedRequest.parameters.containsKey(key) && savedRequest.parameters.get(key) != null) {
      return savedRequest.parameters.get(key);
    }

    return super.getParameterValues(key);
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String uri) {
    if (uri == null) {
      return super.getRequestDispatcher(null);
    }

    HttpServletRequest httpRequest = (HttpServletRequest) super.getRequest();
    RequestDispatcher rd = httpRequest.getRequestDispatcher(uri);
    return new FacadeRequestDispatcher(rd, httpRequest);
  }

  @Override
  public String getRequestURI() {
    return super.getRequestURI();
  }

  @Override
  public String getServletPath() {
    return super.getServletPath();
  }

  public static class FacadeRequestDispatcher implements RequestDispatcher {
    private final HttpServletRequest httpRequest;

    private final RequestDispatcher requestDispatcher;

    public FacadeRequestDispatcher(RequestDispatcher requestDispatcher, HttpServletRequest httpRequest) {
      this.requestDispatcher = requestDispatcher;
      this.httpRequest = httpRequest;
    }

    public void forward(ServletRequest servletRequest, ServletResponse servletResponse)
        throws ServletException, IOException {
      requestDispatcher.forward(httpRequest, servletResponse);
    }

    public void include(ServletRequest servletRequest, ServletResponse servletResponse)
        throws ServletException, IOException {
      requestDispatcher.include(httpRequest, servletResponse);
    }
  }
}