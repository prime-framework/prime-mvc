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

import java.util.Set;

/**
 * Security context interface that must be implemented by applications wishing to use the Prime MVC security handling.
 *
 * @author Brian Pontarelli
 */
public interface UserLoginSecurityContext {
  /**
   * @return The currently logged in user or null.
   */
  Object getCurrentUser();

  /**
   * Loads the roles for the current user.
   *
   * @return The roles for the current user or an empty set if the user isn't logged in or has no roles.
   */
  Set<String> getCurrentUsersRoles();

  /**
   * Determines if the user is currently logged in.
   *
   * @return True if the user is logged in, false otherwise.
   */
  boolean isLoggedIn();

  /**
   * Logs the given user in so that future requests within the same session will be logged in.
   *
   * @param user The user object.
   */
  void login(Object user);

  /**
   * Logs the user out. The user's session will be invalidated.
   */
  void logout();
}
