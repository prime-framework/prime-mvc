/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.guice;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

/**
 * This class bootstraps Guice.
 *
 * @author Brian Pontarelli
 */
public class GuiceBootstrap {
  private static final Logger logger = LoggerFactory.getLogger(GuiceBootstrap.class);

  /**
   * Please do not invoke this method unless you know what you are doing. This initializes Guice and does it once only
   * so that synchronization is not used. This is called by the PrimeServletContextListener when the context is created
   * and should cover all cases.
   *
   * @param mainModule The main module for the application.
   * @return The Guice injector.
   */
  public static Injector initialize(Module mainModule) {
    logger.debug("Initializing Guice");
    return Guice.createInjector(mainModule);
  }

  /**
   * Shuts down the Guice injector by locating all of the {@link Closeable} classes and calling Close on each of them.
   *
   * @param injector The Injector to shutdown.
   */
  public static void shutdown(Injector injector) {
    List<Key<? extends Closeable>> keys = GuiceTools.getKeys(injector, Closeable.class);
    for (Key<? extends Closeable> key : keys) {
      Closeable closable = injector.getInstance(key);
      try {
        closable.close();
      } catch (IOException e) {
        logger.error("Unable to shutdown Closeable [" + key + "]", e);
      }
    }
  }
}