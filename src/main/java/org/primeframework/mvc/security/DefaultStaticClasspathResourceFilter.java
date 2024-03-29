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
 * The default filter behavior will block all requests. You will need to bind your own filter and explicitly allow some
 * or all requests based upon the URI.
 *
 * @author Daniel DeGroff
 */
public class DefaultStaticClasspathResourceFilter implements StaticClasspathResourceFilter {
  @Override
  public boolean allow(String uri, HTTPRequest request) {
    // Loading resources from the class path is disabled by default.
    return false;
  }
}
