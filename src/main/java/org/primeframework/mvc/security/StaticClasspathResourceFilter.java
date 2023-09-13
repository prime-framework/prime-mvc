/*
 * Copyright (c) 2023, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import io.fusionauth.http.server.HTTPRequest;

/**
 * A filter for static resource requests that may be resolved through the class path.
 *
 * @author Daniel DeGroff
 */
public interface StaticClasspathResourceFilter {
  /**
   * @param uri     the request URI
   * @param request the request
   * @return true if resolution should be attempted using class path resolution.
   */
  boolean allow(String uri, HTTPRequest request);
}
