/*
 * Copyright (c) 2017-2018, Inversoft Inc., All Rights Reserved
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

import io.fusionauth.jwt.domain.JWT;

/**
 * @author Daniel DeGroff
 */
public interface JWTConstraintsValidator {

  /**
   * Validate constraints.
   *
   * @param jwt         the decoded JWT payload.
   * @param constraints the action constraints.
   * @return true if the constraints are valid.
   */
  boolean validate(JWT jwt, String[] constraints);
}
