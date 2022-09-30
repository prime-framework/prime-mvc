/*
 * Copyright (c) 2012-2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import com.google.inject.Injector;
import org.primeframework.mock.MockUserAgent;
import org.primeframework.mvc.BasePrimeMain;
import org.primeframework.mvc.TestPrimeMainThread;
import org.primeframework.mvc.message.TestMessageObserver;

/**
 * This class provides a method for testing a full invocation of Prime. This makes full requests to a running Prime HTTP
 * server and an instance of the PrimeMVCRequestHandler.
 *
 * @author Brian Pontarelli
 */
public class RequestSimulator {
  public final TestMessageObserver messageObserver;

  public final MockUserAgent userAgent;

  private final BasePrimeMain main;

  private final TestPrimeMainThread thread;

  private RequestBuilder builder;

  private boolean useTLS;

  /**
   * Creates a new request simulator that can be used to simulate requests to a Prime application.
   *
   * @param main            The main application entry point for the app being tested (or a test entry point). This
   *                        starts the HTTP server and is blocking, but we'll start a thread for it and manage the
   *                        lifecycle in this class.
   * @param messageObserver Used to observe messages from within the HTTP server so that they can be asserted on.
   */
  public RequestSimulator(BasePrimeMain main, TestMessageObserver messageObserver) {
    this.main = main;
    this.thread = new TestPrimeMainThread(main);
    this.messageObserver = messageObserver;
    this.userAgent = new MockUserAgent();
  }

  public Injector getInjector() {
    return main.getInjector();
  }

  public int getPort() {
    return builder.resolveSimulatorPort();
  }

  public void reset() {
    userAgent.clearAllCookies();
    this.builder = null;
    useTLS = false;
  }

  public void shutdown() {
    this.thread.shutdown();
    this.builder = null;
    useTLS = false;
  }

  /**
   * Starts a test for the given path. This returns a RequestBuilder that you can use to set the request up correctly
   * for the test and then execute the GET or POST.
   *
   * @param path The path to test.
   * @return The RequestBuilder.
   */
  public RequestBuilder test(String path) {
    builder = new RequestBuilder(path, main.getInjector(), userAgent, main.configuration(), messageObserver);
    builder.useTLS = useTLS;
    return builder;
  }

  public RequestSimulator withTLS(boolean useTLS) {
    this.useTLS = useTLS;
    return this;
  }
}
