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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Locale;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.util.ResourceTools;

import com.google.inject.Inject;

/**
 * This result performs a servlet forward to a JSP or renders a FreeMarker template depending on the extension of the
 * page.
 *
 * @author Brian Pontarelli
 */
public class ForwardResult extends AbstractResult<Forward> {
  public static final String DIR = "/WEB-INF/templates";
  private final ActionInvocationStore actionInvocationStore;
  private final FreeMarkerService freeMarkerService;
  private final ServletContext servletContext;
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final FreeMarkerMap freeMarkerMap;
  private final Locale locale;

  @Inject
  public ForwardResult(ActionInvocationStore actionInvocationStore, ExpressionEvaluator expressionEvaluator,
                       FreeMarkerService freeMarkerService, ServletContext servletContext, HttpServletRequest request,
                       HttpServletResponse response, FreeMarkerMap freeMarkerMap, Locale locale) {
    super(expressionEvaluator);
    this.locale = locale;
    this.servletContext = servletContext;
    this.request = request;
    this.response = response;
    this.freeMarkerMap = freeMarkerMap;
    this.freeMarkerService = freeMarkerService;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * {@inheritDoc}
   */
  public void execute(Forward forward) throws IOException, ServletException {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
    Object action = invocation.action();

    // Set the content type for the response
    String contentType = expand(forward.contentType(), action, false);
    response.setContentType(contentType);

    // Set the status code
    setStatus(forward.status(), forward.statusStr(), action, response);

    // Determine if the default search should be used
    String page = forward.page();
    String code = forward.code();
    if (page.equals("")) {
      page = findResource(invocation, code);
    }

    if (page == null) {
      throw new PrimeException("Unable to locate result for URI [" + invocation.uri() + "] and result code [" + code + "]");
    }

    page = expand(page, action, false);
    if (!page.startsWith("/")) {
      // Strip off the last part of the URI since it is relative
      String uri = invocation.actionURI();
      int index = uri.lastIndexOf("/");
      if (index >= 0) {
        uri = uri.substring(0, index);
      }

      page = DIR + uri + "/" + page;
    }

    if (page.endsWith(".jsp")) {
      RequestDispatcher requestDispatcher = request.getRequestDispatcher(page);
      requestDispatcher.forward(wrapRequest(invocation, request), response);
    } else if (page.endsWith(".ftl")) {
      PrintWriter writer = response.getWriter();
      freeMarkerService.render(writer, page, freeMarkerMap, locale);
    }
  }

  /**
   * Locates the default Forward for an action invocation and result code from an action.
   * <p/>
   * Checks for results using this search order:
   * <p/>
   * <ol>
   *   <li>/WEB-INF/templates/&lt;uri>-&lt;resultCode>.jsp</li>
   *   <li>/WEB-INF/templates/&lt;uri>-&lt;resultCode>.ftl</li>
   *   <li>/WEB-INF/templates/&lt;uri>.jsp</li>
   *   <li>/WEB-INF/templates/&lt;uri>.ftl</li>
   *   <li>/WEB-INF/templates/&lt;uri>/index.jsp</li>
   *   <li>/WEB-INF/templates/&lt;uri>/index.ftl</li>
   * </ol>
   * <p/>
   * If nothing is found this bombs out.
   *
   * @param invocation The action invocation.
   * @param resultCode The result code from the action invocation.
   * @return The Forward and never null.
   * @throws RuntimeException If the default forward could not be found.
   */
  protected Annotation defaultResult(ActionInvocation invocation, String resultCode) {
    String uri = findResource(invocation, resultCode);
    if (uri != null) {
      return new ForwardImpl(uri, resultCode);
    }

    return null;
  }

  /**
   * Determines if there is an index resource available for the given action invocation. For example, if the action URI
   * is:
   * <pre>
   * /foo
   * </pre>
   * And there is a resource at:
   * <pre>
   * /foo/index.ftl
   * </pre>
   * We can redirect the request to that resource.
   *
   * @param invocation The action invocation.
   * @return The redirect URI or null.
   */
  protected String redirectURI(ActionInvocation invocation) {
    String actionURI = invocation.actionURI();
    if (actionURI.endsWith("/")) {
      return null;
    }

    String uri = ResourceTools.findResource(servletContext, DIR + actionURI + "/index.jsp");
    if (uri == null) {
      uri = ResourceTools.findResource(servletContext, DIR + actionURI + "/index.ftl");
    }

    // Return the redirect portion of the URI
    if (uri != null) {
      uri = actionURI + "/";
    }

    return uri;
  }

  private String findResource(ActionInvocation invocation, String resultCode) {
    String actionURI = invocation.actionURI();
    String extension = invocation.extension();
    String resource = null;
    if (actionURI.endsWith("/")) {
      resource = ResourceTools.findResource(servletContext, DIR + actionURI + "index.jsp");
      if (resource == null) {
        resource = ResourceTools.findResource(servletContext, DIR + actionURI + "index.ftl");
      }
    } else {
      if (extension != null) {
        if (resultCode != null) {
          resource = ResourceTools.findResource(servletContext, DIR + actionURI + "-" + extension + "-" + resultCode + ".jsp");
        }
        if (resource == null && resultCode != null) {
          resource = ResourceTools.findResource(servletContext, DIR + actionURI + "-" + extension + "-" + resultCode + ".ftl");
        }
        if (resource == null) {
          resource = ResourceTools.findResource(servletContext, DIR + actionURI + "-" + extension + ".jsp");
        }
        if (resource == null) {
          resource = ResourceTools.findResource(servletContext, DIR + actionURI + "-" + extension + ".ftl");
        }
      }

      // Look for JSP and FTL results to forward to
      if (resource == null && resultCode != null) {
        resource = ResourceTools.findResource(servletContext, DIR + actionURI + "-" + resultCode + ".jsp");
      }
      if (resource == null && resultCode != null) {
        resource = ResourceTools.findResource(servletContext, DIR + actionURI + "-" + resultCode + ".ftl");
      }
      if (resource == null) {
        resource = ResourceTools.findResource(servletContext, DIR + actionURI + ".jsp");
      }
      if (resource == null) {
        resource = ResourceTools.findResource(servletContext, DIR + actionURI + ".ftl");
      }
    }

    return resource;
  }

  public static class ForwardImpl implements Forward {
    private final String uri;
    private final String code;
    private final String contentType;
    private final int status;
    private final String statusStr;

    public ForwardImpl(String uri, String code) {
      this.uri = uri;
      this.code = code;
      this.contentType = "text/html; charset=UTF-8";
      this.status = 200;
      this.statusStr = "";
    }

    public ForwardImpl(String uri, String code, String contentType, int status) {
      this.uri = uri;
      this.code = code;
      this.contentType = contentType;
      this.status = status;
      this.statusStr = "";
    }

    @Override
    public String code() {
      return code;
    }

    @Override
    public String page() {
      return uri;
    }

    @Override
    public String contentType() {
      return contentType;
    }

    @Override
    public int status() {
      return status;
    }

    @Override
    public String statusStr() {
      return statusStr;
    }

    public Class<? extends Annotation> annotationType() {
      return Forward.class;
    }
  }
}
