/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Something that can get a cookie...nom nom nom...
 *
 * @author Daniel DeGroff
 */
public interface CookieConfig {
  /**
   * Add a cookie to the HTTP response
   *
   * @param request  the HTTP request
   * @param response the HTTP response
   * @param value    the value of the cookie
   */
  void add(HttpServletRequest request, HttpServletResponse response, String value);

  /**
   * Delete a cookie
   *
   * @param request  the HTTP request
   * @param response the HTTP response
   */
  void delete(HttpServletRequest request, HttpServletResponse response);

  /**
   * Get me a cookie please.
   *
   * @param request the HTTP servlet request
   * @return the cookie value or null.
   */
  String get(HttpServletRequest request);

  /**
   * @return the name of the cookie that this config can 'get'.
   */
  String name();
}
