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
    // Use case: this covers a situation that was thought to only come up in CSRF testing, but it turns out that it was possible with any cookie
    //  that is not compressed or encrypted.
    // 1) An action uses a @ManagedCookie or @ManagedSessionCookie with compress=false / encrypt=false
    // 2) When the action is requested, BaseManagedCookieScope.processCookie() attempts to parse the base64-encoded cookie value as a legacy cookie (no header bytes)
    // 3) The function applied for parsing in this case attempts to parse the value as a JSON string using Jackson
    // 4) If base64-decoded cookie value starts with valid JSON followed by a newline (\n, 0x0a, 10) character, Jackson stops its parsing at that point at returns the
    //    JSON value before the newline.
    //
    // The fix is to add the header byte prefix before base64-encoding the value for all cookies going forward. Cookie parsing has not been updated in
    //  order to maintain backward compatibility. This test demonstrates the bug that exists in cookie parsing and shows that the new MVC cookie-writing
    //  logic that includes the header bytes means that the same payload can be written and read back properly.

    // When base64-decoded, the beginning of this cookie value is "5\n". When a cookie does not contain the header bytes, we attempt to
    //  parse the value as a JSON string. Jackson interprets the \n as the end of the JSON string, and the number 5 is valid JSON.
    var badCookieValue = "NQryyR_pFrynPybHfMk_4Hka_J0HZ1WV6iVVWVki0mVg-WpdVkk2HO8_XQ46yhw8_w==";

    // Send the bad cookie value directly on a GET request to avoid it being written by PMVC
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

    // Make a request to set the cookie value via the PMVC annotation
    simulator.test("/managed-cookie")
             .withParameter("value", badCookieValue)
             .post()
             // The original value is available to the action (technically comes from the "value" parameter directly in this case)
             .assertBody(badCookieValue)
             // The cookie value is prefaced with a header
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
                                 .assertCookie("cookie", CookieTools.toCookie("bar".getBytes(), true, false, encryptor))
        )
        // modern (compressed, not encrypted) cookie is now set in our simulator user agent
        .simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody("bar")
                                 .assertCookie("cookie", CookieTools.toCookie("bar".getBytes(), true, false, encryptor))
        )
        // Test the compressed-only cookie on a page that requires the cookie to be encrypted.
        .simulate(() -> simulator.test("/encrypted-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 // Failed to decrypt the cookie, but it failed gracefully (neverNull is true).
                                 // The body does not contain the cookie value.
                                 .assertNormalizedBody("(null)")
                                 // The cookie was deleted because it failed parsing.
                                 .assertCookieWasDeleted("cookie")
        )
        // Making the request back to the compressed page will not find the cookie
        .simulate(() -> simulator.test("/compressed-managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody("(null)")
                                 .assertDoesNotContainsCookie("cookie")
        );
  }

  @Test
  public void cookie_accidentally_starts_with_header() throws Exception {
    // A cookie that happens to start with the header will be interpreted accordingly
    // BBB<null>BB
    var cookie = "QkJCAEJC";

    // Make a request with the raw cookie value included in the header
    test.simulate(() -> simulator.test("/managed-cookie")
                                 .withCookie("cookie", cookie)
                                 .get()
                                 .assertStatusCode(200)
                                 // The body contains string after removing header bytes
                                 .assertBody("BB")
                                 // The new cookie value has header bytes and is encoded
                                 .assertCookie("cookie", cookie))
        // A second request using the cookie value set in user agent behaves the same
        .simulate(() -> simulator.test("/managed-cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBody("BB")
                                 .assertCookie("cookie", cookie));
  }

  @Test
  public void cookie_is_base64_encoded_without_header() throws Exception {
    // Use case: a cookie that is base64-encoded before being written by PMVC will still be base64-encoded when reading back.
    String cookie = Base64.getEncoder().encodeToString("foobar".getBytes());

    // Provide the base64-encoded value as a legacy cookie without headers by including directly on request.
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
    // Test use of multiple cookies on a single action
    var fooCookie = CookieTools.toCookie("foo".getBytes(), false, false, encryptor);
    var barCookie = CookieTools.toCookie("bar".getBytes(), false, false, encryptor);
    var bazCookie = CookieTools.toCookie("baz".getBytes(), false, false, encryptor);

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
                                 // The cookies are written in new format
                                 .assertCookie("cookie1", fooCookie)
                                 .assertCookie("cookie2", barCookie)
                                 .assertCookie("fusionauth.sso", bazCookie)
        )

        // Cookies are persisted, hit the GET, and they will still be there.
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", fooCookie)
                                 .assertCookie("cookie2", barCookie)
                                 .assertCookie("fusionauth.sso", bazCookie)
        )

        // Delete stringCookie2
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .withURLParameter("deleteCookie2", true)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", fooCookie)
                                 .assertCookieWasDeleted("cookie2")
                                 .assertCookie("fusionauth.sso", bazCookie))

        // Next request stringCookie2 will be all the way gone
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", fooCookie)
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookie("fusionauth.sso", bazCookie))

        // stringCookie1 and stringCookie3 holding strong after another request
        .simulate(() -> simulator.test("/managed-cookie-scopes")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertCookie("cookie1", fooCookie)
                                 .assertDoesNotContainsCookie("cookie2")
                                 .assertCookie("fusionauth.sso", bazCookie))

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
    // 2) The cookie is parsed, and the value is displayed as a JSON string
    // 3) The cookie is rewritten in the modern format with a header
    // 4) The follow-up request with the modern cookie still renders the JSON string
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
