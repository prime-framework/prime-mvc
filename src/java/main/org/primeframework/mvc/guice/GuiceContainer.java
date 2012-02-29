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
import java.util.logging.Logger;

import net.java.lang.ClassClassLoaderResolver;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Named;

/**
 * <p> This class is a singleton that allows Guice to be configured by JCatapult and then used in places like Struts or
 * anywhere else in the container. This should be one of the only places that is a singleton in the entire JCatapult
 * system and within any application since it allows for classes to be injected after construction (like JSP tag
 * libraries) and the like. </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class GuiceContainer {
  private static final Logger logger = Logger.getLogger(GuiceContainer.class.getName());
  private static Module[] guiceModules;
  private static String[] guiceModulesNames;
  private static String[] guiceExcludeModules;
  private static Injector injector;

  /**
   * @return Retrieves the injector that is setup via the {@link #initialize()} method.
   */
  public static Injector getInjector() {
    return injector;
  }

  /**
   * The comma separated list of guice modules to use if guice is being setup by the filter. This is controlled by the
   * jcatapult configuration property named <strong>jcatapult.guice.modules</strong>.
   *
   * @param guiceModules A comma separated list of guice modules to use.
   */
  @Inject(optional = true)
  public static void setGuiceModulesNames(@Named("jcatapult.guice.modules") String guiceModules) {
    if (guiceModules == null) {
      GuiceContainer.guiceModulesNames = null;
    } else {
      GuiceContainer.guiceModulesNames = guiceModules.split("\\W*,\\W*");
    }
  }

  /**
   * The list of guice modules to use if guice is being setup by the filter.
   *
   * @param guiceModules A list of guice modules to use.
   */
  public static void setGuiceModules(Module... guiceModules) {
    GuiceContainer.guiceModules = guiceModules;
  }

  /**
   * The comma separated list of guice modules to exclude if the GuiceContainer is configured to search the classpath
   * for modules and implement the leaf node convention. This is controlled by the JCatapult configuration property
   * named <strong>jcatapult.guice.exclude.modules</strong>.
   *
   * @param guiceExcludeModules A comma separated list of guice modules to exclude.
   */
  @Inject(optional = true)
  public static void setExcludeGuiceModules(@Named("jcatapult.guice.exclude.modules") String guiceExcludeModules) {
    if (guiceExcludeModules == null) {
      GuiceContainer.guiceExcludeModules = null;
    } else {
      GuiceContainer.guiceExcludeModules = guiceExcludeModules.split("\\W*,\\W*");
    }
  }

  /**
   * Please do not invoke this method unless you know what you are doing. This initializes Guice and does it once only
   * so that synchronization is not used. This is called by the JCatapultFilter in its constructor and should cover all
   * cases.
   */
  public static void initialize() {
    logger.fine("Initializing JCatapult's Guice support");

    Set<Class<? extends Module>> classes = new HashSet<Class<? extends Module>>();
    addFromClasspath(classes);

    addFromConfiguration(classes);

    List<Module> modules = new ArrayList<Module>();
    for (Class<? extends Module> moduleClass : classes) {
      try {
        modules.add(moduleClass.newInstance());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (GuiceContainer.guiceModules != null) {
      for (Module module : GuiceContainer.guiceModules) {
        if (classes.contains(module.getClass())) {
          continue;
        }

        modules.add(module);
      }
    }

    GuiceContainer.injector = Guice.createInjector(modules);
  }

  /**
   * Shuts down the Guice container by locating all of the {@link Closable} classes and calling Close on each of them.
   * This method sets the Injector to null and returns it.
   *
   * @return The Injector in case it is needed afterwards.
   */
  public static Injector shutdown() {
    List<Key<? extends Closable>> keys = GuiceTools.getKeys(injector, Closable.class);
    for (Key<? extends Closable> key : keys) {
      Closable closable = injector.getInstance(key);
      closable.close();
    }

    injector = null;
    guiceExcludeModules = null;
    guiceModules = null;
    guiceModulesNames = null;

    return injector;
  }

  private static void addFromConfiguration(Set<Class<? extends Module>> modules) {
    // If there is nothing to build, return
    if ((GuiceContainer.guiceModules == null || GuiceContainer.guiceModules.length == 0) &&
      (GuiceContainer.guiceModulesNames == null || GuiceContainer.guiceModulesNames.length == 0)) {
      return;
    }

    if (GuiceContainer.guiceModulesNames != null) {
      for (String moduleName : GuiceContainer.guiceModulesNames) {
        try {
          Class<? extends Module> moduleClass = (Class<? extends Module>) Class.forName(moduleName);
          if (!Module.class.isAssignableFrom(moduleClass)) {
            throw new IllegalArgumentException("Invalid Guice module class [" + moduleName + "]");
          }

          modules.add(moduleClass);
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      }

      for (Class<? extends Module> module : modules) {
        logger.fine("Adding module [" + module + "] from configuration to the Guice injector.");
      }
    }
  }

  private static void addFromClasspath(Set<Class<? extends Module>> modules) {
    ClassClassLoaderResolver<Module> resolver = new ClassClassLoaderResolver<Module>();
    Set<Class<Module>> moduleClasses;
    try {
      moduleClasses = resolver.findByLocators(new ClassClassLoaderResolver.IsA(Module.class), false,
        GuiceContainer.guiceExcludeModules, "guice");
    } catch (IOException e) {
      throw new RuntimeException(e);
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
      Class<?> parent = moduleClass.getSuperclass();
      while (Module.class.isAssignableFrom(parent)) {
        matches.remove(parent);
        parent = parent.getSuperclass();
      }
    }

    for (Class<Module> match : matches) {
      logger.fine("Adding module [" + match + "] from classpath to Guice injector.");
    }

    modules.addAll(matches);
  }


}
