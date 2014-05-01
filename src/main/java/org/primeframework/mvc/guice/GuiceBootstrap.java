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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * Shuts down the Guice injector by locating all of the {@link Closeable} singletons classes and calling Close on each
   * of them.
   *
   * @param injector The Injector to shutdown.
   */
  public static void shutdown(Injector injector) {
    Map<Key<?>, Binding<?>> bindings = injector.getBindings();
    for (Key<?> key : bindings.keySet()) {
      Type type = key.getTypeLiteral().getType();
      if (type instanceof ParameterizedType) {
        type = ((ParameterizedType) type).getRawType();
      }

      if (type instanceof Class) {
        Class<?> bindingType = (Class<?>) type;
        if (Closeable.class.isAssignableFrom(bindingType) && Scopes.isSingleton(bindings.get(key))) {
          Closeable closable = (Closeable) injector.getInstance(key);
          try {
            closable.close();
          } catch (Throwable t) {
            logger.error("Unable to shutdown Closeable [" + key + "]");
          }
        }
      }
    }
  }
}