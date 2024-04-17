/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;

public class HydratedUserSessionContainer extends SerializedSessionContainer {
  public final IdentifiableUser user;

  public HydratedUserSessionContainer(SerializedSessionContainer serializedSessionContainer, IdentifiableUser user) {
    this(serializedSessionContainer.sessionId,
         serializedSessionContainer.signInInstant,
         user);
  }

  public HydratedUserSessionContainer(String sessionId, ZonedDateTime signInInstant, IdentifiableUser user) {
    super(user.getId(), sessionId, signInInstant);
    this.user = user;
  }
}
