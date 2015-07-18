/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * This result renders a FreeMarker template depending on the extension of the page.
 *
 * @author Brian Pontarelli
 */
public class ForwardResult extends AbstractResult<Forward> {
  private final ActionInvocationStore actionInvocationStore;
  private final MVCConfiguration configuration;
  private final FreeMarkerMap freeMarkerMap;
  private final FreeMarkerService freeMarkerService;
  private final ResourceLocator resourceLocator;
  private final HttpServletResponse response;

  @Inject
  public ForwardResult(ActionInvocationStore actionInvocationStore, ExpressionEvaluator expressionEvaluator,
                       ResourceLocator resourceLocator, FreeMarkerService freeMarkerService, HttpServletResponse response,
                       FreeMarkerMap freeMarkerMap, MVCConfiguration configuration) {
    super(expressionEvaluator);
    this.resourceLocator = resourceLocator;
    this.response = response;
    this.freeMarkerMap = freeMarkerMap;
    this.freeMarkerService = freeMarkerService;
    this.actionInvocationStore = actionInvocationStore;
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   */
  public void execute(Forward forward) throws IOException, ServletException {
    ActionInvocation current = actionInvocationStore.getCurrent();
    Object action = current.action;

    // Set the content type for the response
    String contentType = expand(forward.contentType(), action, false);
    response.setContentType(contentType);

    // Set the status code
    setStatus(forward.status(), forward.statusStr(), action, response);

    if (isHeadRequest(current)) {
      return;
    }

    String page = locateAndExpand(current, forward);
    if (page.startsWith("/")) {
      // If this URI hasn't already been fully qualified, do so now.  This may not be the best idea...
      if (!page.startsWith(configuration.resourceDirectory())) {
        page = configuration.resourceDirectory() + page;
      }
    } else {
      // Strip off the last part of the URI since it is relative
      String uri = current.actionURI;
      int index = uri.lastIndexOf("/");
      if (index >= 0) {
        uri = uri.substring(0, index);
      }
      page = configuration.resourceDirectory() + "/templates" + uri + "/" + page;
    }
    PrintWriter writer = response.getWriter();
    freeMarkerService.render(writer, page, freeMarkerMap);
  }


  private String locateAndExpand(ActionInvocation current, Forward forward) {
    // Determine if the default search should be used
    String page = forward.page();
    if (page.equals("")) {
      page = resourceLocator.locate(configuration.resourceDirectory() + "/templates");
      if (page == null) {
        throw new PrimeException("Unable to locate result for URI [" + current.uri() + "] and result code [" + forward.code() + "]");
      }
    }
    return expand(page, current.action, false);
  }

  public static class ForwardImpl implements Forward {
    private final String code;
    private final String contentType;
    private final int status;
    private final String statusStr;
    private final String uri;

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

    public Class<? extends Annotation> annotationType() {
      return Forward.class;
    }

    @Override
    public String code() {
      return code;
    }

    @Override
    public String contentType() {
      return contentType;
    }

    @Override
    public String page() {
      return uri;
    }

    @Override
    public int status() {
      return status;
    }

    @Override
    public String statusStr() {
      return statusStr;
    }
  }
}
