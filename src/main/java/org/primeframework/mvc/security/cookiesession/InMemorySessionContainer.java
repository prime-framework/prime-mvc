/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;

public class InMemorySessionContainer extends SerializedSessionContainer {
  public final IdentifiableUser user;

  public InMemorySessionContainer(SerializedSessionContainer serializedSessionContainer, IdentifiableUser user) {
    this(serializedSessionContainer.sessionId,
         serializedSessionContainer.signInInstant,
         user);
  }

  public InMemorySessionContainer(String sessionId, ZonedDateTime signInInstant, IdentifiableUser user) {
    super(user.getId(), sessionId, signInInstant);
    this.user = user;
  }
}
