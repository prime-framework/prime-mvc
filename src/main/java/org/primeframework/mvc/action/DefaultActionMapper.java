/*
 * Copyright (c) 2001-2016, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.URITools;

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
  public ActionInvocation map(HTTPMethod httpMethod, String uri, boolean executeResult) {
    // Handle extensions
    String extension = URITools.determineExtension(uri);
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
          return new ActionInvocation(null, null, uri + "/", null, actionConfiguration);
        }
      }
    }

    // Okay, no index handling was found and there isn't anything yet, let's search for it, but
    // only if it isn't an index like URI (i.e. not /admin/)
    Deque<String> uriParameters = new ArrayDeque<>();
    if (actionConfiguration == null && !uri.endsWith("/")) {
      int index = uri.lastIndexOf('/');
      String localURI = uri;
      while (index > 0 && actionConfiguration == null) {
        // Add the restful parameter
        uriParameters.offerFirst(localURI.substring(index + 1));

        // Check if this matches
        localURI = localURI.substring(0, index);
        actionConfiguration = actionConfigurationProvider.lookup(localURI);
        if (actionConfiguration != null && !canHandle(actionConfiguration, uri)) {
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

    // Create the action and find the method
    Object action = null;
    ExecuteMethodConfiguration method = null;
    if (actionConfiguration != null) {
      action = injector.getInstance(actionConfiguration.actionClass);
      // The action may be null if not implemented.
      method = actionConfiguration.executeMethods.get(httpMethod);
    }

    return new ActionInvocation(action, method, uri, extension, uriParameters, actionConfiguration, executeResult);
  }

  /**
   * Determines if the action configuration can handle the given URI. MVCConfiguration objects provide additional
   * handling for URI parameters and other cases and this method uses the full incoming URI to determine if the
   * configuration can handle it.
   *
   * @param actionConfiguration The action configuration to check.
   * @param uri                 The full incoming URI.
   * @return True if this configuration can handle the URI, false if not.
   */
  protected boolean canHandle(ActionConfiguration actionConfiguration, String uri) {
    // Check if the URIs are equal
    if (actionConfiguration.uri.equals(uri)) {
      return true;
    }

    // Verify that the full URI starts with this URI
    if (!uri.startsWith(actionConfiguration.uri)) {
      return false;
    }

    String[] uriParts = uri.substring(actionConfiguration.uri.length() + 1).split("/");
    for (int i = 0; i < uriParts.length; i++) {
      String uriPart = uriParts[i];

      // If there are no more pattern parts, bail
      if (i >= actionConfiguration.patternParts.length) {
        break;
      }

      if (actionConfiguration.patternParts[i].startsWith("{*")) {
        // Bad pattern
        if (!actionConfiguration.patternParts[i].endsWith("}")) {
          throw new PrimeException("Action annotation in class [" + actionConfiguration.actionClass +
              "] contains an invalid URI parameter pattern [" + actionConfiguration.pattern + "]. A curly " +
              "bracket is unclosed. If you want to include a curly brakcet that is not " +
              "a URI parameter capture, you need to escape it like \\{");
        }

        // Can't have wildcard capture in the middle
        if (i != actionConfiguration.patternParts.length - 1) {
          throw new PrimeException("Action annotation in class [" + actionConfiguration.actionClass +
              "] contains an invalid URI parameter pattern [" + actionConfiguration.pattern + "]. You cannot " +
              "have a wildcard capture (i.e. {*foo}) in the middle of the pattern. It must " +
              "be on the end of the pattern.");
        }

        break;
      } else if (actionConfiguration.patternParts[i].startsWith("{")) {
        if (!actionConfiguration.patternParts[i].endsWith("}")) {
          throw new PrimeException("Action annotation in class [" + actionConfiguration.actionClass +
              "] contains an invalid URI parameter pattern [" + actionConfiguration.pattern + "]. A curly " +
              "bracket is unclosed. If you want to include a curly brakcet that is not " +
              "a URI parameter capture, you need to escape it like \\{");
        }
      } else {
        String patternPart = normalize(actionConfiguration.patternParts[i]);
        if (!uriPart.equals(patternPart)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Replaces \{ with { and \} with }.
   *
   * @param pattern The pattern to normalize.
   * @return The normalized pattern.
   */
  protected String normalize(String pattern) {
    return pattern.replace("\\{", "{").replace("\\}", "}");
  }
}