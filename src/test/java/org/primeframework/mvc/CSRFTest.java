/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import org.example.domain.User;
import org.primeframework.mvc.security.MockUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.testng.annotations.Test;

/**
 * @author Daniel DeGroff
 */
public class CSRFTest extends PrimeBaseTest {
  @Inject public UserLoginSecurityContext securityContext;

  @Test(enabled = false)
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
}
