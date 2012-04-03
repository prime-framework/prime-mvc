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

import javax.servlet.ServletContext;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.util.ResourceTools;

import com.google.inject.Inject;

/**
 * Uses a lookup methodology for finding resources based on the action URI, extension, and result code.
 *
 * @author Brian Pontarelli
 */
public class DefaultResourceLocator implements ResourceLocator {
  private final ActionInvocationStore actionInvocationStore;
  private final ResultStore resultStore;
  private final ServletContext servletContext;

  @Inject
  public DefaultResourceLocator(ActionInvocationStore actionInvocationStore, ResultStore resultStore, ServletContext servletContext) {
    this.servletContext = servletContext;
    this.resultStore = resultStore;
    this.actionInvocationStore = actionInvocationStore;
  }

  @Override
  public String locate(String directory) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    String actionURI = actionInvocation.actionURI;
    String extension = actionInvocation.extension;
    String resultCode = resultStore.get();

    String resource = null;
    if (actionURI.endsWith("/")) {
      resource = ResourceTools.findResource(servletContext, directory + actionURI + "index.ftl");
    } else {
      if (extension != null) {
        if (resultCode != null) {
          resource = ResourceTools.findResource(servletContext, directory + actionURI + "-" + extension + "-" + resultCode + ".ftl");
        }
        if (resource == null) {
          resource = ResourceTools.findResource(servletContext, directory + actionURI + "-" + extension + ".ftl");
        }
      }

      // Look for FTL results to forward to
      if (resource == null && resultCode != null) {
        resource = ResourceTools.findResource(servletContext, directory + actionURI + "-" + resultCode + ".ftl");
      }
      if (resource == null) {
        resource = ResourceTools.findResource(servletContext, directory + actionURI + ".ftl");
      }
    }

    return resource;
  }

  @Override
  public String locateIndex(String directory) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    String actionURI = actionInvocation.actionURI;
    String uri = ResourceTools.findResource(servletContext, directory + actionURI + "/index.ftl");

    // Return the redirect portion of the URI
    if (uri != null) {
      uri = actionURI + "/";
    }

    return uri;
  }
}
