/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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

/**
 * Manage retrieving the JWT from the HTTP request and ensuring we don't return the JWT (in the form a cookie for
 * example) on a response if it is invalid.
 *
 * @author Daniel DeGroff
 */
public interface JWTRequestAdapter {

  /**
   * Return the encoded JWT from the HTTP request.
   *
   * @return the encoded JWT or null if no JWT was found in the request.
   */
  String getEncodedJWT();

  /**
   * Call when the JWT Security Scheme identifies an invalid JWT has been provided in the request.
   *
   * @return the encoded JWT or null if one was not found.
   */
  String invalidateJWT();

  /**
   * @return true if the HTTP request contains a JWT.
   */
  boolean requestContainsJWT();
}
