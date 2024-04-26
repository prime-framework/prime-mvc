/*
 * Copyright (c) 2024-2024, Inversoft Inc., All Rights Reserved
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

/**
 * Session container serialized as JSON into the cookie
 *
 * @author Brady Wied
 */
public interface UserIdSessionContext<TUserId> {
  /**
   * The instant the user logged in
   *
   * @return instant user logged in
   */
  ZonedDateTime getLoginInstant();

  /**
   * When a login occurs, the BaseUserIdCookieSecurityContext will set the moment of login on the object
   *
   * @param loginInstant login time
   */
  void setLoginInstant(ZonedDateTime loginInstant);

  /**
   * The session ID for the logged in user. This should be unique.
   *
   * @return the session ID
   */
  String getSessionId();

  /**
   * The User ID for the logged in user
   *
   * @return the user ID
   */
  TUserId getUserId();

  /**
   * When a login occurs, the BaseUserIdCookieSecurityContext will set the user ID on the object
   *
   * @param userId user ID
   */
  void setUserId(TUserId userId);
}
