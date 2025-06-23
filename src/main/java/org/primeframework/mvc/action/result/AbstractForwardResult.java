/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.lang.annotation.Annotation;

import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This result renders a FreeMarker template depending on the extension of the page.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractForwardResult<U extends Annotation> extends AbstractResult<U> {
  private final ActionInvocationStore actionInvocationStore;

  private final MVCConfiguration configuration;

  private final FreeMarkerMap freeMarkerMap;

  private final FreeMarkerService freeMarkerService;

  private final ResourceLocator resourceLocator;

  private final HTTPResponse response;

  protected AbstractForwardResult(ActionInvocationStore actionInvocationStore, ExpressionEvaluator expressionEvaluator,
                                  ResourceLocator resourceLocator, FreeMarkerService freeMarkerService,
                                  HTTPResponse response, FreeMarkerMap freeMarkerMap, MVCConfiguration configuration) {
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
  public boolean execute(U forward) throws IOException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;

    String page;
    if (action == null) {
      // No action, no template. Return false to allow the workflow chain to continue
      page = resourceLocator.locate(configuration.templateDirectory());
      if (page == null) {
        return false;
      }
    }

    // Set the content type for the response
    String contentType = expand(getContentType(forward), action, false);
    response.setContentType(contentType);

    // Set the status code
    setStatus(getStatus(forward), getStatusStr(forward), action, response);

    // Handle setting cache controls
    addCacheControlHeader(forward, response);

    if (isHeadRequest(actionInvocation)) {
      return true;
    }

    // Locate the page and render the freemarker
    page = buildFullyQualifiedPath(actionInvocation, forward);
    freeMarkerService.render(response.getWriter(), page, freeMarkerMap);

    return true;
  }

  protected String expandPage(String page, ActionInvocation actionInvocation) {
    return expand(page, actionInvocation.action, false);
  }

  protected abstract String getCode(U forward);

  protected abstract String getContentType(U forward);

  protected abstract String getPage(U forward);

  protected abstract int getStatus(U forward);

  protected abstract String getStatusStr(U forward);

  /**
   * Return a String representation of the absolute path in the container to the FreeMarker template.
   *
   * @param actionInvocation The current action invocation.
   * @param forward          The annotation.
   * @return The fully qualified path to the FTL file.
   */
  private String buildFullyQualifiedPath(ActionInvocation actionInvocation, U forward) {
    String page = getPage(forward);
    if (page.equals("")) {
      page = locateDefault(actionInvocation, forward);
    } else if (page.startsWith("/")) {
      // Adjust absolute path to be relative to the configuration resource directory
      page = configuration.templateDirectory() + page;
    } else {
      // Strip off the last part of the URI since it is relative
      String uri = actionInvocation.actionURI;
      int index = uri.lastIndexOf('/');
      if (index >= 0) {
        uri = uri.substring(0, index);
      }
      page = configuration.templateDirectory() + uri + "/" + page;
    }

    return expandPage(page, actionInvocation);
  }

  /**
   * Locate the default template if one was not specified. Checks for results using this search order:
   * <p>
   * <ol>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;-&lt;resultCode&gt;.ftl</li>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;.ftl</li>
   * <li>${configuration.resourceDirectory}/templates/&lt;uri&gt;/index.ftl</li>
   * </ol>
   * <p>
   * If nothing is found this bombs out.
   *
   * @param actionInvocation The current action invocation.
   * @param forward          The annotation.
   * @return The default page.
   */
  private String locateDefault(ActionInvocation actionInvocation, U forward) {
    String page = resourceLocator.locate(configuration.templateDirectory());
    if (page == null) {
      throw new PrimeException("Missing result for action class [" + actionInvocation.configuration.actionClass + "] URI [" +
                               actionInvocation.uri() + "] and result code [" + getCode(forward) + "]");
    }
    return page;
  }
}
