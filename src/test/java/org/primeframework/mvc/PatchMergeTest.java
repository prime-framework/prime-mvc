/*
 * Copyright (c) 2001-2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * Test RFC 7396 for a basic implementation of patch merge https://tools.ietf.org/html/rfc7396
 *
 * @author Daniel DeGroff
 */
public class PatchMergeTest extends PrimeBaseTest {
  @DataProvider(name = "exampleTestCases")
// https://tools.ietf.org/html/rfc7396#appendix-A
  public Object[][] exampleTestCases() {
// @formatter:off
    return new Object[][]{
//      Original                       Patch                                   Result
//      ------------------------------------------------------------------------------
        // Replace value, string -> string
        {obj("a", "b"),                obj("a", "c"),                          obj("a", "c")},
        // Add key
        {obj("a", "b"),                obj("b", "c"),                          obj("a", "b",
                                                                                   "b", "c")},
        // Remove key
        {obj("a", "b"),                obj("a", null),                         obj()},

        // Remove key
        {obj("a", "b",
             "b", "c"),                obj("a", null),                         obj("b", "c")},
        // Replace value, array -> string
        {obj("a", arr("b")),           obj("a", "c"),                          obj("a", "c")},
        // Replace value, string -> array
        {obj("a", "c"),                obj("a", arr("b")),                     obj("a", arr("b"))},

        // Replace key in inner object, and add remove non-existent key (no-op)
        {obj("a", obj
            ("b", "c")),               obj("a", obj("b", "d", "c", null)),     obj("a", obj("b", "d"))},

        // Replace value, object -> array
        {obj("a", obj
            ("b", "c")),               obj("a", arr(1)),                       obj("a", arr(1))},

        // Replace array values
        {arr("a", "b"),                arr("c", "d"),                          arr("c", "d")},

        // Replace, object -> array
        {obj("a", "b"),                arr("c"),                               arr("c")},

        // Remove object
        {obj("a", "foo"),              null,                                   null},

        // Replace, object -> string
        {obj("a", "foo"),              "bar",                                  "bar"},

        // Add key
        {obj("e",null),                obj("a", 1),                             obj("e", null, "a", 1)},

        // Replace, array -> object
        {arr(1, 2),                    obj("a", "b", "c", null),                obj("a", "b")},

        // Add to empty object
        {obj(),                        obj("a", obj("bb", obj("ccc", null))),  obj("a", obj("bb", obj()))}
    };
// @formatter:on
  }

  @Test(dataProvider = "exampleTestCases", enabled = false)
  public void patchMerge_readerForUpdating(Object o, Object p, Object r) throws Exception {
    // Currently 9 fail, 3 not equal, and 6 error.
    JsonNode original = objectMapper.readTree(serialize(o));
    JsonNode result = objectMapper.readerForUpdating(original).readValue(serialize(p));
    JsonNode expected = objectMapper.readTree(serialize(r));

    assertEquals(result.toPrettyString(), expected.toPrettyString());
  }

  @Test(dataProvider = "exampleTestCases")
  public void patchMerge_rfc7396(Object o, Object p, Object r) throws Exception {
    // Set some configuration overrides so we can serialize the original object and patch correctly.
    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    objectMapper.setSerializationInclusion(Include.ALWAYS);

    // Currently 8 fail, 6 not equal, and 2 error.
    JsonNode original = objectMapper.readTree(serialize(o));
    JsonNode patch = p == null ? null : p instanceof String
        ? JsonNodeFactory.instance.textNode((String) p) : objectMapper.readTree(serialize(p));

    JsonNode result = mergePatch(original, patch);
    JsonNode expected = r == null ? null : r instanceof String
        ? JsonNodeFactory.instance.textNode((String) r) : objectMapper.readTree(serialize(r));

    assertEquals(result, expected,
        "Expected [" + (expected == null ? null : expected.toPrettyString()) + "] but found [" + (result == null ? null : result.toPrettyString()) + "].");
  }

  private List<Object> arr(Object... v) {
    List<Object> result = new ArrayList<>(v.length);
    Collections.addAll(result, v);
    return result;
  }

  // Example built from RFC 7396 Section 2 pseudocode
  private JsonNode mergePatch(JsonNode target, JsonNode patch) {
    if (patch == null) {
      return null;
    }

    if (patch.isObject()) {
      if (target == null || !target.isObject()) {
        target = JsonNodeFactory.instance.objectNode();
      }

      Iterator<String> patchFields = patch.fieldNames();
      while (patchFields.hasNext()) {
        String patchField = patchFields.next();
        JsonNode patchValue = patch.get(patchField);
        if (patchValue.isNull()) {

          JsonNode targetValue = target.get(patchField);
          if (targetValue != null && !targetValue.isMissingNode()) {
            ((ObjectNode) target).remove(patchField);
          }

        } else {
          ((ObjectNode) target).put(patchField, mergePatch(target.get(patchField), patchValue));
        }
      }

      return target;
    } else {
      return patch;
    }
  }

  private Map<String, Object> obj(Object... v) {
    int size = v.length == 0 ? 0 : v.length / 2;
    Map<String, Object> result = new HashMap<>(size);
    for (int i = 0; i < v.length; i = i + 2) {
      result.put(v[i].toString(), v[i + 1]);
    }

    return result;
  }

  private byte[] serialize(Object o) throws JsonProcessingException {
    if (o instanceof Map || o instanceof List) {
      return objectMapper.writeValueAsBytes(o);
    } else {
      return o.toString().getBytes(StandardCharsets.UTF_8);
    }
  }
}