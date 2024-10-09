/*
 * Copyright (c) 2019-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Daniel DeGroff
 */
public class QueryStringBuilder {
  private final StringBuilder sb = new StringBuilder();

  private final List<String> segments = new ArrayList<>();

  private final StringBuilder uri = new StringBuilder();

  private boolean addSeparator;

  protected QueryStringBuilder() {
  }

  protected QueryStringBuilder(String uri) {
    uri(uri);
  }

  public static QueryStringBuilder builder() {
    return new QueryStringBuilder();
  }

  public static QueryStringBuilder builder(String uri) {
    return new QueryStringBuilder(uri);
  }

  public QueryStringBuilder beginFragment() {
    // If query string contains no terms, remove the question mark
    if (sb.toString().endsWith("?")) {
      sb.setLength(sb.length() - 1);
    }

    if (sb.indexOf("#") == -1) {
      sb.append("#");
      addSeparator = false;
    } else {
      char lastChar = sb.charAt(sb.length() - 1);
      addSeparator = lastChar != '#' && lastChar != '&';
    }
    return this;
  }

  public QueryStringBuilder beginQuery() {
    if (sb.indexOf("#") != -1) {
      throw new IllegalStateException("You cannot add a query after a fragment");
    }

    if (sb.indexOf("?") == -1) {
      sb.append("?");
      addSeparator = false;
    } else {
      char lastChar = sb.charAt(sb.length() - 1);
      addSeparator = lastChar != '?' && lastChar != '&';
    }
    return this;
  }

  public String build() {
    if (uri.isEmpty()) {
      return sb.toString();
    }

    if (segments.size() > 0) {
      if (uri.lastIndexOf("/") != uri.length() - 1) {
        uri.append("/");
      }

      uri.append(String.join("/", segments));
    }

    if ((sb.indexOf("?") == 0 || sb.indexOf("#") == 0) && sb.length() > 1) {
      return uri.append(sb).toString();
    }

    if (sb.isEmpty() || sb.toString().equals("?") || sb.toString().equals("#")) {
      return uri.toString();
    }

    return uri.append("?").append(sb).toString();
  }

  public QueryStringBuilder ifFalse(boolean test, Runnable runnable) {
    if (!test) {
      runnable.run();
    }

    return this;
  }

  public QueryStringBuilder ifFalse(boolean test, Consumer<QueryStringBuilder> consumer) {
    if (!test) {
      consumer.accept(this);
    }

    return this;
  }

  public QueryStringBuilder ifTrue(boolean test, Runnable runnable) {
    if (test) {
      runnable.run();

    }

    return this;
  }

  public QueryStringBuilder ifTrue(boolean test, Consumer<QueryStringBuilder> consumer) {
    if (test) {
      consumer.accept(this);
    }

    return this;
  }

  public QueryStringBuilder uri(String uri) {
    if (!this.uri.isEmpty()) {
      throw new IllegalStateException("Object has already been initialized with a URL");
    }

    if (uri != null) {
      String del = null;
      if (uri.contains("?")) {
        del = "?";
      } else if (uri.contains("#")) {
        del = "#";
      }

      if (del != null) {
        this.uri.append(uri, 0, uri.indexOf(del));
        this.sb.append(uri, uri.indexOf(del), uri.length());
      } else {
        this.uri.append(uri);
      }

      if ((uri.contains("?") || uri.contains("#")) && !List.of('#', '?', '&').contains(uri.charAt(uri.length() - 1))) {
        addSeparator = true;
      }
    }
    return this;
  }

  public QueryStringBuilder with(String name, Consumer<QueryStringBuilder> consumer) {
    QueryStringBuilder b = new QueryStringBuilder();
    consumer.accept(b);
    return with(name, b);
  }

  public QueryStringBuilder with(String name, Object value) {
    if (value == null) {
      return this;
    }

    if (addSeparator) {
      sb.append("&");
    }

    sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));

    addSeparator = true;
    return this;
  }

  public QueryStringBuilder withActual(String name) {
    throw new UnsupportedOperationException();
  }

  public QueryStringBuilder withSegment(Object segment) {
    String message = "You cannot add a URL segment after you have appended a %s to the end of the URL";

    if (!sb.isEmpty() && (sb.indexOf("?") != -1)) {
      throw new IllegalStateException(String.format(message, "?"));
    }
    if (!sb.isEmpty() && (sb.indexOf("#") != -1)) {
      throw new IllegalStateException(String.format(message, "#"));
    }

    if (segment != null) {
      segments.add(segment.toString());
    }

    return this;
  }
}
