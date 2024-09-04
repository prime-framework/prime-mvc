/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import org.testng.annotations.Test;

/**
 * This class tests managed JSON cookies.
 *
 * @author Brian Pontarelli
 */
public class BrowserSessionTest extends PrimeBaseTest {
  @Test
  public void not_encrypted_cookie() throws Exception {
    // Scenario:
    // 1) Browser visits the /browser-session/decrypted page
    // 2) DecryptedAction sets the cookie value to a non-encrypted value
    // 3) SecondAction, which expects an encrypted cookie, will not be able to decrypt an un-encrypted cookie since
    //    encryption is required.

    test.simulate(() -> simulator.test("/browser-session/decrypted")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/browser-session/second")
                                 .assertContainsCookie("user"))
        .simulate(() -> simulator.test("/browser-session/second")
                                 .get()
                                 .assertStatusCode(200)
                                 // decrypted tries to use a non-encrypted cookie
                                 // to supply a user to SecondAction, which
                                 // requires an encrypted cookie by virtue of
                                 // relying on the defaults for @BrowserSession
                                 .assertBodyContains("The user is missing")
                                 .assertCookieWasDeleted("user"));
  }

  @Test
  public void sessionCookie() throws Exception {
    test.simulate(() -> simulator.test("/browser-session/first")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/browser-session/second")
                                 .assertContainsCookie("user"))
        .simulate(() -> simulator.test("/browser-session/second")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("The user is Brian Pontarelli")
                                 .assertContainsCookie("user"))
        .simulate(() -> simulator.test("/browser-session/second")
                                 .post()
                                 .assertStatusCode(302)
                                 .assertRedirect("/browser-session/second")
                                 .assertContainsCookie("user")) // Delete the cookie
        .simulate(() -> simulator.test("/browser-session/second")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("The user is missing")
                                 .assertDoesNotContainsCookie("user")); // Should be deleted
  }
}
