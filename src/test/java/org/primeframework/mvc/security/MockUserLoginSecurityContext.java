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

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.csrf.CSRFProvider;

/**
 * @author Daniel DeGroff
 */
public class MockUserLoginSecurityContext extends BaseHttpSessionUserLoginSecurityContext {
  public static Set<String> roles = new HashSet<>();

  @Inject
  public MockUserLoginSecurityContext(MVCConfiguration configuration, CSRFProvider csrfProvider,
                                      HttpServletRequest request) {
    super(configuration, csrfProvider, request);
  }

  @Override
  public Set<String> getCurrentUsersRoles() {
    return roles;
  }
}
