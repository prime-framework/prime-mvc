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
 * A default version of the login security context that is not usable. This is helpful if the application has no login
 * or is in a state that login is not possible.
 *
 * @author Brian Pontarelli
 */
public class NoLoginSecurityContext implements UserLoginSecurityContext {
  @Override
  public Object getCurrentUser() {
    return null;
  }

  @Override
  public Set<String> getCurrentUsersRoles() {
    return Set.of();
  }

  @Override
  public String getSessionId() {
    return null;
  }

  @Override
  public boolean isLoggedIn() {
    return false;
  }

  @Override
  public void login(Object context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void logout() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateUser(Object user) {
    throw new UnsupportedOperationException();
  }
}
