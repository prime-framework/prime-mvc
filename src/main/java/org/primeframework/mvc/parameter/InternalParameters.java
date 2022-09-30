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
package org.primeframework.mvc.parameter;

import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.PrimeException;
import static org.primeframework.mvc.security.csrf.CSRFProvider.CSRF_PARAMETER_KEY;

/**
 * This class handles all of the parameters that control the Prime MVC internal behavior like validation, result
 * execution, action execution, etc.
 *
 * @author Brian Pontarelli
 */
public final class InternalParameters {
  /**
   * HTTP request parameter or scoped attribute from the request that indicates if the result should be executed or not.
   * By default the result is always executed, but this can be used to suppress that behavior.
   */
  public static final String EXECUTE_RESULT = "primeExecuteResult";

  /**
   * HTTP request parameter or scoped attribute from the request that indicates if validation should be executed or not.
   * By default the validation is always executed, but this can be used to suppress that behavior.
   */
  public static final String EXECUTE_VALIDATION = "primeExecuteValidation";


  /**
   * Determines if the key given is true or false. The key must be one of the statics defined on this class and the
   * request parameters and request scope are checked, in that order.
   *
   * @param request The request to look in.
   * @param key     The key to check.
   * @return True of false. If the key doesn't exist in the request, this returns true. If it does exist in the request
   *     and is equal to {@code true}, this returns true. Otherwise, this returns false.
   */
  public static boolean is(HTTPRequest request, String key) {
    if (!isInternalParameter(key)) {
      throw new PrimeException("Invalid key [" + key + "]");
    }

    Object value = request.getParameter(key);
    if (value == null) {
      value = request.getAttribute(key);
    }

    String str = value == null ? null : value.toString().toLowerCase();
    if (str != null && !isValidBoolean(str)) {
      throw new PrimeException("Invalid value [" + str + "] for key [" + key + "]. Must be either the string [true] or [false].");
    }

    return str == null || str.equals("true");
  }

  /**
   * Determines if the given key is an internal parameter.
   *
   * @param key The key.
   * @return True if it is, false otherwise.
   */
  public static boolean isInternalParameter(String key) {
    return key.equals(EXECUTE_RESULT) || key.equals(EXECUTE_VALIDATION) || key.equals(CSRF_PARAMETER_KEY);
  }

  private static boolean isValidBoolean(String str) {
    return str != null && (str.equals("true") || str.equals("false"));
  }
}
