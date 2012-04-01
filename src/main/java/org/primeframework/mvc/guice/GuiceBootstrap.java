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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.util.ClassClasspathResolver;
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
@SuppressWarnings("unchecked")
public class GuiceBootstrap {
  private static final Logger logger = LoggerFactory.getLogger(GuiceBootstrap.class);

  /**
   * Please do not invoke this method unless you know what you are doing. This initializes Guice and does it once only
   * so that synchronization is not used. This is called by the PrimeServletContextListener when the context is created
   * and should cover all cases.
   *
   * @param modules The modules to initialize Guice with.
   * @return The Guice injector.
   */
  public static Injector initialize(Module... modules) {
    logger.debug("Initializing Guice");

    Set<Class<? extends Module>> classes = new HashSet<Class<? extends Module>>();
    addFromClasspath(classes);

    List<Module> allModules = new ArrayList<Module>();
    for (Class<? extends Module> moduleClass : classes) {
      try {
        allModules.add(moduleClass.newInstance());
      } catch (Exception e) {
        throw new PrimeException(e);
      }
    }

    for (Module module : modules) {
      if (classes.contains(module.getClass())) {
        continue;
      }

      allModules.add(module);
    }

    return Guice.createInjector(allModules);
  }

  /**
   * Shuts down the Guice injector by locating all of the {@link Closable} classes and calling Close on each of them.
   *
   * @param injector The Injector to shutdown.
   */
  public static void shutdown(Injector injector) {
    List<Key<? extends Closable>> keys = GuiceTools.getKeys(injector, Closable.class);
    for (Key<? extends Closable> key : keys) {
      Closable closable = injector.getInstance(key);
      closable.close();
    }
  }

  private static void addFromClasspath(Set<Class<? extends Module>> modules) {
    ClassClasspathResolver<Module> resolver = new ClassClasspathResolver<Module>();
    Set<Class<Module>> moduleClasses;
    try {
      moduleClasses = resolver.findByLocators(new ClassClasspathResolver.IsA(Module.class), false, "guice");
    } catch (IOException e) {
      throw new PrimeException(e);
    }

    Set<Class<Module>> matches = new HashSet<Class<Module>>(moduleClasses);

    for (Class<Module> moduleClass : moduleClasses) {
      // Ensure the class is not abstract
      if ((moduleClass.getModifiers() & Modifier.ABSTRACT) != 0 ||
        moduleClass.isAnonymousClass() || moduleClass.isLocalClass()) {
        matches.remove(moduleClass);
        continue;
      }

      // Remove any instances of this classes parents from the matches
      Class<? super Module> parent = moduleClass.getSuperclass();
      while (Module.class.isAssignableFrom(parent)) {
        matches.remove(parent);
        parent = parent.getSuperclass();
      }
    }

    for (Class<Module> match : matches) {
      logger.debug("Adding module [" + match + "] from classpath to Guice injector.");
    }

    modules.addAll(matches);
  }
}