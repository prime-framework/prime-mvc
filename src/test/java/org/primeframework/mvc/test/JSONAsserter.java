/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class JSONAsserter {
  public String errorMessage;

  public JsonNode node;

  public Integer size;

  public JSONAsserter(JsonNode node) {
    this.node = node;
  }

  public JSONAsserter assertLength(int expected) {
    if (node.size() != expected) {
      errorMessage = "Expected an array of size [" + expected + "] but found [" + node.size() + "]";
    }

    return this;
  }

  public JSONAsserter assertType(JsonNodeType expected) {
    if (node.getNodeType() != expected) {
      errorMessage = "Expected node to be of type [" + expected + "] but found [" + node.getNodeType() + "]";
    }

    return this;
  }
}
