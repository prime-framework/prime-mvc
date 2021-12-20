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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A simple error message container. This is used when the messages in the MessageStore are converted to JSON. It can
 * also be used in other places, but that is it for now.
 *
 * @author Brian Pontarelli
 */
public class ErrorMessages {
  public final Map<String, List<ErrorMessage>> fieldErrors = new LinkedHashMap<>();

  public final List<ErrorMessage> generalErrors = new LinkedList<>();

  public ErrorMessages addFieldError(String field, String code, String message, Map<String, Object> data) {
    List<ErrorMessage> errorMessages = fieldErrors.computeIfAbsent(field, k -> new ArrayList<>());
    errorMessages.add(new ErrorMessage(code, message, data));
    return this;
  }
}
