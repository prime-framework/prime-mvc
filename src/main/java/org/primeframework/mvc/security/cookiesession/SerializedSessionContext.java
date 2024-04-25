/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Session container serialized as JSON into the cookie
 *
 * @author Brady Wied
 */
public interface SerializedSessionContext {
  /**
   * The User ID for the logged in user
   *
   * @return the user ID
   */
  UUID userId();

  /**
   * The session ID for the logged in user
   *
   * @return the session ID
   */
  String sessionId();

  /**
   * The instant the user logged in
   *
   * @return instant user logged in
   */
  ZonedDateTime loginInstant();
}
