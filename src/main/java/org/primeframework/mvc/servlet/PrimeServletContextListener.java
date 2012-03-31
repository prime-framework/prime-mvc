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
package org.primeframework.mvc.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.primeframework.mvc.guice.GuiceBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * This class bootstraps the Prime by creating a Guice injector and putting it in the ServletContext.
 *
 * @author Brian Pontarelli
 */
public class PrimeServletContextListener implements ServletContextListener {
  private static final Logger logger = LoggerFactory.getLogger(PrimeServletContextListener.class);
  public static final String GUICE_INJECTOR_KEY = "guiceInjector";

  /**
   * Initialize the ServletContext into the {@link ServletObjectsHolder} and initializes Guice.
   *
   * @param event The event to get the ServletContext from.
   */
  public void contextInitialized(ServletContextEvent event) {
    logger.info("Initializing Prime");
    ServletContext context = event.getServletContext();
    ServletObjectsHolder.setServletContext(context);

    // Start guice and set it into the servlet context
    context.setAttribute(GUICE_INJECTOR_KEY, GuiceBootstrap.initialize());
  }

  /**
   * Shuts down the GuiceBootstrap.
   *
   * @param event Not used.
   */
  public void contextDestroyed(ServletContextEvent event) {
    ServletContext context = event.getServletContext();
    Injector injector = (Injector) context.getAttribute(GUICE_INJECTOR_KEY);
    GuiceBootstrap.shutdown(injector);
  }
}
