/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.log;

import io.fusionauth.http.log.Logger;
import io.fusionauth.http.log.LoggerFactory;

/**
 * Adapts the Java HTTP server logging to SLF4J.
 *
 * @author Brian Pontarelli
 */
public class SLF4JLoggerFactoryAdapter implements LoggerFactory {
  @Override
  public Logger getLogger(Class<?> klass) {
    return new SLF4JLoggerAdapter(org.slf4j.LoggerFactory.getLogger(klass));
  }

  public static class SLF4JLoggerAdapter implements Logger {
    private final org.slf4j.Logger logger;

    public SLF4JLoggerAdapter(org.slf4j.Logger logger) {
      this.logger = logger;
    }

    @Override
    public void debug(String message) {
      logger.debug(message);
    }

    @Override
    public void debug(String message, Object... values) {
      logger.debug(message, values);
    }

    @Override
    public void debug(String message, Throwable throwable) {
      logger.debug(message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
      logger.error(message, throwable);
    }

    @Override
    public void error(String message) {
      logger.error(message);
    }

    @Override
    public void info(String message) {
      logger.info(message);
    }

    @Override
    public void info(String message, Object... values) {
      logger.info(message, values);
    }

    @Override
    public boolean isDebuggable() {
      return logger.isDebugEnabled();
    }

    @Override
    public void trace(String message, Object... values) {
      logger.trace(message, values);
    }

    @Override
    public void trace(String message) {
      logger.trace(message);
    }
  }
}
