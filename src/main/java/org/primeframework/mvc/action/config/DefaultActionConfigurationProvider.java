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

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.util.ClassClasspathResolver;
import org.primeframework.mvc.util.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class loads the configuration by scanning the classpath for packages and action classes.
 *
 * @author Brian Pontarelli
 */
@Singleton
@SuppressWarnings("unchecked")
public class DefaultActionConfigurationProvider implements ActionConfigurationProvider {
  private static final Logger logger = LoggerFactory.getLogger(DefaultActionConfigurationProvider.class);
  public static final String ACTION_CONFIGURATION_KEY = "primeActionConfiguration";
  private final ServletContext context;

  @Inject
  public DefaultActionConfigurationProvider(ServletContext context, URIBuilder uriBuilder) {
    this.context = context;

    ClassClasspathResolver resolver = new ClassClasspathResolver();
    Set<Class<?>> actionClassses;
    try {
      actionClassses = resolver.findByLocators(new ClassClasspathResolver.AnnotatedWith(Action.class), true, null, "action");
    } catch (IOException e) {
      throw new PrimeException("Error discovering action classes", e);
    }

    Map<String, ActionConfiguration> configuration = new HashMap<String, ActionConfiguration>();
    for (Class<?> actionClass : actionClassses) {
      String uri = uriBuilder.build(actionClass);
      ActionConfiguration actionConfiguration = new DefaultActionConfiguration(actionClass, uri);

      if (configuration.containsKey(uri)) {
        boolean previousOverrideable = configuration.get(uri).actionClass().getAnnotation(Action.class).overridable();
        boolean thisOverrideable = actionClass.getAnnotation(Action.class).overridable();
        if ((previousOverrideable && thisOverrideable) || (!previousOverrideable && !thisOverrideable)) {
          throw new PrimeException("Duplicate action found for URI [" + uri + "]. The " +
            "first action class found was [" + configuration.get(uri).actionClass() + "]. " +
            "The second action class found was [" + actionClass + "]. Either both classes " +
            "are marked as overridable or neither is marked as overridable. You can fix " +
            "this by only marking one of the classes with the overridable flag on the " +
            "Action annotation.");
        } else if (previousOverrideable) {
          configuration.put(uri, actionConfiguration);
        }
      } else {
        configuration.put(uri, actionConfiguration);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Added action configuration for [" + actionClass + "] and the uri [" + uri + "]");
      }
    }

    context.setAttribute(ACTION_CONFIGURATION_KEY, configuration);
  }

  /**
   * {@inheritDoc}
   */
  public ActionConfiguration lookup(String uri) {
    Map<String, ActionConfiguration> configuration = (Map<String, ActionConfiguration>) context.getAttribute(ACTION_CONFIGURATION_KEY);
    if (configuration == null) {
      return null;
    }

    return configuration.get(uri);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, ActionConfiguration> knownConfiguration() {
    return (Map<String, ActionConfiguration>) context.getAttribute(ACTION_CONFIGURATION_KEY);
  }
}