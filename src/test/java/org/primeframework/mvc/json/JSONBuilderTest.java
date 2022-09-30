/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.json;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the JSONBuilder
 *
 * @author Brian Pontarelli
 */
public class JSONBuilderTest {
  @Test
  public void punctuation() {
    StringBuilder build = new StringBuilder();
    String json = new JSONBuilder(build, false).
        startObject().
        startObject("add").
        startObject("doc").
        addStringField("id", "id").
        addStringField("content", "\"hey there\"").
        addIntField("number", 1).
        endObject().
        endObject().
        endObject().
        toString();

    assertEquals(json,
        "{" +
            "\"add\":{" +
            "\"doc\":{" +
            "\"id\":\"id\"," +
            "\"content\":\"\\\"hey there\\\"\"," +
            "\"number\":1" +
            "}" +
            "}" +
            "}");
  }

  @Test
  public void simplePretty() {
    StringBuilder build = new StringBuilder();
    String json = new JSONBuilder(build, true).
        startObject().
        startObject("add").
        startObject("doc").
        addStringField("id", "foo").
        addStringField("content", "bar").
        addIntField("number", 1).
        addIntField("numberNull", null).
        addEnumOrdinalField("enum", Status.ACTIVE).
        addEnumOrdinalField("enumNull", null).
        addBooleanField("boolean", true).
        addBooleanField("booleanNull", null).
        endObject().
        startArray("array").
        startObject().
        addStringField("id", "foo").
        addStringField("content", "bar").
        addIntField("number", 1).
        endObject().
        startObject().
        addStringField("id", "foo").
        addStringField("content", "bar").
        addIntField("number", 1).
        endObject().
        endArray().
        endObject().
        endObject().
        toString();

    assertEquals(json,
        "{\n" +
            "  \"add\":{\n" +
            "    \"doc\":{\n" +
            "      \"id\":\"foo\",\n" +
            "      \"content\":\"bar\",\n" +
            "      \"number\":1,\n" +
            "      \"enum\":0,\n" +
            "      \"boolean\":true\n" +
            "    },\n" +
            "    \"array\":[\n" +
            "      {\n" +
            "        \"id\":\"foo\",\n" +
            "        \"content\":\"bar\",\n" +
            "        \"number\":1\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\":\"foo\",\n" +
            "        \"content\":\"bar\",\n" +
            "        \"number\":1\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}");
  }

  @Test
  public void simpleUgly() {
    StringBuilder build = new StringBuilder();
    String json = new JSONBuilder(build, false).
        startObject().
        startObject("add").
        startObject("doc").
        addStringField("id", "foo").
        addStringField("content", "bar").
        addIntField("number", 1).
        endObject().
        startArray("array").
        startObject().
        addStringField("id", "foo").
        addStringField("content", "bar").
        addIntField("number", 1).
        endObject().
        startObject().
        addStringField("id", "foo").
        addStringField("content", "bar").
        addIntField("number", 1).
        endObject().
        endArray().
        endObject().
        endObject().
        toString();

    assertEquals(json,
        "{" +
            "\"add\":{" +
            "\"doc\":{" +
            "\"id\":\"foo\"," +
            "\"content\":\"bar\"," +
            "\"number\":1" +
            "}," +
            "\"array\":[" +
            "{" +
            "\"id\":\"foo\"," +
            "\"content\":\"bar\"," +
            "\"number\":1" +
            "}," +
            "{" +
            "\"id\":\"foo\"," +
            "\"content\":\"bar\"," +
            "\"number\":1" +
            "}" +
            "]" +
            "}" +
            "}");
  }

  @Test
  public void unicode() {
    StringBuilder build = new StringBuilder();
    String json = new JSONBuilder(build, false).
        startObject().
        startObject("add").
        startObject("doc").
        addStringField("id", "\u00c3bc").
        addStringField("content", "bar").
        addIntField("number", 1).
        endObject().
        endObject().
        endObject().
        toString();

    assertEquals(json,
        "{" +
            "\"add\":{" +
            "\"doc\":{" +
            "\"id\":\"\u00c3bc\"," +
            "\"content\":\"bar\"," +
            "\"number\":1" +
            "}" +
            "}" +
            "}");
  }

  public enum Status {
    ACTIVE
  }
}
