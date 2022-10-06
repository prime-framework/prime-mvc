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

import io.fusionauth.http.HTTPMethod;

/**
 * This interface defines the method that maps URIs to actions.
 *
 * @author Brian Pontarelli
 */
public interface ActionMapper {
  /**
   * Maps the given URI to an action invocation.
   *
   * @param httpMethod    The HTTP method being invoked.
   * @param uri           The URI.
   * @param executeResult This flag is set into the ActionInvocation to control whether or not the result is executed or
   *                      not.
   * @return The action invocation and never null. This invocation might be a redirect for index handling.
   */
  ActionInvocation map(HTTPMethod httpMethod, String uri, boolean executeResult);
}