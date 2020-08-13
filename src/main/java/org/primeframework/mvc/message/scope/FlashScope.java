/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.message.scope;

/**
 * This is the flash scope which stores messages in the HttpSession under the flash key. It fetches values from the
 * HttpServletRequest under the same key as well as the HttpSession under that key. This allows for flash messages to be
 * migrated from the session to the request during request handling so that they are not persisted in the session
 * forever. However, it also allows flash values to be retrieved during the initial request from the session.
 *
 * @author Brian Pontarelli
 */
public interface FlashScope extends Scope {
  String KEY = "primeFlashMessages";

  /**
   * Moves the flash from the session to the request.
   */
  void transferFlash();
}
