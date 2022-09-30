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

import com.google.inject.Injector;
import com.google.inject.Module;
import io.fusionauth.http.server.HTTPServer;
import io.fusionauth.http.server.HTTPServerConfiguration;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.log.SLF4JLoggerFactoryAdapter;

/**
 * An abstract class that is used to create the main entry point for Prime (HTTP server and MVC).
 * <p>
 * To use this class, you need to subclass it like this:
 *
 * <pre>
 *   public class MyPrimeMain extends basePrimeMain {
 *     public static void main(String... args) {
 *       ...
 *     }
 *
 *     protected determinePort(Injector injector) {
 *       // Lookup port
 *     }
 *
 *     protected Module[] modules() {
 *       // Determine modules used to start the server. These can change during restarts
 *     }
 *   }
 * </pre>
 *
 * @author Brian Pontarelli
 */
public abstract class BasePrimeMain {
  protected Injector injector;

  protected PrimeMVCRequestHandler requestHandler;

  protected HTTPServer server;

  public abstract HTTPServerConfiguration configuration();

  public Injector getInjector() {
    return injector;
  }

  /**
   * This method handles swapping out the injector into a running HTTP server. It is production ready and should be
   * thread safe.
   */
  public void hup() {
    injector = GuiceBootstrap.initialize(modules());
    injector.injectMembers(this);
    requestHandler.updateInjector(injector);
  }

  /**
   * Registers the shutdown hook when Prime is started as an app rather than for testing (usually in a separate
   * thread).
   */
  public void registerShutdownHook() {
    // Add the shutdown hook (which will do nothing for testing purposes)
    Runtime.getRuntime().addShutdownHook(new PrimeHTTPServerShutdown());
  }

  /**
   * Shuts down the entire Prime system (HTTP server and MVC). This can be called from tests. It is also called by the
   * shutdown hook if it has been registered.
   */
  public void shutdown() {
    server.close();
    requestHandler.close();
  }

  public void start() {
    // Make the request handler
    requestHandler = new PrimeMVCRequestHandler(null);

    // Load the injector
    hup();

    // Create the server
    server = new HTTPServer().withConfiguration(configuration()).withHandler(requestHandler).withLoggerFactory(new SLF4JLoggerFactoryAdapter());

    // Start the server
    server.start();
  }

  protected abstract Module[] modules();

  private class PrimeHTTPServerShutdown extends Thread {
    public void run() {
      BasePrimeMain.this.shutdown();
    }
  }
}
