/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.util.QueryStringTools;

public class URIBuilder extends QueryStringBuilder {
  private URIBuilder() {
  }

  private URIBuilder(String uri) {
    super(uri);
  }

  public static URIBuilder builder() {
    return new URIBuilder();
  }

  public static URIBuilder builder(String uri) {
    return new URIBuilder(uri);
  }

  @Override
  public URIBuilder uri(String uri) {
    return (URIBuilder) super.uri(uri);
  }

  @Override
  public URIBuilder with(String name, Object value) {
    return (URIBuilder) super.with(name, value);
  }

  @Override
  public URIBuilder with(String name, Consumer<QueryStringBuilder> consumer) {
    return (URIBuilder) super.with(name, consumer);
  }

  /**
   * Add a parameter to the request that you will expect to match the expected. This may be useful if a timestamp oro other random data is returned
   * that is not important to assert on.
   * <p>
   * <strong>This is only intended for use during testing.</strong>
   *
   * @param name the parameter name
   * @return this
   */
  @Override
  public URIBuilder withActual(String name) {
    with(name, "___actual___");
    return this;
  }

  public URIBuilder withConsumer(Consumer<URIBuilder> consumer) {
    if (consumer != null) {
      consumer.accept(this);
    }

    return this;
  }

  /**
   * Add a parameter as optional which means that it is not required to be on the query string, but if it is, the actual value will be used during
   * the assertion.
   * <p>
   * <strong>This is only intended for use during testing.</strong>
   *
   * @param name the parameter name
   * @return this
   */
  public URIBuilder withOptional(String name) {
    with(name, "___optional___");
    return this;
  }

  public URIBuilder withQueryString(String queryString) {
    Map<String, List<String>> parsed = QueryStringTools.parseQueryString(queryString);
    parsed.forEach((name, values) -> values
        .forEach(value -> super.with(name, value)));
    return this;
  }

  @Override
  public URIBuilder withSegment(Object segment) {
    return (URIBuilder) super.withSegment(segment);
  }
}
