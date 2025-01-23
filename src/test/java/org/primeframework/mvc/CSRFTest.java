/*
 * Copyright (c) 2019-2025, Inversoft Inc., All Rights Reserved
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
import org.example.action.SecureAction;
import org.example.domain.User;
import org.primeframework.mvc.security.CBCCipherProvider;
import org.primeframework.mvc.security.DefaultEncryptor;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.MockUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel DeGroff
 */
public class CSRFTest extends PrimeBaseTest {
  @Inject public UserLoginSecurityContext securityContext;

  @Test
  public void get_CSRFToken() {
    MockUserLoginSecurityContext.roles.add("admin");
    securityContext.login(new User());

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .get()
             .assertStatusCode(200)
             .assertBody("Secure!");

    // A GET request won't contain the parameter
    // - This is just testing the RequestBuilder that will try and automatically add the CSRF token
    assertFalse(SecureAction.UnknownParameters.containsKey(csrfProvider.getParameterName()));
  }

  @Test
  public void post_CSRFOriginFailure() {
    MockUserLoginSecurityContext.roles.add("admin");
    securityContext.login(new User());

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .withSingleHeader("Origin", "https://malicious.com")
             .post()
             .assertStatusCode(403); // Unauthorized

    // Re-test with a "null" value for the Origin header
    simulator.test("/secure")
             .withSingleHeader("Origin", "null")
             .post()
             .assertStatusCode(403); // Unauthorized
  }

  @Test
  public void post_CSRFRefererFailure() {
    MockUserLoginSecurityContext.roles.add("admin");
    securityContext.login(new User());

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .withSingleHeader("Referer", "https://malicious.com")
             .post()
             .assertStatusCode(403); // Unauthorized
  }

  @Test
  public void post_CSRFTokenFailure() {
    MockUserLoginSecurityContext.roles.add("admin");
    securityContext.login(new User());

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .withCSRFToken("bad-token")
             .post()
             .assertStatusCode(403); // Unauthorized
  }

  @Test
  public void post_CSRFTokenSuccess() {
    MockUserLoginSecurityContext.roles.add("admin");
    securityContext.login(new User());

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .withSingleHeader("Referer", "http://localhost:" + simulator.getPort() + "/secure")
             .withCSRFToken(csrfProvider.getToken(request))
             .post()
             .assertStatusCode(200)
             .assertBody("Secure!");

    // No referer to ensure that RequestBuilder adds it
    simulator.test("/secure")
             .withCSRFToken(csrfProvider.getToken(request))
             .post()
             .assertStatusCode(200)
             .assertBody("Secure!");

    // No referer or token to ensure that RequestBuilder adds it
    simulator.test("/secure")
             .post()
             .assertStatusCode(200)
             .assertBody("Secure!");
  }

  @Test
  public void post_CSRFTokenCompatibility() throws Exception {
    // Use case: a CSRF token encrypted with CBC can be decrypted using the updated method
    MockUserLoginSecurityContext.roles.add("admin");
    securityContext.login(new User());
    configuration.csrfEnabled = true;

    // Craft a CSRF token, serialize to JSON, encrypt with CBC, base64url-encode
    CSRFToken token = new CSRFToken(securityContext.getSessionId(), System.currentTimeMillis());
    byte[] serialized = objectMapper.writeValueAsBytes(token);
    // Instantiate DefaultEncryptor with two copies of CBCCipherProvider to encrypt with CBC
    Encryptor cbcEncryptor = new DefaultEncryptor(new CBCCipherProvider(configuration), new CBCCipherProvider(configuration));
    byte[] encrypted = cbcEncryptor.encrypt(serialized);
    String encoded = Base64.getUrlEncoder().encodeToString(encrypted);

    simulator.test("/secure")
             .withSingleHeader("Referer", "http://localhost:" + simulator.getPort() + "/secure")
             .withCSRFToken(encoded)
             .post()
             .assertStatusCode(200)
             .assertBody("Secure!");

    // A POST request will contain the CSRF token
    // - This is just testing the RequestBuilder that will try and automatically add the CSRF token
    assertTrue(SecureAction.UnknownParameters.containsKey(csrfProvider.getParameterName()));
  }

  // Add for testing legacy-encrypted CSRF token which is defined as a private class in DefaultEncryptionBasedTokenCSRFProvider
  private record CSRFToken(String sid, long instant) {
  }
}
