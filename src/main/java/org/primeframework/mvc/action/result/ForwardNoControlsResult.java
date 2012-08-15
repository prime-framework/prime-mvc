/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.ForwardNoControls;
import org.primeframework.mvc.freemarker.ControllessFreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * This result renders a FreeMarker template depending on the extension of the page.
 *
 * @author Brian Pontarelli
 */
public class ForwardNoControlsResult extends AbstractResult<ForwardNoControls> {
  public static final String DIR = "/WEB-INF/templates";
  private final ActionInvocationStore actionInvocationStore;
  private final ResourceLocator resourceLocator;
  private final FreeMarkerService freeMarkerService;
  private final HttpServletResponse response;
  private final ControllessFreeMarkerMap freeMarkerMap;

  @Inject
  public ForwardNoControlsResult(ActionInvocationStore actionInvocationStore, ExpressionEvaluator expressionEvaluator,
                                 ResourceLocator resourceLocator, FreeMarkerService freeMarkerService, HttpServletResponse response,
                                 ControllessFreeMarkerMap freeMarkerMap) {
    super(expressionEvaluator);
    this.resourceLocator = resourceLocator;
    this.response = response;
    this.freeMarkerMap = freeMarkerMap;
    this.freeMarkerService = freeMarkerService;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * {@inheritDoc}
   */
  public void execute(ForwardNoControls forward) throws IOException, ServletException {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
    Object action = invocation.action;

    // Set the content type for the response
    String contentType = expand(forward.contentType(), action, false);
    response.setContentType(contentType);

    // Set the status code
    setStatus(forward.status(), forward.statusStr(), action, response);

    // Determine if the default search should be used
    String page = forward.page();
    String code = forward.code();
    if (page.equals("")) {
      page = resourceLocator.locate(DIR);
    }

    if (page == null) {
      throw new PrimeException("Unable to locate result for URI [" + invocation.uri() + "] and result code [" + code + "]");
    }

    page = expand(page, action, false);
    if (!page.startsWith("/")) {
      // Strip off the last part of the URI since it is relative
      String uri = invocation.actionURI;
      int index = uri.lastIndexOf("/");
      if (index >= 0) {
        uri = uri.substring(0, index);
      }

      page = DIR + uri + "/" + page;
    }

    PrintWriter writer = response.getWriter();
    freeMarkerService.render(writer, page, freeMarkerMap);
  }

  public static class ForwardNoControlsImpl implements ForwardNoControls {
    private final String uri;
    private final String code;
    private final String contentType;
    private final int status;
    private final String statusStr;

    public ForwardNoControlsImpl(String uri, String code) {
      this.uri = uri;
      this.code = code;
      this.contentType = "text/html; charset=UTF-8";
      this.status = 200;
      this.statusStr = "";
    }

    public ForwardNoControlsImpl(String uri, String code, String contentType, int status) {
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
