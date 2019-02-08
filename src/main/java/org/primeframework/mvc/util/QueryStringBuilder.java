/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.primeframework.mvc.NotImplementedException;

/**
 * @author Daniel DeGroff
 */
public class QueryStringBuilder {
  private final StringBuilder sb = new StringBuilder();

  private String uri;

  private boolean addSeparator;

  protected QueryStringBuilder() {
  }

  protected QueryStringBuilder(String uri) {
    this.uri = uri;
    if (uri != null) {
      if (uri.contains("?") && !uri.endsWith("&")) {
        addSeparator = true;
      }
    }
  }

  public static QueryStringBuilder builder() {
    return new QueryStringBuilder();
  }

  public static QueryStringBuilder builder(String uri) {
    return new QueryStringBuilder(uri);
  }

  public QueryStringBuilder withActual(String name) {
    throw new NotImplementedException();
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

  @Override
  public String toString() {
    if (uri == null) {
      return sb.toString();
    }

    // URL provided contains a ?, perhaps other parameters as well
    if (uri.contains("?")) {
      if (sb.indexOf("?") == 0) {
        return uri + sb.substring(1);
      }
      return uri + sb.toString();
    }

    if (sb.indexOf("?") == 0) {
      return uri + sb.toString();
    }

    if (sb.indexOf("#") == 0) {
      return uri + sb.toString();
    }

    if (uri.contains("#") && sb.length() > 0) {
      return uri + "&" + sb.toString();
    }

    return uri + "?" + sb.toString();
  }

  public QueryStringBuilder with(String name, Object value) {
    if (value == null) {
      return this;
    }

    if (addSeparator) {
      sb.append("&");
    }

    try {
      sb.append(name).append("=").append(URLEncoder.encode(value.toString(), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // Uh, oh, UTF-8 is no longer supported.
      throw new RuntimeException(e);
    }

    addSeparator = true;
    return this;
  }
}
