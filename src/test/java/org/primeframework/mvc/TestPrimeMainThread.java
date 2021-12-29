/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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

import java.net.Socket;

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
      try (Socket socket = new Socket("localhost", main.determinePort())) {
        if (socket.isConnected()) {
          logger.info("Prime HTTP server started");
          break;
        }

        Thread.sleep(500);
      } catch (Exception e) {
        // Ignore
      }
    }
  }

  public void run() {
    main.start();
  }

  public void shutdown() {
    main.shutdown();
  }
}
