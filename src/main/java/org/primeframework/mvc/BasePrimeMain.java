/*
 * Copyright (c) 2021-2023, Inversoft Inc., All Rights Reserved
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

import java.util.LinkedList;
import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.fusionauth.http.server.HTTPServer;
import io.fusionauth.http.server.HTTPServerConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
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

  private PrimeMVCInstrumenter instrumenter;

  private List<PrimeHTTPServer> servers = new LinkedList<>();

  public abstract HTTPServerConfiguration[] configuration();

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
    servers.forEach(server -> server.handler.updateInjector(injector));
    instrumenter.updateInjector(injector);
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
   * Shuts down the entire Prime system (HTTP server(s) and MVC). This can be called from tests. It is also called by
   * the shutdown hook if it has been registered.
   */
  public void shutdown() {
    servers.forEach(server -> {
      server.server.close();
      server.handler.close();
    });
  }

  public void start() {
    // Make the instrumenter
    instrumenter = new PrimeMVCInstrumenter();

    // Load the injector because the configuration might not be known until the injector is created
    hup();

    // For each configured server, create the necessary objects
    for (var config : configuration()) {
      // Make the request handler for each server and use the current injector
      var requestHandler = new PrimeMVCRequestHandler(injector);

      // Set the logger factory for the server.
      configureLoggerFactory(config);

      // prime and the HTTP server both have a configuration for max file size on an upload request.
      // - Ensure they are compatible.
      // -
      // Note that the prime-mvc check must wait for the file to be written to disk, so it does not keep the file from being written.
      //   It waits until the file is written by the HTTP server and then fails nicely to let the end user know it is too big.
      // Note that the java-http check will be performed during upload, so if it fails, the prime-mvc request handler will not be invoked.
      MVCConfiguration mvcConfiguration = injector.getInstance(MVCConfiguration.class);
      long mvcMaxFileSize = mvcConfiguration.fileUploadMaxSize();
      long httpMaxFileSize = config.getMultipartConfiguration().getMaxFileSize();
      // TODO : Daniel : Review : We could use fileUploadMaxSize() to set the java-http configuration file instead of just ensuring it is larger.
      //        The only benefit if using the MVC config would be to provide a better error message I suppose?
      if (mvcMaxFileSize < httpMaxFileSize) {
        throw new IllegalStateException("MVCConfiguration.fileUploadMaxSize must be greater than or equal to the HTTP server configuration. Prime MVC configuration [" + mvcMaxFileSize + "] HTTP server configuration [" + httpMaxFileSize + "]");
      }

      // Create the server
      var server = new HTTPServer().withConfiguration(config)
                                   .withHandler(requestHandler)
                                   .withInstrumenter(instrumenter);

      servers.add(new PrimeHTTPServer(requestHandler, server));
    }

    // Start the server(s)
    servers.forEach(server -> server.server.start());
  }

  protected abstract Module[] modules();

  private void configureLoggerFactory(HTTPServerConfiguration config) {
    // If there is no loggerFactory, or the config is still using the java-http default, let's use SLF4J, cause that is how we roll.
    if (config.getLoggerFactory() == null || config.getLoggerFactory().getClass().equals(new HTTPServerConfiguration().getLoggerFactory().getClass())) {
      config.withLoggerFactory(new SLF4JLoggerFactoryAdapter());
    }
  }

  private static class PrimeHTTPServer {
    public final PrimeMVCRequestHandler handler;

    public final HTTPServer server;

    private PrimeHTTPServer(PrimeMVCRequestHandler handler, HTTPServer server) {
      this.handler = handler;
      this.server = server;
    }
  }

  private class PrimeHTTPServerShutdown extends Thread {
    public void run() {
      BasePrimeMain.this.shutdown();
    }
  }
}
