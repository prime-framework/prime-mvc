/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

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
                       ResourceLocator resourceLocator, FreeMarkerService freeMarkerService,
                       HttpServletResponse response,
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
  public boolean execute(Forward forward) throws IOException {

    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;

    String page;
    if (action == null) {
      // No action, no template. Return false to allow the workflow chain to continue
      page = resourceLocator.locate(configuration.resourceDirectory() + "/templates");
      if (page == null) {
        return false;
      }
    }

    // Set the content type for the response
    String contentType = expand(forward.contentType(), action, false);
    response.setContentType(contentType);

    // Set the status code
    setStatus(forward.status(), forward.statusStr(), action, response);

    if (isHeadRequest(actionInvocation)) {
      return true;
    }

    // Locate the page and render the freemarker
    page = buildFullyQualifiedPath(actionInvocation, forward);
    freeMarkerService.render(response.getWriter(), page, freeMarkerMap);

    return true;
  }

  /**
   * Return a String representation of the absolute path in the container to the FreeMarker template.
   *
   * @param actionInvocation The current action invocation.
   * @param forward          The annotation.
   * @return The fully qualified path to the FTL file.
   */
  private String buildFullyQualifiedPath(ActionInvocation actionInvocation, Forward forward) {
    String page = forward.page();
    if (page.equals("")) {
      page = locateDefault(actionInvocation, forward);
    } else if (page.startsWith("/")) {
      // Adjust absolute path to be relative to the configuration resource directory
      page = configuration.resourceDirectory() + page;
    } else {
      // Strip off the last part of the URI since it is relative
      String uri = actionInvocation.actionURI;
      int index = uri.lastIndexOf("/");
      if (index >= 0) {
        uri = uri.substring(0, index);
      }
      page = configuration.resourceDirectory() + "/templates" + uri + "/" + page;
    }
    return expand(page, actionInvocation.action, false);
  }

  /**
   * Locate the default template if one was not specified. Checks for results using this search order:
   * <p>
   * <ol>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;-&lt;resultCode&gt;.jsp</li>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;-&lt;resultCode&gt;.ftl</li>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;.jsp</li>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;.ftl</li>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;/index.jsp</li>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;/index.ftl</li>
   * </ol>
   * <p>
   * If nothing is found this bombs out.
   *
   * @param actionInvocation The current action invocation.
   * @param forward          The annotation.
   * @return The default page.
   */
  private String locateDefault(ActionInvocation actionInvocation, Forward forward) {
    String page = resourceLocator.locate(configuration.resourceDirectory() + "/templates");
    if (page == null) {
      throw new PrimeException("Missing result for action class [" + actionInvocation.configuration.actionClass + "] URI [" +
          actionInvocation.uri() + "] and result code [" + forward.code() + "]");
    }
    return page;
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
