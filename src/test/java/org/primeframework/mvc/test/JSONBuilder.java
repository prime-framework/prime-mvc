/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.primeframework.mvc.test.RequestResult.ThrowingBiConsumer;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;
import static org.testng.Assert.fail;

/**
 * @author Daniel DeGroff
 */
public class JSONBuilder {
  public ObjectMapper objectMapper;

  public ObjectNode root;

  public JSONBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public JSONBuilder(JsonNode root) {
    this.root = (ObjectNode) root;
    objectMapper = new ObjectMapper();
  }

  public JSONBuilder add(String field, Object value) throws Exception {
    return addValue(field,
        (node, name) -> node.put(name, value.toString()),
        (node) -> node.add(value.toString()));
  }

  public JSONBuilder add(String field, List<String> value) throws Exception {
    return addValue(field,
        (node, name) -> {
          ArrayNode array = node.putArray(name);
          value.forEach(array::add);
        },
        (node) -> value.forEach(node::add));
  }

  public JSONBuilder add(String field, Map<String, Object> value) throws Exception {
    byte[] bytes = objectMapper.writeValueAsBytes(value);
    JsonNode valueNode = objectMapper.readTree(bytes);
    return addValue(field,
        (node, name) -> node.putObject(name).setAll((ObjectNode) valueNode),
        (node) -> node.add(valueNode));
  }

  public JSONBuilder add(String field, boolean value) throws Exception {
    return addValue(field,
        (node, name) -> node.put(name, value),
        node -> node.add(value));
  }

  public JSONBuilder add(String field, long value) throws Exception {
    return addValue(field,
        (node, name) -> node.put(name, value),
        node -> node.add(value));
  }

  public JSONBuilder add(String field, int value) throws Exception {
    return addValue(field,
        (node, name) -> node.put(name, value),
        node -> node.add(value));
  }

  public String build() throws JsonProcessingException {
    return objectMapper.writeValueAsString(root);
  }

  public JSONBuilder remove(String... fields) {
    for (String field : fields) {

      JSONPointer pointer = parseFieldName(field);
      JsonNode node = root.at(pointer.parent);

      if (node.isObject()) {
        ((ObjectNode) node).remove(pointer.field);
      } else if (node.isArray()) {
        int index = Integer.parseInt(pointer.field);
        ((ArrayNode) node).remove(index);
      }
    }

    return this;
  }

  public JSONBuilder withJSON(Map<String, Object> json) throws IOException {
    byte[] bytes = objectMapper.writeValueAsBytes(json);
    root = (ObjectNode) objectMapper.readTree(bytes);
    return this;
  }

  public JSONBuilder withJSON(String json) throws IOException {
    root = (ObjectNode) objectMapper.readTree(json);
    return this;
  }

  public JSONBuilder withJSONFile(Path jsonFile, Object... values) throws IOException {
    root = (ObjectNode) objectMapper.readTree(BodyTools.processTemplate(jsonFile, values));
    return this;
  }

  private JSONBuilder addValue(String field, ThrowingBiConsumer<ObjectNode, String> objectConsumer,
                               ThrowingConsumer<ArrayNode> arrayConsumer) throws Exception {

    JSONPointer pointer = parseFieldName(field);

    // Create the path if it does not yet exist so we can create nexted values.
    createNestedObjectPaths(pointer);

    JsonNode node = root.at(pointer.parent);

    JsonNode child = node.path(pointer.field);

    // If the field name is an array, we are adding to it I think? I think will have to do replace to modify the entire array
    if (child instanceof ArrayNode arrayNode) {
      arrayConsumer.accept(arrayNode);
      return this;
    }

    if (node.isMissingNode()) {
      fail("Node not found. [" + field + "]");
    }

    if (node instanceof ObjectNode objectNode) {
      objectConsumer.accept(objectNode, pointer.field);
    } else if (node instanceof ArrayNode arrayNode) {
      arrayConsumer.accept(arrayNode);
    } else {
      throw new UnsupportedOperationException("Not expecting this. Node is [" + node.getClass().getSimpleName() + "]");
    }

    return this;
  }

  private void createNestedObjectPaths(JSONPointer pointer) {
    if (pointer.parent == null || pointer.parent.equals("")) {
      return;
    }

    String path = "/";
    JsonNode working = root;
    for (String part : pointer.parent.substring(1).split("/")) {
      path = path + (path.endsWith("/") ? "" : "/") + part;
      if (root.at(path).isMissingNode()) {
        if (working instanceof ObjectNode objectNode) {
          objectNode.set(part, JsonNodeFactory.instance.objectNode());
        } else {
          throw new UnsupportedOperationException("Not expecting this. Node is [" + working.getClass().getSimpleName() + "]");
        }
      } else {
        working = root.at(path);
      }
    }
  }

  private JSONPointer parseFieldName(String field) {
    String pointer = "/" + field.replaceAll("\\.", "/").replaceAll("\\[(.*?)\\]", "/$1");
    String fieldName = pointer.substring(pointer.lastIndexOf("/") + 1);
    return new JSONPointer(fieldName, pointer.substring(0, pointer.indexOf(fieldName) - 1));
  }

  public static class JSONPointer {
    public String field;

    public String parent;

    public JSONPointer(String field, String parent) {
      this.field = field;
      this.parent = parent;
    }
  }
}
