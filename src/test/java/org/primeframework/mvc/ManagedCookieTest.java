/*
 * Copyright (c) 2001-2025, Inversoft Inc., All Rights Reserved
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

import java.nio.charset.StandardCharsets;
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
  public void broken_csrf_case() throws Exception {
    // When base64-decoded, the beginning of this cookie value is "5\n". When a cookie does not contain the header bytes, we attempt to
    //  parse the value as a JSON string. Jackson interprets the \n as the end of the JSON string, and the number 5 is valid JSON.
    var badCookieValue = "NQryyR_pFrynPybHfMk_4Hka_J0HZ1WV6iVVWVki0mVg-WpdVkk2HO8_XQ46yhw8_w==";

    // Send the bad cookie value directly on a GET request.
    simulator.test("/managed-cookie")
             .withCookie("cookie", badCookieValue)
             .get()
             // The cookie is base64-decoded and parsed as JSON
             .assertBody("5")
             // The new cookie value is written with the header and base64-encoded
             .assertCookie("cookie", CookieTools.toCookie("5".getBytes(), false, false, encryptor));

    // Make a second GET request with the cookie. No change in rendered value or cookie value
    simulator.test("/managed-cookie")
             .get()
             .assertBody("5")
             .assertCookie("cookie", CookieTools.toCookie("5".getBytes(), false, false, encryptor));

    // Make a request to set the cookie value via the annotation
    simulator.test("/managed-cookie")
             .withParameter("value", badCookieValue)
             .post()
             .assertBody(badCookieValue)
             .assertCookie("cookie", CookieTools.toCookie(badCookieValue.getBytes(), false, false, encryptor));

    // Make a GET request with the existing cookie value. No change.
    simulator.test("/managed-cookie")
             .get()
             .assertBody(badCookieValue)
             .assertCookie("cookie", CookieTools.toCookie(badCookieValue.getBytes(), false, false, encryptor));
  }

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
    var modernCookie = CookieTools.toCookie(value.getBytes(), true, false, encryptor);
    test.simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .withCookie("cookie", legacyCookieWithNoCompression)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody(value)
                                 // Expected result is modern format with 0x42 3 times...
                                 .assertCookie("cookie", modernCookie))

        // The request works a second time with the updated cookie
        .simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody(value)
                                 .assertCookie("cookie", modernCookie))
    ;
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
    var modernCookie = CookieTools.toCookie(value.getBytes(), true, false, encryptor);
    test.simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .withCookie("cookie", legacyCookieWithNoCompression)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody(value)
                                 // Expected result is modern format with 0x42 3 times...
                                 .assertCookie("cookie", modernCookie)
        )

        // The request works a second time with the updated cookie
        .simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody(value)
                                 .assertCookie("cookie", modernCookie))
    ;
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
                                 .withCookie("cookie", cookie)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie", "QkJCAEJC"));
  }

  @Test
  public void cookie_is_base64_encoded_without_header() throws Exception {
    String cookie = Base64.getEncoder().encodeToString("foobar".getBytes());

    test.simulate(() -> simulator.test("/managed-cookie")
                                 .withCookie("cookie", cookie)
                                 .get()
                                 .assertStatusCode(200)
                                 // The rendered value is not decoded
                                 .assertBody(cookie)
                                 // The cookie will be updated with a header
                                 .assertCookie("cookie", CookieTools.toCookie(cookie.getBytes(), false, false, encryptor)));

    // Make a second GET request with the updated cookie. No change to body or cookie value
    test.simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody(cookie)
                                 .assertCookie("cookie", CookieTools.toCookie(cookie.getBytes(), false, false, encryptor)));
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
    test.simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertDoesNotContainsCookie("cookie1")
                                 .assertDoesNotContainsCookie("cookie2")
                                 // there should never be this one, it is named 'fusionauth.sso'
                                 .assertDoesNotContainsCookie("cookie3")
                                 .assertDoesNotContainsCookie("fusionauth.sso"))

        // Write all three cookies
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .withURLParameter("writeCookie1", "foo")
                                 .withURLParameter("writeCookie2", "bar")
                                 .withURLParameter("writeCookie3", "baz")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo"))
        .assertCookie("cookie2", "bar")
        .assertCookie("fusionauth.sso", "baz")

        // Cookies are persisted, hit the GET, and they will still be there.
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo"))
        .assertCookie("cookie2", "bar")
        .assertCookie("fusionauth.sso", "baz")

        // Delete stringCookie2
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .withURLParameter("deleteCookie2", true)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo")
                                 .assertCookieWasDeleted("cookie2")
                                 .assertCookie("fusionauth.sso", "baz"))

        // Next request stringCookie2 will be all the way gone
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookie("fusionauth.sso", "baz"))

        // stringCookie1 and stringCookie3 holding strong after another request
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", "foo")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookie("fusionauth.sso", "baz"))

        // Delete all of them!!! - 1 and 3
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .withURLParameter("deleteCookie1", true)
                                 .withURLParameter("deleteCookie3", true)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookieWasDeleted("cookie1")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookieWasDeleted("fusionauth.sso"))

        // They are now all gone.
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertDoesNotContainsCookie("cookie1")
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertDoesNotContainsCookie("fusionauth.sso"));
  }

  @Test
  public void uncompressed_unencrypted_contains_json() throws Exception {
    // 1) The legacy cookie contains a value that can be parsed as JSON
    String value = "foo"; // `foo`
    byte[] json = objectMapper.writeValueAsBytes(value); // `"foo"`
    String jsonStr = new String(json, StandardCharsets.UTF_8);

    // The first request is a modern, base64-encoded cookie with headers. The value is the JSON String `"foo"`
    test.simulate(() -> simulator.test("/managed-cookie")
                                 .withCookie("cookie", jsonStr)
                                 .get()
                                 .assertStatusCode(200)
                                 // `"foo"` is rendered
                                 .assertBody(jsonStr)
                                 // The new cookie is written in the modern format
                                 .assertCookie("cookie", CookieTools.toCookie(json, false, false, encryptor)));

    // The request works a second time with the updated cookie
    test.simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 // The cookie still contains `"foo"` after decoding
                                 .assertBody(jsonStr)
                                 // No change in the cookie value
                                 .assertCookie("cookie", CookieTools.toCookie(json, false, false, encryptor)));
  }
}
