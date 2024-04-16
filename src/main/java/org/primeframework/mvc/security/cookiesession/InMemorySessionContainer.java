/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;

public class InMemorySessionContainer {
  public final Object user;

  public final String sessionId;

  public final ZonedDateTime signInInstant;

  public InMemorySessionContainer(SerializedSessionContainer serializedSessionContainer, Object user) {
    this(serializedSessionContainer.sessionId,
         serializedSessionContainer.signInInstant,
         user);
  }

  public InMemorySessionContainer(String sessionId, ZonedDateTime signInInstant, Object user) {
    this.sessionId = sessionId;
    this.signInInstant = signInInstant;
    this.user = user;
  }
}
