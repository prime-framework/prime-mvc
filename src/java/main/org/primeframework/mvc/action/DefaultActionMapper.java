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
package org.primeframework.mvc.action;

import java.util.ArrayDeque;
import java.util.Deque;

import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This class is the default action mapper implementation.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionMapper implements ActionMapper {
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
  public ActionInvocation map(String uri, boolean executeResult) {
    // Handle extensions
    String extension = determineExtension(uri);
    if (extension != null) {
      uri = uri.substring(0, uri.length() - extension.length() - 1);
    }

    ActionConfiguration actionConfiguration = actionConfigurationProvider.lookup(uri);
    if (actionConfiguration == null) {
      // Try the index cases. If the URI is /foo/, look for an action config of /foo/index and
      // use it. If the uri is /foo, look for a config of /foo/index and then send a redirect
      // to /foo/
      if (uri.endsWith("/")) {
        actionConfiguration = actionConfigurationProvider.lookup(uri + "index");
      } else {
        actionConfiguration = actionConfigurationProvider.lookup(uri + "/index");
        if (actionConfiguration != null) {
          return new DefaultActionInvocation(null, uri + "/", null, actionConfiguration);
        }
      }
    }

    // Okay, no index handling was found and there isn't anything yet, let's search for it, but
    // only if it isn't an index like URI (i.e. not /admin/)
    Deque<String> uriParameters = new ArrayDeque<String>();
    if (actionConfiguration == null && !uri.endsWith("/")) {
      int index = uri.lastIndexOf('/');
      String localURI = uri;
      while (index > 0 && actionConfiguration == null) {
        // Add the restful parameter
        uriParameters.offerFirst(localURI.substring(index + 1));

        // Check if this matches
        localURI = localURI.substring(0, index);
        actionConfiguration = actionConfigurationProvider.lookup(localURI);
        if (actionConfiguration != null && !actionConfiguration.canHandle(uri)) {
          actionConfiguration = null;
        }

        if (actionConfiguration == null) {
          index = localURI.lastIndexOf('/');
        }
      }

      if (actionConfiguration == null) {
        uriParameters.clear();
      } else {
        uri = localURI;
      }
    }

    Object action = null;
    if (actionConfiguration != null) {
      action = injector.getInstance(actionConfiguration.actionClass());
    }

    return new DefaultActionInvocation(action, uri, extension, uriParameters, actionConfiguration, executeResult);
  }

  private String determineExtension(String uri) {
    String extension = null;
    int index = uri.lastIndexOf('.');
    if (index >= 0) {
      extension = uri.substring(index + 1);

      // Sanity check the extension to ensure it is NOT part of a version number like /foo-1.0
      boolean good = false;
      for (int i = 0; i < extension.length(); i++) {
        good = Character.isLetter(extension.charAt(i));
        if (!good) {
          break;
        }
      }

      if (!good) {
        extension = null;
      }
    }

    return extension;
  }
}