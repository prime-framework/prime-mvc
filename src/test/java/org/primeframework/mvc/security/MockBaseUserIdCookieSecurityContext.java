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
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;

public class MockBaseUserIdCookieSecurityContext extends BaseUserIdCookieSecurityContext<UUID> {
  public MockBaseUserIdCookieSecurityContext(HTTPRequest request, HTTPResponse response, Encryptor encryptor, ObjectMapper objectMapper,
                                             Clock clock,
                                             Duration sessionTimeout, Duration sessionMaxAge) {
    super(request, response, encryptor, objectMapper, clock, sessionTimeout, sessionMaxAge);
  }

  @Inject
  protected MockBaseUserIdCookieSecurityContext(HTTPRequest request, HTTPResponse response, Encryptor encryptor, ObjectMapper objectMapper,
                                                Clock clock) {
    super(request, response, encryptor, objectMapper, clock, Duration.ofMinutes(5), Duration.ofMinutes(30));
  }

  @Override
  public Set<String> getCurrentUsersRoles() {
    return Set.of();
  }

  @Override
  protected UserIdSessionContext<UUID> createUserIdSessionContext(UUID userId, ZonedDateTime loginInstant) {
    return new MockUserIdSessionContext(userId, loginInstant);
  }

  @Override
  protected UUID getIdFromUser(Object user) {
    if (!(user instanceof MockUser mockUser)) {
      throw new RuntimeException("Expected MockUser and got " + user.getClass());
    }
    return mockUser.id;
  }

  @Override
  protected Class<? extends UserIdSessionContext<UUID>> getUserIdSessionContextClass() {
    return MockUserIdSessionContext.class;
  }

  @Override
  protected MockUser retrieveUserById(UUID id) {
    return new MockUser("bob");
  }
}
