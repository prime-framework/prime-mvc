/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Allows projects that use Prime MVC with the UserIDCookieSessionSecurityContext base class
 * to tie the HydratedSessionContainer and SerializedSessionContainer interfaces to implementations
 *
 * @author Brady Wied
 */
public interface UserIdSessionContextProvider {
  /**
   * Create a new Jackson serializable session container
   *
   * @param userId       user ID the session is for
   * @param loginInstant the instant the user logged in
   * @return a Jackson serializable container
   */
  UserIdSessionContext get(Object userId, ZonedDateTime loginInstant);
}
