/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Session container class that includes a hydrated user object
 *
 * @author Brady Wied
 */
class HydratedUserSessionContainer extends SerializedSessionContainer {
  public final Object user;

  public HydratedUserSessionContainer(SerializedSessionContainer serializedSessionContainer, Object user) {
    this(serializedSessionContainer.sessionId,
         serializedSessionContainer.loginInstant,
         user,
         serializedSessionContainer.userId);
  }

  public HydratedUserSessionContainer(String sessionId, ZonedDateTime loginInstant, Object user, UUID userId) {
    super(userId, sessionId, loginInstant);
    this.user = user;
  }
}
