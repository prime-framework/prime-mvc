/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SerializedSessionContainer {
  public UUID userId;
  public String sessionId;
  public ZonedDateTime signInInstant;

  SerializedSessionContainer(UUID userId, String sessionId, ZonedDateTime signInInstant) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.signInInstant = signInInstant;
  }

  @JsonCreator
  private SerializedSessionContainer() {}
}
