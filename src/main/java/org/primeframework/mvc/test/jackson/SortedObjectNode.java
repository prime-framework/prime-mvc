/*
 * Copyright (c) 2018, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test.jackson;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A sorted implementation of ObjectNode used for testing assertions.
 *
 * @author Daniel DeGroff
 */
public class SortedObjectNode extends ObjectNode {
  public SortedObjectNode(JsonNodeFactory nf) {
    super(nf);
  }

  public SortedObjectNode(JsonNodeFactory nf, Map<String, JsonNode> kids) {
    super(nf, kids);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (o instanceof ObjectNode) {
      ObjectNode other = (ObjectNode) o;
      if (other.size() != size()) {
        return false;
      }

      if (other.size() == 0) {
        return true;
      }

      sort(this);
      sort(other);

      return super.equals(o);
    }
    return false;
  }

  private void sort(ObjectNode node) {
    Map<String, JsonNode> sorted = new TreeMap<>();
    node.fields().forEachRemaining(entry -> sorted.put(entry.getKey(), entry.getValue()));

    _children.clear();
    sorted.forEach(this::set);
  }
}
