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

public class CSRFTest extends PrimeBaseTest {
  @Inject public UserLoginSecurityContext securityContext;

  @Test
  public void post_CSRFOriginFailure() {
    MockUserLoginSecurityContext.roles.add("admin");

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .withSingleHeader("Origin", "https://malicious.com")
             .withSingleHeader("Referer", null)
             .setup(req -> securityContext.login(new User()))
             .post()
             .assertStatusCode(403); // Unauthorized
  }

  @Test
  public void post_CSRFRefererFailure() {
    MockUserLoginSecurityContext.roles.add("admin");

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .withSingleHeader("Origin", null)
             .withSingleHeader("Referer", "https://malicious.com")
             .setup(req -> securityContext.login(new User()))
             .post()
             .assertStatusCode(403); // Unauthorized
  }

  @Test
  public void post_CSRFTokenFailure() {
    MockUserLoginSecurityContext.roles.add("admin");

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .setup(req -> securityContext.login(new User()))
             .withCSRFToken("bad-token")
             .post()
             .assertStatusCode(403); // Unauthorized
  }

  @Test
  public void post_CSRFTokenSuccess() {
    MockUserLoginSecurityContext.roles.add("admin");

    configuration.csrfEnabled = true;
    simulator.test("/secure")
             .setup(req -> securityContext.login(new User()))
             .post()
             .assertStatusCode(200)
             .assertBody("Secure!");
  }
}
