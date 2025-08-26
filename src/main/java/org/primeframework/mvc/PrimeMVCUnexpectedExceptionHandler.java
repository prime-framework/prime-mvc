/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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

import io.fusionauth.http.log.Logger;
import io.fusionauth.http.server.HTTPUnexpectedExceptionHandler;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel DeGroff
 */
public class PrimeMVCUnexpectedExceptionHandler implements HTTPUnexpectedExceptionHandler {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PrimeMVCUnexpectedExceptionHandler.class);

  @Override
  public int handle(Logger unused, Throwable t) {
    // TODO : Review : I don't love this : I could just use the provided logger as well.
    //        In practice I probably shouldn't pass in a logger, because it will be difficult to know
    //        what logger by package naming you need to adjust for logging levels.
    //        It gets a little tricky in the Default impl in java-http since I don't want to have to inject anything.
    logger.error("Error encountered", t);
    return 500;
  }
}
