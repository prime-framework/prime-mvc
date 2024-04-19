/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

/**
 * Session container that includes a hydrated user object
 *
 * @author Brady Wied
 */
public interface HydratedSessionContainer extends SerializedSessionContainer {
  /**
   * The fully hydrated user object for who logged in
   *
   * @return logged in user
   */
  Object user();
}
