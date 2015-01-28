/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.config;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.util.ClassClasspathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class loads the configuration by scanning the classpath for packages and action classes.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class DefaultActionConfigurationProvider implements ActionConfigurationProvider {
  private static final Logger logger = LoggerFactory.getLogger(DefaultActionConfigurationProvider.class);
  public static final String ACTION_CONFIGURATION_KEY = "primeActionConfiguration";
  private final ServletContext context;

  @Inject
  public DefaultActionConfigurationProvider(ServletContext context, ActionConfigurationBuilder builder) {
    this.context = context;

    ClassClasspathResolver<?> resolver = new ClassClasspathResolver<>();
    Set<? extends Class<?>> actionClassses;
    try {
      actionClassses = resolver.findByLocators(new ClassClasspathResolver.AnnotatedWith(Action.class), true, null, "action");
    } catch (IOException e) {
      throw new PrimeException("Error discovering action classes", e);
    }

    Map<String, ActionConfiguration> actionConfigurations = new HashMap<>();
    for (Class<?> actionClass : actionClassses) {
      // Only accept classes loaded by the ClassLoader for Prime. This prevents classes loaded by parent loader from
      // being included as available Actions. One situation that this can occur: A jar with Actions (Prime) is in the classpath
      // of a Java program, and that program starts up an embedded web server that includes prime-mvc. When the embedded web server
      // initializes prime-mvc it will locate the actions in the jar outside the war file.
      if ( ! inClassLoaderOrParentClassLoader(Action.class.getClassLoader(), actionClass)) {
        continue;
      }

      ActionConfiguration actionConfiguration = builder.build(actionClass);
      String uri = actionConfiguration.uri;

      if (actionConfigurations.containsKey(uri)) {
        boolean previousOverrideable = actionConfigurations.get(uri).actionClass.getAnnotation(Action.class).overridable();
        boolean thisOverrideable = actionClass.getAnnotation(Action.class).overridable();
        if ((previousOverrideable && thisOverrideable) || (!previousOverrideable && !thisOverrideable)) {
          throw new PrimeException("Duplicate action found for URI [" + uri + "]. The first action class found was [" +
            actionConfigurations.get(uri).actionClass + "]. The second action class found was [" + actionClass + "]. Either " +
            "both classes are marked as overridable or neither is marked as overridable. You can fix this by only " +
            "marking one of the classes with the overridable flag on the Action annotation.");
        } else if (previousOverrideable) {
          actionConfigurations.put(uri, actionConfiguration);
        }
      } else {
        actionConfigurations.put(uri, actionConfiguration);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Added action configuration for [" + actionClass + "] and the uri [" + uri + "]");
      }
    }

    context.setAttribute(ACTION_CONFIGURATION_KEY, actionConfigurations);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ActionConfiguration lookup(String uri) {
    Map<String, ActionConfiguration> configuration = (Map<String, ActionConfiguration>) context.getAttribute(ACTION_CONFIGURATION_KEY);
    if (configuration == null) {
      return null;
    }

    return configuration.get(uri);
  }

  /**
   * Return true if the {@code actionClass} is loaded by {@code classLoader} or one of it's descendant {@link ClassLoader}
   *
   * @param classLoader the ClassLoader
   * @param actionClass the Class to test
   * @return true if actionClass was loaded by classLoader or one one of its children
   */
  private boolean inClassLoaderOrParentClassLoader(ClassLoader classLoader, Class<?> actionClass) {
    ClassLoader actionClassClassLoader = actionClass.getClassLoader();
    while (actionClassClassLoader != null) {
      if (classLoader.equals(actionClassClassLoader)) {
        return true;
      }
      actionClassClassLoader = actionClassClassLoader.getParent();
    }
    return false;
  }
}