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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This class provides a JSON generator that uses the builder pattern.
 *
 * @author Brian Pontarelli
 */
public class JSONBuilder {
  public static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  private final Deque<Boolean> firstField = new ArrayDeque<Boolean>();
  private final StringBuilder build;
  private final boolean pretty;

  public JSONBuilder(StringBuilder build, boolean pretty) {
    this.build = build;
    this.pretty = pretty;
    firstField.push(true);
  }

  /**
   * Starts a JSON object with no name. You need to call {@link #endObject()} when you have finished adding
   * everything to this object.
   *
   * @return This JSONBuilder.
   */
  public JSONBuilder startObject() {
    addComma();
    indent();
    build.append("{");
    if (pretty) {
      build.append("\n");
    }
    firstField.push(true);
    return this;
  }

  /**
   * Starts a JSON object with the given name. You need to call {@link #endObject()} when you have finished adding
   * everything to this object.
   *
   * @param name The name of the object.
   * @return This JSONBuilder.
   */
  public JSONBuilder startObject(String name) {
    addComma();
    indent();
    appendString(name);
    build.append(":{");
    if (pretty) {
      build.append("\n");
    }
    firstField.push(true);
    return this;
  }

  /**
   * Starts a JSON array. You need to call {@link #endArray()} when you have finished adding everything to this array.
   *
   * @param name The name of the array inside the current JSON object.
   * @return This JSONBuilder.
   */
  public JSONBuilder startArray(String name) {
    addComma();
    indent();
    appendString(name);
    build.append(":[");
    if (pretty) {
      build.append("\n");
    }
    firstField.push(true);
    return this;
  }

  /**
   * Adds a String field to the current JSON object.
   *
   * @param name  The name of the field.
   * @param value The value of the field.
   * @return This JSONBuilder.
   */
  public JSONBuilder addStringField(String name, String value) {
    if (value == null) {
      return this;
    }

    addComma();
    indent();
    appendString(name);
    build.append(":");
    appendString(value);
    return this;
  }

  /**
   * Adds an int field to the current JSON object.
   *
   * @param name  The name of the field.
   * @param value The value of the field.
   * @return This JSONBuilder.
   */
  public JSONBuilder addIntField(String name, Integer value) {
    if (value == null) {
      return this;
    }

    addComma();
    indent();
    appendString(name);
    build.append(":").append(Integer.toString(value));
    return this;
  }

  /**
   * Adds the given enum as an int field using the enums ordinal value.
   *
   * @param name  The field name.
   * @param value The enum.
   * @return This JSONBuilder.
   */
  public JSONBuilder addEnumOrdinalField(String name, Enum<?> value) {
    if (value == null) {
      return this;
    }

    addIntField(name, value.ordinal());
    return this;
  }

  /**
   * Adds the given boolean field..
   *
   * @param name  The field name.
   * @param value The value.
   * @return This JSONBuilder.
   */
  public JSONBuilder addBooleanField(String name, Boolean value) {
    if (value == null) {
      return this;
    }

    addComma();
    indent();
    appendString(name);
    build.append(":").append(value);
    return this;
  }

  /**
   * Ends the current JSON object.
   *
   * @return This JSONBuilder
   */
  public JSONBuilder endObject() {
    firstField.pop();
    if (pretty) {
      build.append("\n");
      indent();
    }
    build.append("}");
    return this;
  }

  /**
   * Ends the current JSON array.
   *
   * @return This JSONBuilder
   */
  public JSONBuilder endArray() {
    firstField.pop();
    if (pretty) {
      build.append("\n");
      indent();
    }
    build.append("]");
    return this;
  }

  private void indent() {
    if (!pretty) {
      return;
    }

    for (int i = 1; i < firstField.size(); i++) {
      build.append("  ");
    }
  }

  private void addComma() {
    if (!firstField.peek()) {
      build.append(",");
      if (pretty) {
        build.append("\n");
      }
    } else {
      firstField.pop();
      firstField.push(false);
    }
  }

  private void appendString(String str) {
    build.append("\"");
    escape(str, build);
    build.append("\"");
  }

  public static String escape(String str) {
    StringBuilder build = new StringBuilder();
    escape(str, build);
    return build.toString();
  }

  public static void escape(String str, StringBuilder build) {
    char[] ca = str.toCharArray();
    for (char c : ca) {
      switch (c) {
        case '"':
        case '\\':
          build.append('\\');
          build.append(c);
          break;
        case '\r':
          build.append("\\r");
          break;
        case '\n':
          build.append("\\n");
          break;
        case '\t':
          build.append("\\t");
          break;
        case '\b':
          build.append("\\b");
          break;
        case '\f':
          build.append("\\f");
          break;
        case '/':
          build.append("\\/");
          break;
        default:
          if (c <= 0x1F) {
            unicodeEscape(c, build);
          } else {
            build.append(c);
          }
      }
    }
  }

  public static void unicodeEscape(int ch, StringBuilder build) {
    build.append('\\');
    build.append('u');
    build.append(HEX_CHARS[ch >>> 12]);
    build.append(HEX_CHARS[(ch >>> 8) & 0xf]);
    build.append(HEX_CHARS[(ch >>> 4) & 0xf]);
    build.append(HEX_CHARS[ch & 0xf]);
  }

  /**
   * @return The JSON.
   */
  @Override
  public String toString() {
    return build.toString();
  }
}
