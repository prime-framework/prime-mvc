/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.message;

import java.util.*;

/**
 * A simple error message container. This is used when the messages in the MessageStore are converted to JSON. It can
 * also be used in other places, but that is it for now.
 *
 * @author Brian Pontarelli
 */
public class ErrorMessages {
  public final Map<String, List<ErrorMessage>> fieldErrors = new LinkedHashMap<String, List<ErrorMessage>>();

  public final List<ErrorMessage> generalErrors = new LinkedList<ErrorMessage>();

  public ErrorMessages addFieldError(String field, String code, String message) {
    List<ErrorMessage> errorMessages = fieldErrors.get(field);
    if (errorMessages == null) {
      errorMessages = new ArrayList<ErrorMessage>();
      fieldErrors.put(field, errorMessages);
    }

    errorMessages.add(new ErrorMessage(code, message));
    return this;
  }
}
