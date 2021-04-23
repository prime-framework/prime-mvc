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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel DeGroff
 */
public class JSONRequestBuilderTest {
  public Map<String, Object> json = Map.of(
      "user", Map.of(
          "email", "erlich@piedpiper.com",
          "mobilePhone", "555-555-5555"));

  private JSONRequestBuilder handler;

  private ObjectMapper objectMapper;

  private JsonNode root;

  @BeforeMethod
  public void beforeMethod() throws IOException {
    objectMapper = new ObjectMapper();
    root = objectMapper.readTree(objectMapper.writeValueAsBytes(json));
    handler = new JSONRequestBuilder(root);
  }

  @Test
  public void test() throws Exception {
    // Remove a string node
    assertTrue(handler.remove("user.email")
        .root.at("/user/email").isMissingNode(), handler.root.toString());

    // Add a string
    handler.add("user.email", "erlich@piedpiper.com");
    assertEquals(handler.root.at("/user/email").asText(), "erlich@piedpiper.com");

    // Add a number
    handler.add("user.favoriteNumber", 42);
    assertEquals(handler.root.at("/user/favoriteNumber").asInt(), 42);

    // Add a boolean
    handler.add("user.active", true);
    assertEquals(handler.root.at("/user/active").asBoolean(), true);

    // Add an array
    handler.add("user.roles", List.of("admin", "user"));
    assertTrue(handler.root.at("/user/roles").isArray(), handler.root.toString());
    assertEquals(handler.root.at("/user/roles/0").asText(), "admin", handler.root.toString());
    assertEquals(handler.root.at("/user/roles/1").asText(), "user", handler.root.toString());

    // Add a value to an array
    handler.add("user.roles", "manager");
    assertTrue(handler.root.at("/user/roles").isArray(), handler.root.toString());
    assertEquals(handler.root.at("/user/roles/0").asText(), "admin", handler.root.toString());
    assertEquals(handler.root.at("/user/roles/1").asText(), "user", handler.root.toString());
    assertEquals(handler.root.at("/user/roles/2").asText(), "manager", handler.root.toString());

    // Add an object
    handler.add("user.data", Map.of("foo", "bar"));
    assertTrue(handler.root.at("/user/data").isObject(), handler.root.toString());
    assertEquals(handler.root.at("/user/data/foo").asText(), "bar", handler.root.toString());

    // Remove a value from an array
    handler.remove("user.roles[1]");
    assertEquals(handler.root.at("/user/roles/0").asText(), "admin", handler.root.toString());
    assertEquals(handler.root.at("/user/roles/1").asText(), "manager", handler.root.toString());
    assertTrue(handler.root.at("/user/roles/2").isMissingNode());
  }
}
