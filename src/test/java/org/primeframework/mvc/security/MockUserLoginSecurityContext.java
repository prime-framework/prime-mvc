/*
 * Copyright (c) 2016-2022, Inversoft Inc., All Rights Reserved
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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel DeGroff
 */
public class MockUserLoginSecurityContext implements UserLoginSecurityContext {
  public static Object currentUser;

  public static Set<String> roles = new HashSet<>();

  @Override
  public Object getCurrentUser() {
    return currentUser;
  }

  @Override
  public Set<String> getCurrentUsersRoles() {
    return roles;
  }

  @Override
  public String getSessionId() {
    return currentUser != null ? Integer.toString(currentUser.hashCode()) : null;
  }

  @Override
  public boolean isLoggedIn() {
    return currentUser != null;
  }

  @Override
  public void login(Object user) {
    currentUser = user;
  }

  @Override
  public void logout() {
    currentUser = null;
  }

  @Override
  public void updateUser(Object user) {
    currentUser = user;
  }
}
