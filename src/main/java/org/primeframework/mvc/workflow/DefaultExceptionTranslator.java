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
package org.primeframework.mvc.workflow;

import java.util.Set;

import org.primeframework.mvc.ErrorException;

import com.google.inject.Inject;

/**
 * Translates exceptions.
 *
 * @author Brian Pontarelli
 */
public class DefaultExceptionTranslator implements ExceptionTranslator {
  private final Set<Class<? extends RuntimeException>> handlableExceptions;

  @Inject
  public DefaultExceptionTranslator(Set<Class<? extends RuntimeException>> handlableExceptions) {
    this.handlableExceptions = handlableExceptions;
  }

  /**
   * If the exception is an {@link ErrorException} this returns the result code. Otherwise, it returns "error".
   *
   * @param e The exceptions.
   * @return The result code.
   */
  @Override
  public String translate(RuntimeException e) {
    if (handlableExceptions.contains(e.getClass())) {
      return (e instanceof ErrorException) ? ((ErrorException) e).resultCode : "error";
    }

    return null;
  }
}
