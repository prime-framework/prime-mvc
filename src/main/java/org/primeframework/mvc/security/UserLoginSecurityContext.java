/*
 * Copyright (c) 2015-2022, Inversoft Inc., All Rights Reserved
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

import java.util.Set;

/**
 * Security context interface that must be implemented by applications wishing to use the Prime MVC security handling.
 *
 * @author Brian Pontarelli
 */
public interface UserLoginSecurityContext {
  /**
   * @return The currently logged-in user or null.
   */
  Object getCurrentUser();

  /**
   * Loads the roles for the current user.
   *
   * @return The roles for the current user or an empty set if the user isn't logged in or has no roles.
   */
  Set<String> getCurrentUsersRoles();

  /**
   * @return the current sessionId or null if the user is not logged in.
   */
  String getSessionId();

  /**
   * Determines if the user is currently logged in.
   *
   * @return True if the user is logged in, false otherwise.
   */
  boolean isLoggedIn();

  /**
   * Logs the current user (via the context) so that future requests within the same session will be logged in.
   *
   * @param context The context object. This could be the user, a session object, various tokens, etc.
   */
  void login(Object context);

  /**
   * Logs the user out. The user's session will be invalidated.
   */
  void logout();

  /**
   * Logs the user out. The user's session will be invalidated.
   *
   * @param context an implementation specific context
   */
  default void logout(Object context) {
    logout();
  }

  /**
   * Allows implementations to update the user object, which might be different from the context object.
   *
   * @param user The user.
   */
  void updateUser(Object user);
}
