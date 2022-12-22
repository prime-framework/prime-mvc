/*
 * Copyright (c) 2021-2022, Inversoft Inc., All Rights Reserved
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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.inversoft.net.ssl.UnsafeTrustManager;
import io.fusionauth.http.HTTPValues.Headers;
import io.fusionauth.http.HTTPValues.Methods;
import io.fusionauth.http.server.HTTPListenerConfiguration;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.http.server.HTTPServerConfiguration;
import org.example.action.user.EditAction;
import org.primeframework.mvc.PrimeBaseTest.TestContentModule;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.NoCORSConfigurationProvider;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.security.MockOAuthUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel DeGroff
 */
public class TLSTest {
  public RequestSimulator simulator;

  @AfterClass
  public void afterClass() {
    simulator.shutdown();

    try {
      SSLContext.getInstance("SSL").init(null, null, null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterMethod
  public void afterMethod() {
    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.clearResponse();
  }

  @BeforeClass
  public void beforeClass() throws IOException, GeneralSecurityException {
    Module mvcModule = new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        bind(MVCConfiguration.class).toInstance(new MockConfiguration());
        bind(UserLoginSecurityContext.class).to(MockOAuthUserLoginSecurityContext.class);
        bind(CORSConfigurationProvider.class).to(NoCORSConfigurationProvider.class);
      }
    };

    String certificate = Files.readString(Paths.get("src/test/resources/testcert.pem"));
    String privateKey = Files.readString(Paths.get("src/test/resources/testcert.key"));
    Module module = Modules.override(mvcModule).with(new TestContentModule());
    var config = new HTTPServerConfiguration().withListener(new HTTPListenerConfiguration(9081, certificate, privateKey));
    TestPrimeMain main = new TestPrimeMain(new HTTPServerConfiguration[]{config}, module);

    simulator = new RequestSimulator(main, new TestMessageObserver(), 0, 9081);

    // Disable SSL validation so we can use a self-signed cert
    try {
      SSLContext context = SSLContext.getInstance("SSL");
      context.init(null, new TrustManager[]{new UnsafeTrustManager()}, null);
      HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets up the servlet objects and injects the test.
   */
  @BeforeMethod
  public void beforeMethod() {
    HTTPObjectsHolder.setRequest(new HTTPRequest());
    HTTPObjectsHolder.setResponse(new HTTPResponse(null, null));
  }

  @Test
  public void tls() throws IOException {
    // Just a copy of GlobalTest.get(), the Test isn't important, just that we are making the request over TLS.
    simulator.withTLS(true)
             .test("/user/edit")
             .get()
             .assertStatusCode(200)
             // header name is not case-sensitive
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertHeaderContains("cache-control", "no-cache")
             .assertHeaderDoesNotContain("Potato")
             .assertBodyFile(Path.of("src/test/resources/html/edit.html"));

    EditAction.getCalled = false;
    simulator.withTLS(true)
             .test("/user/edit")
             .withHeader(Headers.MethodOverride, Methods.GET)
             .get()
             .assertStatusCode(200);

    assertTrue(EditAction.getCalled);
  }
}
