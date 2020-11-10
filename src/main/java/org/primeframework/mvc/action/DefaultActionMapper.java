/*
 * Copyright (c) 2001-2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the default action mapper implementation.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionMapper implements ActionMapper {
  private static final Logger logger = LoggerFactory.getLogger(DefaultActionMapper.class);

  private final ActionConfigurationProvider actionConfigurationProvider;

  private final Injector injector;

  @Inject
  public DefaultActionMapper(ActionConfigurationProvider actionConfigurationProvider, Injector injector) {
    this.actionConfigurationProvider = actionConfigurationProvider;
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  public ActionInvocation map(HTTPMethod httpMethod, String uri, boolean executeResult) {
    ActionInvocation invocation = actionConfigurationProvider.lookup(uri);
    if (invocation.configuration == null && !uri.endsWith("/")) {
      // Do an index check but if it doesn't return a valid invocation, then return the original one from above.
      ActionInvocation indexInvocation = actionConfigurationProvider.lookup(uri + "/index");
      if (indexInvocation.configuration != null) {
        indexInvocation.actionURI = indexInvocation.actionURI.substring(0, indexInvocation.actionURI.length() - 5); // Strip index but leave the slash
        indexInvocation.redirect = true;
        return indexInvocation;
      }
    } else if (invocation.configuration != null) {
      // Timer if debug is enabled
      long start = System.currentTimeMillis();

      // Create the action and find the method
      invocation.action = injector.getInstance(invocation.configuration.actionClass);

      // Spit out the timer
      if (logger.isDebugEnabled()) {
        logger.debug("Injection took [{}] for [{}]", (System.currentTimeMillis() - start), invocation.actionURI);
      }

      invocation.method = invocation.configuration.executeMethods.get(httpMethod);
    }

    // If we have a double slash, redirect after normalizing the URI and then let the chips fall where they may.
    // - We may or may not have an action invocation, so we don't care actually if the normalizedInvocation.configuration is null or not.
    if (uri.contains("//")) {
      String normalized = uri.replace("//", "/");
      ActionInvocation normalizedInvocation = actionConfigurationProvider.lookup(normalized);
      normalizedInvocation.redirect = true;
      return normalizedInvocation;
    }

    return invocation;
  }
}