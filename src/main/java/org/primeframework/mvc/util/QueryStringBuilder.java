/*
 * Copyright (c) 2019-2023, Inversoft Inc., All Rights Reserved
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
    sb.append("#");
    addSeparator = false;
    return this;
  }

  public QueryStringBuilder beginQuery() {
    sb.append("?");
    addSeparator = false;
    return this;
  }

  public String build() {
    if (uri.length() == 0) {
      return sb.toString();
    }

    // URL provided contains a ?, perhaps other parameters as well
    if (uri.indexOf("?") != -1) {
      if (sb.indexOf("?") == 0) {
        return uri + sb.substring(1);
      }
      return uri.append(sb).toString();
    } else {
      if (segments.size() > 0) {
        if (uri.lastIndexOf("/") != uri.length() - 1) {
          uri.append("/");
        }

        uri.append(String.join("/", segments));
      }
    }

    if (sb.indexOf("?") == 0) {
      return uri.append(sb).toString();
    }

    if (sb.indexOf("#") == 0) {
      return uri.append(sb).toString();
    }

    if (uri.indexOf("#") != -1 && sb.length() > 0) {
      return uri.append("&").append(sb).toString();
    }

    if (sb.length() == 0) {
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
    if (this.uri.length() > 0) {
      throw new IllegalStateException("Object has already been initialized with a URL");
    }

    if (uri != null) {
      this.uri.append(uri);
      if (uri.contains("?") && !uri.endsWith("&")) {
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

    sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8))
      .append("=")
      .append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));

    addSeparator = true;
    return this;
  }

  public QueryStringBuilder withActual(String name) {
    throw new UnsupportedOperationException();
  }

  public QueryStringBuilder withSegment(Object segment) {
    if (uri.length() > 0 && (uri.indexOf("?") == uri.length() - 1)) {
      throw new IllegalStateException("You cannot add a URL segment after you have appended a ? to the end of the URL");
    }

    if (segment != null) {
      segments.add(segment.toString());
    }

    return this;
  }
}
