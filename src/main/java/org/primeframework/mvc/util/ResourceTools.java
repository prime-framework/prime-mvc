/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.util;

import javax.servlet.ServletContext;
import java.net.MalformedURLException;

/**
 * This class provides resource utility methods.
 *
 * @author Brian Pontarelli
 */
public class ResourceTools {
  /**
   * Locates a resource in the servlet context or the classloader for the given path.
   *
   * @param servletContext The servlet context.
   * @param path           The path.
   * @return The path or null if the resource doesn't exist.
   */
  public static String findResource(ServletContext servletContext, String path) {
    try {
      String classLoaderPath = path.substring(1);
      if (servletContext.getResource(path) != null ||
          Thread.currentThread().getContextClassLoader().getResource(classLoaderPath) != null) {
        return path;
      }
    } catch (MalformedURLException e) {
      // Ignore because we are searching and somethings might be bad URLs
    }

    return null;
  }
}
