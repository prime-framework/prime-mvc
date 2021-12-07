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

import java.util.Map;

/**
 * A simple error message class.
 *
 * @author Brian Pontarelli
 */
public class ErrorMessage {
  public String code;

  public Map<String, Object> data;

  public String message;

  public ErrorMessage() {
  }

  public ErrorMessage(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public ErrorMessage(String code, String message, Map<String, Object> data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }
}
