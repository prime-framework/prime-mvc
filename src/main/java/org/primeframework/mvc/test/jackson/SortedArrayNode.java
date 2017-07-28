/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * A sorted implementation of ArrayNode used for testing assertions.
 *
 * @author Daniel DeGroff
 */
public class SortedArrayNode extends ArrayNode {
  public SortedArrayNode(JsonNodeFactory nf) {
    super(nf);
  }

  public SortedArrayNode(JsonNodeFactory nf, int capacity) {
    super(nf, capacity);
  }

  @SuppressWarnings("unused")
  public SortedArrayNode(JsonNodeFactory nf, List<JsonNode> children) {
    super(nf, children);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (o instanceof ArrayNode) {
      ArrayNode other = (ArrayNode) o;
      if (other.size() != size()) {
        return false;
      }

      if (other.size() == 0) {
        return true;
      }

      // If this collection doesn't contain text nodes, delegate to super
      if (!other.get(0).isTextual()) {
        return super.equals(o);
      }

      ArrayNode sortedNode1 = sort(this);
      ArrayNode sortedNode2 = sort(other);

      return sortedNode1.equals(sortedNode2);
    }
    return false;
  }

  private ArrayNode sort(ArrayNode node) {
    List<String> sorted = new ArrayList<>();
    node.elements().forEachRemaining(n -> sorted.add(n.asText()));
    Collections.sort(sorted);

    ArrayNode sortedNode = JsonNodeFactory.instance.arrayNode();
    sorted.forEach(s -> sortedNode.add(JsonNodeFactory.instance.textNode(s)));
    return sortedNode;
  }
}
