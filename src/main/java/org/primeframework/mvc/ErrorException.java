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
package org.primeframework.mvc;

/**
 * Base class for exceptions that cause Prime to invoke the error workflow.
 *
 * @author Brian Pontarelli
 */
public class ErrorException extends PrimeException {

  /**
   * The prime response result code
   */
  public String resultCode;

  /**
   * The arguments passed to the message template
   */
  public Object[] args;

  public ErrorException(Throwable cause, Object... args) {
    this(null, cause, args);
  }

  public ErrorException(Object... args) {
    this(null, null, args);
  }

  public ErrorException(String resultCode) {
    this(resultCode, null, (Object[])null);
  }

  public ErrorException(String resultCode, Object... args) {
    this(resultCode, null, args);
  }

  public ErrorException(String resultCode, Throwable cause, Object... args) {
    super(cause);
    this.resultCode = resultCode;
    this.args = args;
  }
}
