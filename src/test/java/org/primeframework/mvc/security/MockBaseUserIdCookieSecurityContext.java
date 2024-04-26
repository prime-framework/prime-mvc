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

import java.time.Clock;
import java.time.Duration;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;

public class MockBaseUserIdCookieSecurityContext extends BaseUserIdCookieSecurityContext {
  @Inject
  protected MockBaseUserIdCookieSecurityContext(HTTPRequest request, HTTPResponse response, Encryptor encryptor, ObjectMapper objectMapper,
                                                Clock clock, UserIdSessionContextProvider userIdSessionContextProvider) {
    super(request, response, encryptor, objectMapper, clock, Duration.ofMinutes(5), Duration.ofMinutes(30), userIdSessionContextProvider);
  }

  public MockBaseUserIdCookieSecurityContext(HTTPRequest request, HTTPResponse response, Encryptor encryptor, ObjectMapper objectMapper,
                                             Clock clock,
                                             Duration sessionTimeout, Duration sessionMaxAge,
                                             UserIdSessionContextProvider userIdSessionContextProvider) {
    super(request, response, encryptor, objectMapper, clock, sessionTimeout, sessionMaxAge, userIdSessionContextProvider);
  }

  @Override
  protected Class<? extends UserIdSessionContext> getUserIdSessionContextClass() {
    return MockUserIdSessionContext.class;
  }

  @Override
  public Set<String> getCurrentUsersRoles() {
    return Set.of();
  }

  @Override
  protected MockUser retrieveUserById(Object id) {
    return new MockUser("bob");
  }

  @Override
  protected Object getIdFromUser(Object user) {
    if (!(user instanceof MockUser mockUser)) {
      throw new RuntimeException("Expected MockUser and got " + user.getClass());
    }
    return mockUser.id;
  }
}