/*
 * Copyright (c) 2012-2015, Inversoft Inc., All Rights Reserved
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

import java.util.Map;
import java.util.WeakHashMap;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPContext;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;

/**
 * Uses a lookup methodology for finding resources based on the action URI, extension, and result code.
 *
 * @author Brian Pontarelli
 */
public class DefaultResourceLocator implements ResourceLocator {
  private static final Map<String, String> resourceCache = new WeakHashMap<>();

  private final ActionInvocationStore actionInvocationStore;

  private final HTTPContext context;

  private final ResultStore resultStore;

  @Inject
  public DefaultResourceLocator(ActionInvocationStore actionInvocationStore, ResultStore resultStore,
                                HTTPContext context) {
    this.context = context;
    this.resultStore = resultStore;
    this.actionInvocationStore = actionInvocationStore;
  }

  @Override
  public String locate(String directory) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    String actionURI = actionInvocation.actionURI;
    String extension = actionInvocation.extension;
    String resultCode = resultStore.get();
    String key = directory + actionURI + "-" + extension + "-" + resultCode;

    synchronized (resourceCache) {
      String resource = resourceCache.get(key);
      if (resource != null) {
        return resource;
      }

      if (actionURI.endsWith("/")) {
        resource = findResource(directory + actionURI + "index.ftl");
      } else {
        if (extension != null) {
          if (resultCode != null) {
            resource = findResource(directory + actionURI + "-" + extension + "-" + resultCode + ".ftl");
          }
          if (resource == null) {
            resource = findResource(directory + actionURI + "-" + extension + ".ftl");
          }
        }

        // Look for FTL results to forward to
        if (resource == null && resultCode != null) {
          resource = findResource(directory + actionURI + "-" + resultCode + ".ftl");
        }
        if (resource == null) {
          resource = findResource(directory + actionURI + ".ftl");
        }
      }

      if (resource != null) {
        resourceCache.put(key, resource);
      }

      return resource;
    }
  }

  @Override
  public String locateIndex(String directory) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    String actionURI = actionInvocation.actionURI;
    String key = directory + actionURI + "/index";
    synchronized (resourceCache) {
      String resource = resourceCache.get(key);
      if (resource != null) {
        return resource;
      }

      resource = findResource(directory + actionURI + "/index.ftl");

      // Return the redirect portion of the URI
      if (resource != null) {
        resource = actionURI + "/";
        resourceCache.put(key, resource);
      }

      return resource;
    }
  }

  private String findResource(String path) {
    return context.getResource(path) != null ? path : null;
  }
}
