/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HTTPContext {
  public Map<String, Object> attributes = Collections.synchronizedMap(new HashMap<>());

  public Path baseDir;

  public HTTPContext(Path baseDir) {
    this.baseDir = baseDir;
  }

  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /**
   * Attempts to retrieve a file or classpath resource at the given path. If the path is invalid, this will return null.
   * If the classpath is borked or the path somehow cannot be converted to a URL, then this throws an exception.
   *
   * @param path The path.
   * @return The URL to the resource or null.
   * @throws IllegalStateException If the classpath is borked or the file system is jacked.
   */
  public URL getResource(String path) throws IllegalStateException {
    // Protect against absolute paths that break out of the context of the web app
    String filePath = path;
    if (path.startsWith("/")) {
      filePath = path.substring(1);
    }

    try {
      Path resolved = baseDir.resolve(filePath);
      if (Files.exists(resolved)) {
        return resolved.toUri().toURL();
      }

      return HTTPContext.class.getResource(path);
    } catch (MalformedURLException e) {
      // This is quite likely impossible but we don't really care since the resource was not obtainable. Therefore, we are
      // just rethrow an exception
      throw new IllegalStateException(e);
    }
  }

  /**
   * Locates the path given the webapps baseDir (passed into the constructor.
   *
   * @param appPath The app path to a resource (like an FTL file).
   * @return The resolved path, which is almost always just the baseDir plus the appPath with a file separator in the
   *     middle.
   */
  public Path resolve(String appPath) {
    if (appPath.startsWith("/")) {
      appPath = appPath.substring(1);
    }

    return baseDir.resolve(appPath);
  }

  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }
}
