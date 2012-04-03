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

import java.lang.reflect.Method;
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
  private final HTTPMethod httpMethod;

  @Inject
  public DefaultActionMapper(ActionConfigurationProvider actionConfigurationProvider, Injector injector, HTTPMethod httpMethod) {
    this.actionConfigurationProvider = actionConfigurationProvider;
    this.injector = injector;
    this.httpMethod = httpMethod;
  }

  /**
   * {@inheritDoc}
   */
  public ActionInvocation map(String uri, boolean executeResult) {
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
          return new DefaultActionInvocation(null, null, uri + "/", null, actionConfiguration);
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

    // Create the action and find the method
    Object action = null;
    Method method = null;
    if (actionConfiguration != null) {
      Class<?> actionClass = actionConfiguration.actionClass();
      if (extension != null) {
        try {
          method = actionClass.getMethod(extension);
        } catch (NoSuchMethodException e) {
          // Ignore
        }
      }

      if (method == null) {
        try {
          method = actionClass.getMethod(httpMethod.name().toLowerCase());
        } catch (NoSuchMethodException e) {
          // Ignore
        }
      }

      // Handle HEAD requests using a GET
      if (method == null && httpMethod == HTTPMethod.HEAD) {
        try {
          method = actionClass.getMethod("get");
        } catch (NoSuchMethodException e) {
          // Ignore
        }
      }

      if (method == null) {
        try {
          method = actionClass.getMethod("execute");
        } catch (NoSuchMethodException e) {
          // Ignore
        }
      }

      if (method == null) {
        throw new PrimeException("The action class [" + actionClass + "] is missing a valid execute method. The class " +
          "can define a method with the same names as the HTTP method (which is currently [" +
          httpMethod.name().toLowerCase() + "]) or it can define a default method named [execute].");
      }

      verify(method);

      action = injector.getInstance(actionClass);
    }

    return new DefaultActionInvocation(action, method, uri, extension, uriParameters, actionConfiguration, executeResult);
  }

  /**
   * Ensures that the method is a correct execute method.
   *
   * @param method The method.
   * @throws PrimeException If the method is invalid.
   */
  protected void verify(Method method) {
    if (method.getReturnType() != String.class || method.getParameterTypes().length != 0) {
      throw new PrimeException("The action class [" + method.getDeclaringClass().getClass() + "] has defined an " +
        "execute method named [" + method.getName() + "] that is invalid. Execute methods must have zero parameters " +
        "and return a String like this: [public String execute()].");
    }
  }
}