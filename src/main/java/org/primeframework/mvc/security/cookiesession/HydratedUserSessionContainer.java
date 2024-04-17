/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;

/**
 * Session container class that includes a hydrated user object
 *
 * @author Brady Wied
 */
class HydratedUserSessionContainer extends SerializedSessionContainer {
  public final IdentifiableUser user;

  public HydratedUserSessionContainer(SerializedSessionContainer serializedSessionContainer, IdentifiableUser user) {
    this(serializedSessionContainer.sessionId,
         serializedSessionContainer.loginInstant,
         user);
  }

  public HydratedUserSessionContainer(String sessionId, ZonedDateTime loginInstant, IdentifiableUser user) {
    super(user.getId(), sessionId, loginInstant);
    this.user = user;
  }
}
