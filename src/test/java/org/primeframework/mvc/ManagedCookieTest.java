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
import org.primeframework.mvc.security.CBCCipherProvider;
import org.primeframework.mvc.security.DefaultEncryptor;
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
  @Inject private Encryptor encryptor;

  @Test
  public void compressed_annotation_legacy_uncompressed_cookie_longer_than_5() throws Exception {
    // Scenario:
    // 1) Browser has uncompressed, non-encrypted cookie with value 'foobar' from a long time ago
    // 2) Application decides to start compressing (but not encrypting) cookie. Adds @ManagedCookie(compress = true, encrypt = false)
    // to app
    // 3) Browser submits cookie

    var value = "foobar";
    byte[] json = objectMapper.writeValueAsBytes(value);
    var legacyCookieWithNoCompression = Base64.getEncoder().encodeToString(json);
    test.simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .withCookie("cookie", legacyCookieWithNoCompression)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody("foobar")
                                 // Expected result is modern format with 0x42 3 times...
                                 .assertCookie("cookie", "QkJCAnjaS8vPT0osAgAIqwJ6"));
  }

  @Test
  public void compressed_annotation_legacy_uncompressed_cookie_shorter_than_5() throws Exception {
    // Scenario:
    // 1) Browser has uncompressed, non-encrypted cookie with value 'f' from a long time ago
    // 2) Application decides to start compressing (but not encrypting) cookie. Adds @ManagedCookie(compress = true, encrypt = false)
    // to app
    // 3) Browser submits cookie

    var value = "f";
    byte[] json = objectMapper.writeValueAsBytes(value);
    var legacyCookieWithNoCompression = Base64.getEncoder().encodeToString(json);
    test.simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .withCookie("cookie", legacyCookieWithNoCompression)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody("f")
                                 // Expected result is modern format with 0x42 3 times...
                                 .assertCookie("cookie", "QkJCAnjaSwMAAGcAZw=="));
  }

  @Test
  public void compressed_only_cookie() throws Exception {
    // Browser sets cookie for the first time
    test.simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .withParameter("value", "bar")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBody("bar")
                                 .assertContainsCookie("cookie"))
        // modern (compressed, not encrypted) cookie is now set in our simulator user agent
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
  public void legacy_cookie() throws Exception {
    // Scenario:
    // 1) Browser has uncompressed, AES/CBC-encrypted cookie with value '"foo"' from a long time ago
    // 2) Application upgrades to the modern cookie format with header, etc.
    // 3) Browser submits cookie
    String value = "foo";
    byte[] json = objectMapper.writeValueAsBytes(value);
    // We want to use the deprecated encrypt method to test forward compatibility with the new decryption method
    // Instantiate DefaultEncryptor with two copies of CBCCipherProvider to encrypt with CBC
    Encryptor cbcEncryptor = new DefaultEncryptor(new CBCCipherProvider(configuration), new CBCCipherProvider(configuration));
    byte[] legacyEncrypted = cbcEncryptor.encrypt(json);
    String legacyEncoded = Base64.getUrlEncoder().encodeToString(legacyEncrypted);

    // This is the legacy version, but it should work even though the EncryptedManagedCookieAction
    // has compression enabled
    test.simulate(() -> simulator.test("/encrypted-managed-cookie")
                                 .withCookie("cookie", legacyEncoded)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertNormalizedBody("foo")
                                 // during the get, our cookie is modernized
                                 .assertEncryptedCookie("cookie", "foo"))

        // Set a modern version and re-test
        .simulate(() -> simulator.test("/encrypted-managed-cookie")
                                 .withParameter("value", "bar")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertNormalizedBody("bar")
                                 .assertEncryptedCookie("cookie", "bar"))
        .simulate(() -> simulator.test("/encrypted-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertNormalizedBody("bar"));
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
