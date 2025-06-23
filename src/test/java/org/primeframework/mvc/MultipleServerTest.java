/*
 * Copyright (c) 2001-2022, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.fusionauth.http.server.HTTPListenerConfiguration;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.http.server.HTTPServerConfiguration;
import org.primeframework.mvc.PrimeBaseTest.TestContentModule;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.NoCORSConfigurationProvider;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.security.MockOAuthUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import static org.primeframework.mvc.PrimeBaseTest.messageObserver;

/**
 * This class tests the MVC from a (mile) high level perspective. (see what I did there?)
 *
 * @author Brian Pontarelli
 */
public class MultipleServerTest {
  public RequestSimulator simulator;

  @AfterClass
  public void afterClass() {
    if (simulator != null) {
      simulator.shutdown();
    }

    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.clearResponse();
  }

  @Test
  public void multipleServers() {
    HTTPObjectsHolder.setRequest(new HTTPRequest());
    HTTPObjectsHolder.setResponse(new HTTPResponse());

    Module mvcModule = new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        bind(MVCConfiguration.class).toInstance(new MockConfiguration());
        bind(UserLoginSecurityContext.class).to(MockOAuthUserLoginSecurityContext.class);
        bind(CORSConfigurationProvider.class).to(NoCORSConfigurationProvider.class);
      }
    };

    Module module = Modules.override(mvcModule).with(new TestContentModule());
    var mainConfig = new HTTPServerConfiguration().withListener(new HTTPListenerConfiguration(9082));
    var secondaryConfig = new HTTPServerConfiguration().withListener(new HTTPListenerConfiguration(9083));
    TestPrimeMain main = new TestPrimeMain(new HTTPServerConfiguration[]{mainConfig, secondaryConfig}, module);
    simulator = new RequestSimulator(main, messageObserver, 9082);
    simulator.test("/port")
             .get()
             .assertStatusCode(200)
             .assertBody("9082");

    simulator.port = 9083;
    simulator.test("/port")
             .get()
             .assertStatusCode(200)
             .assertBody("9083");
  }
}