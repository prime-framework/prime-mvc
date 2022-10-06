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

import java.net.InetSocketAddress;
import java.net.Socket;

import io.fusionauth.http.server.HTTPServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This allows the PrimeHTTPServer to be started in a background thread. The thread will be a non-daemon thread so that
 * the JVM does not terminate.
 *
 * @author Brian Pontarelli
 */
public class TestPrimeMainThread extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(TestPrimeMainThread.class);

  private final BasePrimeMain main;

  public TestPrimeMainThread(BasePrimeMain main) {
    super("Prime HTTP server thread for testing");
    this.main = main;
    setDaemon(true);
    start();

    // Wait for startup
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 10_000) {
      // Check if the Injector has been created and has injected the BasePrimeMain. This happens in the hup() method, which
      // is called in a separate thread, so it's a timing issue
      if (main.injector == null) {
        sleep();
        continue;
      }

      // Pause for good measure to let the injector inject the main instance itself
      sleep();

      // Assume we want the first configured port
      HTTPServerConfiguration configuration = main.configuration();
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress("localhost", configuration.getListeners().get(0).getPort()), 5);
        if (socket.isConnected()) {
          logger.info("Prime HTTP server started");
          break;
        }

        sleep();
      } catch (Exception ignore) {
      }
    }
  }

  private static void sleep() {
    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
      // Ignore
    }
  }

  public void run() {
    main.start();
  }

  public void shutdown() {
    main.shutdown();
  }
}
