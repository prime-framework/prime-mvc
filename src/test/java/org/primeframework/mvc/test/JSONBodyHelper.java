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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static org.testng.Assert.fail;

/**
 * @author Daniel DeGroff
 */
public class JSONBodyHelper {
  public ObjectMapper objectMapper;

  public ObjectNode root;

  public JSONBodyHelper(JsonNode root) {
    this.root = (ObjectNode) root;
    objectMapper = new ObjectMapper();
  }

  public JSONBodyHelper add(String field, Object value) {
    return addValue(field, (node, name) -> node.put(name, value.toString()));
  }

  public JSONBodyHelper add(String field, List<String> value) {
    return addValue(field, (node, name) -> {
      ArrayNode array = node.putArray(name);
      for (String s : value) {
        array.add(s);
      }
    });
  }

  public JSONBodyHelper add(String field, Map<String, Object> value) {
    return addValue(field, (node, name) -> {
      try {
        ObjectNode object = node.putObject(name);
        byte[] bytes = objectMapper.writeValueAsBytes(value);
        JsonNode valueNode = objectMapper.readTree(bytes);
        object.setAll((ObjectNode) valueNode);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  public JSONBodyHelper add(String field, boolean value) {
    return addValue(field, (node, name) -> node.put(name, value));
  }

  public JSONBodyHelper add(String field, long value) {
    return addValue(field, (node, name) -> node.put(name, value));
  }

  public JSONBodyHelper add(String field, int value) {
    return addValue(field, (node, name) -> node.put(name, value));
  }

  public JSONBodyHelper remove(String... fields) {
    for (String field : fields) {
      List<Object> tokens = new ArrayList<>();
      String path = "";
      JsonNode node = root;


      for (String token : field.split("\\.")) {
        int bracket = token.indexOf("[");
        if (bracket == -1) {
          tokens.add(token);
        } else {
          if (bracket != -1) {
            String s = token.substring(bracket + 1, token.lastIndexOf("]"));
            tokens.add(token.substring(0, bracket));
            tokens.add(Integer.parseInt(s));
          }
        }
      }

      for (int i = 0; i < tokens.size(); i++) {
        Object token = tokens.get(i);
        if (i == tokens.size() - 1) {
          if (token instanceof String) {
            if (node instanceof ObjectNode on) {
              on.remove((String) token);
              return this;
            } else if (node instanceof ArrayNode an) {
              throw new UnsupportedOperationException("Whoops, we don't support this yet.");
            }
          } else {
            // Assuming this is an integer index
            if (node instanceof ArrayNode an) {
              an.remove((Integer) token);
            }
          }

        } else {
          node = node.get((String) token);
          path = path.equals("") ? (String) token : (path + "." + token);
          if (node.isMissingNode()) {
            fail("A node could not be found at [" + path + "]");
          }
        }
      }
    }

    return this;
  }

  public JSONBodyHelper replace(String pointer, String value) {

    return this;
  }

  private JSONBodyHelper addValue(String field, BiConsumer<ObjectNode, String> consumer) {
    String[] parts = field.split("\\.");
    String path = "";
    JsonNode node = root;

    for (int i = 0; i < parts.length; i++) {
      if (i == parts.length - 1) {
        if (node instanceof ObjectNode on) {
          consumer.accept(on, parts[i]);
        } else if (node instanceof ArrayNode an) {
          throw new UnsupportedOperationException("Whoops, we don't support this yet.");
        }
      } else {
        node = node.get(parts[i]);
        path = path.equals("") ? parts[i] : (path + "." + parts[i]);
        if (node.isMissingNode()) {
          fail("A node could not be found at [" + path + "]");
        }
      }
    }

    return this;
  }

  private ObjectNode traverseToObject(String field) {
    String[] parts = field.split("\\.");
    String path = "";
    JsonNode node = root;

    for (int i = 0; i < parts.length; i++) {
      if (i == parts.length - 1) {
        if (node instanceof ObjectNode on) {
          return on;
        } else if (node instanceof ArrayNode an) {
          throw new UnsupportedOperationException("Whoops, we don't support this yet.");
        }
      } else {
        node = node.get(parts[i]);
        path = path.equals("") ? parts[i] : (path + "." + parts[i]);
        if (node.isMissingNode()) {
          fail("A node could not be found at [" + path + "]");
        }
      }
    }

    return null;
  }
}
