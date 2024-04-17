/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Session container serialized as JSON into the cookie
 *
 * @author Brady Wied
 */
class SerializedSessionContainer {
  public UUID userId;

  public String sessionId;

  public ZonedDateTime loginInstant;

  SerializedSessionContainer(UUID userId, String sessionId, ZonedDateTime loginInstant) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.loginInstant = loginInstant;
  }

  @JsonCreator
  private SerializedSessionContainer() {
  }
}
