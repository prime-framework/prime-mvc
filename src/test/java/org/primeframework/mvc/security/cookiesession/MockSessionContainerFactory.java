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
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;
import java.util.UUID;

public class MockSessionContainerFactory implements SessionContainerFactory {
  @Override
  public HydratedSessionContainer createHydrated(SerializedSessionContainer serializedSessionContainer, Object user) {
    return new MockHydratedSessionContainer(serializedSessionContainer.userId(), serializedSessionContainer.sessionId(), serializedSessionContainer.loginInstant(), user);
  }

  @Override
  public SerializedSessionContainer create(UUID userId, String sessionId, ZonedDateTime loginInstant) {
    return new MockSerializedSessionContainer(userId, sessionId, loginInstant);
  }
}
