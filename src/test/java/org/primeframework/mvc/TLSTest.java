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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.inversoft.net.ssl.UnsafeTrustManager;
import org.example.action.user.EditAction;
import org.primeframework.mvc.PrimeBaseTest.TestContentModule;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.NoCORSConfigurationProvider;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.http.DefaultHTTPRequest;
import org.primeframework.mvc.http.DefaultHTTPResponse;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.http.HTTPStrings.Headers;
import org.primeframework.mvc.http.HTTPStrings.Methods;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.netty.PrimeHTTPListenerConfiguration;
import org.primeframework.mvc.netty.PrimeHTTPServerConfiguration;
import org.primeframework.mvc.security.MockOAuthUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
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
  public void beforeClass() {
    Module mvcModule = new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        bind(MVCConfiguration.class).toInstance(new MockConfiguration());
        bind(UserLoginSecurityContext.class).to(MockOAuthUserLoginSecurityContext.class);
        bind(CORSConfigurationProvider.class).to(NoCORSConfigurationProvider.class);
      }
    };

    KeyPair keyPair = generateNewRSAKeyPair();
    X509Certificate x509Certificate = generateX509Certificate(keyPair.getPublic(), keyPair.getPrivate());

    // Verify localhost as ok.
    final HostnameVerifier defaultHostnameVerifier = javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier();
    final HostnameVerifier localhostAcceptedHostnameVerifier = new javax.net.ssl.HostnameVerifier() {
      public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
        if (hostname.equals("localhost")) {
          return true;
        }
        return defaultHostnameVerifier.verify(hostname, sslSession);
      }
    };
    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(localhostAcceptedHostnameVerifier);

    Module module = Modules.override(mvcModule).with(new TestContentModule());
    TestPrimeMain main = new TestPrimeMain(
        new PrimeHTTPServerConfiguration(new PrimeHTTPListenerConfiguration().with(c -> c.httpsPort = 9081)
                                                                             .with(c -> c.privateKey = keyPair.getPrivate())
                                                                             .with(c -> c.x509Certificates = List.of(x509Certificate))),
        module);

    simulator = new RequestSimulator(main, new TestMessageObserver());

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
    HTTPObjectsHolder.setRequest(new DefaultHTTPRequest());
    HTTPObjectsHolder.setResponse(new DefaultHTTPResponse(new ByteArrayOutputStream()));
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
             .withBody("testing")
             .withHeader(Headers.MethodOverride, Methods.GET)
             .get()
             .assertStatusCode(200);

    assertTrue(EditAction.getCalled);
  }

  private KeyPair generateNewRSAKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      return keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private X509Certificate generateX509Certificate(PublicKey publicKey, PrivateKey privateKey)
      throws IllegalArgumentException {
    try {
      X509CertInfo certInfo = new X509CertInfo();
      CertificateX509Key certKey = new CertificateX509Key(publicKey);
      certInfo.set(X509CertInfo.KEY, certKey);
      // X.509 Certificate version 2 (0 based)
      certInfo.set(X509CertInfo.VERSION, new CertificateVersion(1));
      certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(ObjectIdentifier.of(KnownOIDs.SHA256withRSA))));
      certInfo.set(X509CertInfo.ISSUER, new X500Name("CN=localhost"));
      certInfo.set(X509CertInfo.SUBJECT, new X500Name("CN=org.primeframework.prime-mvc"));
      certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(Date.from(Instant.now().minusSeconds(30)), Date.from(Instant.now().plusSeconds(10_000))));
      certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)));

      X509CertImpl impl = new X509CertImpl(certInfo);
      impl.sign(privateKey, "SHA256withRSA");
      return impl;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
