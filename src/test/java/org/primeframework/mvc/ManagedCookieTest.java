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

import java.util.Base64;

import com.google.inject.Inject;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.CookieTools;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

/**
 * This class tests managed cookies.
 *
 * @author Brian Pontarelli
 */
public class ManagedCookieTest extends PrimeBaseTest {
  @Inject public Encryptor encryptor;

  @Test
  public void compressed_only_cookie() throws Exception {
    test.simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .withParameter("value", "bar")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBody("bar")
                                 .assertContainsCookie("cookie"))
        .simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody("bar"))
        .simulate(() -> simulator.test("/encrypted-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 // this would be expected to "fail" gracefully (neverNull is true)
                                 // because we're taking an unencrypted managed cookie from CompressedManagedCookieAction
                                 // and feeding it to EncryptedManagedCookieAction which requires encryption
                                 .assertNormalizedBody("(null)"));
  }

  @Test
  // test fails in current code as well
  @Ignore
  public void cookie_accidentally_starts_with_header() throws Exception {
    // BBB<null>BB
    var cookie = "QkJCAEJC";

    test.simulate(() -> simulator.test("/managed-cookie")
                                 .withCookie("cookie1", cookie)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "QkJCAEJC"));
  }

  @Test
  public void cookie_is_base64_encoded_without_header() throws Exception {
    String cookie = Base64.getEncoder().encodeToString("foobar".getBytes());

    test.simulate(() -> simulator.test("/managed-cookie")
                                 .withCookie("cookie1", cookie)
                                 .get()
                                 .assertStatusCode(200)
                                 // Zm9vYmFy is base64 encoded foobar
                                 .assertCookie("cookie1", "Zm9vYmFy"));
  }

  @Test
  public void managed_cookie_scope() throws Exception {
    // Values are not set, no cookies
    test.simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertDoesNotContainsCookie("cookie1")
                                 .assertDoesNotContainsCookie("cookie2")
                                 // there should never be this one, it is named 'fusionauth.sso'
                                 .assertDoesNotContainsCookie("cookie3")
                                 .assertDoesNotContainsCookie("fusionauth.sso"))

        // Write all three cookies
        .simulate(() -> simulator.test("/managed-cookie")
                                 .withURLParameter("writeCookie1", "foo")
                                 .withURLParameter("writeCookie2", "bar")
                                 .withURLParameter("writeCookie3", "baz")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo"))
        .assertCookie("cookie2", "bar")
        .assertCookie("fusionauth.sso", "baz")

        // Cookies are persisted, hit the GET, and they will still be there.
        .simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo"))
        .assertCookie("cookie2", "bar")
        .assertCookie("fusionauth.sso", "baz")

        // Delete stringCookie2
        .simulate(() -> simulator.test("/managed-cookie")
                                 .withURLParameter("deleteCookie2", true)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo")
                                 .assertCookieWasDeleted("cookie2")
                                 .assertCookie("fusionauth.sso", "baz"))

        // Next request stringCookie2 will be all the way gone
        .simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookie("fusionauth.sso", "baz"))

        // stringCookie1 and stringCookie3 holding strong after another request
        .simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookie("fusionauth.sso", "baz"))

        // Delete all of them!!! - 1 and 3
        .simulate(() -> simulator.test("/managed-cookie")
                                 .withURLParameter("deleteCookie1", true)
                                 .withURLParameter("deleteCookie3", true)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookieWasDeleted("cookie1")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookieWasDeleted("fusionauth.sso"))

        // They are now all gone.
        .simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertDoesNotContainsCookie("cookie1")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertDoesNotContainsCookie("fusionauth.sso"));
  }

  @Test
  public void uncompressed_unencrypted_starts_with_header_byte_sequence() throws Exception {
    // if compress and encrypt are off, BaseManagedCookieScope#buildCookie will not put a header on the cookie
    // but there might be some edge cases where we have a header indicating an uncompressed, unencrypted cookie
    // and we should allow that if the annotation does not use/require encryption (and
    // ManagedCookieAction's cookie1 field does not)
    String value = "foo";
    byte[] json = objectMapper.writeValueAsBytes(value);
    String base64EncodedCookieWithHeader = CookieTools.toCookie(json, false, false, encryptor);

    test.simulate(() -> simulator.test("/managed-cookie")
                                 .withCookie("cookie1", base64EncodedCookieWithHeader)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo"));
  }
}
