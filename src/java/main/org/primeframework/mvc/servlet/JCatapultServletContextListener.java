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
package org.primeframework.mvc.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

import org.primeframework.mvc.guice.GuiceContainer;

/**
 * <p> This class bootstraps the entire JCatapult system. This must be defined as the first servlet context listener
 * that is initialized otherwise JCatapult might fail to startup correctly. </p>
 *
 * @author Brian Pontarelli
 */
public class JCatapultServletContextListener implements ServletContextListener {
  private static final Logger logger = Logger.getLogger(JCatapultServletContextListener.class.getName());

  /**
   * Initialize the ServletContext into the {@link ServletObjectsHolder} and initializes Guice.
   *
   * @param event The event to get the ServletContext from.
   */
  public void contextInitialized(ServletContextEvent event) {
    logger.info("Initializing JCatapult");
    ServletObjectsHolder.setServletContext(event.getServletContext());

    // setup guice and set it into the servlet context
    initGuice();
  }

  /**
   * Initialize the {@link GuiceContainer}.
   */
  protected void initGuice() {
    GuiceContainer.inject();
    GuiceContainer.initialize();
  }

  /**
   * Shuts down the GuiceContainer.
   *
   * @param event Not used.
   */
  public void contextDestroyed(ServletContextEvent event) {
    GuiceContainer.shutdown();
  }
}
