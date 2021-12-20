/*
 * Copyright (c) 2012-2019, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mock.MockUserAgent;
import org.primeframework.mvc.BasePrimeMain;
import org.primeframework.mvc.message.TestMessageObserver;

/**
 * This class provides a method for testing a full invocation of Prime. This simulates the JEE web objects
 * (HttpServletRequest, etc.) and an invocation of the PrimeMVCRequestHandler. You can also simulate multiple
 * invocations across a single session by using the same instance of this class multiple times.
 *
 * @author Brian Pontarelli
 */
public class RequestSimulator {
  public final TestMessageObserver messageObserver;

  public final MockUserAgent userAgent;

  private final BasePrimeMain main;

  /**
   * Creates a new request simulator that can be used to simulate requests to a Prime application.
   *
   * @param main            The PrimeMain that is used to start an HTTP server.
   * @param messageObserver Used to observe messages from within the HTTP server so that they can be asserted on.
   */
  public RequestSimulator(BasePrimeMain main, TestMessageObserver messageObserver) {
    this.main = main;
    this.messageObserver = messageObserver;
    this.userAgent = new MockUserAgent();

    // Start the server
    this.main.start();
  }

  public void shutdown() {
    main.stop();
  }

  /**
   * Starts a test for the given path. This returns a RequestBuilder that you can use to set the request up correctly
   * for the test and then execute the GET or POST.
   *
   * @param path The path to test.
   * @return The RequestBuilder.
   */
  public RequestBuilder test(String path) {
    return new RequestBuilder(main.determinePort(), path, main.getInjector(), userAgent, messageObserver);
  }
}
