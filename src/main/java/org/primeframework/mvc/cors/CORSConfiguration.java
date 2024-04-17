/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.cors;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.fusionauth.http.HTTPMethod;
import org.primeframework.mvc.util.Buildable;

/**
 * @author Trevor Smith
 */
public class CORSConfiguration implements Buildable<CORSConfiguration> {
  public boolean allowCredentials;

  public List<String> allowedHeaders = new ArrayList<>();

  public List<HTTPMethod> allowedMethods = new ArrayList<>();

  public List<URI> allowedOrigins = new ArrayList<>();

  public boolean debug;

  public Pattern excludedPathPattern;

  public Pattern includedPathPattern;

  public Predicate<String> excludeUriPredicate;

  public Predicate<String> includeUriPredicate;

  public List<String> exposedHeaders = new ArrayList<>();

  public int preflightMaxAgeInSeconds;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    CORSConfiguration that = (CORSConfiguration) o;
    return allowCredentials == that.allowCredentials &&
        debug == that.debug &&
        preflightMaxAgeInSeconds == that.preflightMaxAgeInSeconds &&
        Objects.equals(allowedHeaders, that.allowedHeaders) &&
        Objects.equals(allowedMethods, that.allowedMethods) &&
        Objects.equals(allowedOrigins, that.allowedOrigins) &&
        Objects.equals(exposedHeaders, that.exposedHeaders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), allowCredentials, allowedHeaders, allowedMethods, allowedOrigins, debug, exposedHeaders, preflightMaxAgeInSeconds);
  }

  public CORSConfiguration withAllowCredentials(boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
    return this;
  }

  public CORSConfiguration withAllowedHeaders(String... headers) {
    this.allowedHeaders.clear();
    this.allowedHeaders.addAll(List.of(headers));
    return this;
  }

  public CORSConfiguration withAllowedMethods(HTTPMethod... methods) {
    this.allowedMethods.clear();
    this.allowedMethods.addAll(List.of(methods));
    return this;
  }

  public CORSConfiguration withAllowedOrigins(URI... origins) {
    this.allowedOrigins.clear();
    this.allowedOrigins.addAll(List.of(origins));
    return this;
  }

  public CORSConfiguration withDebug(boolean debug) {
    this.debug = debug;
    return this;
  }

  private void checkExclusiveMatching() {
    if (excludedPathPattern != null && includedPathPattern != null) {
      throw new IllegalStateException("You cannot use both withExcludedPathPattern and withIncludedPathPattern. You must use one or the other.");
    }
    if (includeUriPredicate != null && excludeUriPredicate != null) {
      throw new IllegalStateException("You cannot use both withIncludeUriPredicate and withExcludeUriPredicate. You must use one or the other.");
    }
    if ((includeUriPredicate != null ^ excludeUriPredicate != null) &&
        (excludedPathPattern != null ^ includedPathPattern != null)) {
      throw new IllegalStateException("You cannot use both a path (withIncludedPathPattern/withExcludedPathPattern) and predicate based (withIncludeUriPredicate/withExcludeUriPredicate). You must use one or the other.");
    }
  }

  public CORSConfiguration withExcludedPathPattern(Pattern pattern) {
    this.excludedPathPattern = pattern;
    checkExclusiveMatching();
    return this;
  }

  public CORSConfiguration withIncludedPathPattern(Pattern pattern) {
    this.includedPathPattern = pattern;
    checkExclusiveMatching();
    return this;
  }

  public CORSConfiguration withExcludeUriPredicate(Predicate<String> excludeUriPredicate) {
    this.excludeUriPredicate = excludeUriPredicate;
    checkExclusiveMatching();
    return this;
  }

  public CORSConfiguration withIncludeUriPredicate(Predicate<String> includeUriPredicate) {
    this.includeUriPredicate = includeUriPredicate;
    checkExclusiveMatching();
    return this;
  }

  public CORSConfiguration withExposedHeaders(String... headers) {
    this.exposedHeaders.clear();
    this.exposedHeaders.addAll(List.of(headers));
    return this;
  }

  public CORSConfiguration withPreflightMaxAgeInSeconds(int maxAge) {
    this.preflightMaxAgeInSeconds = maxAge;
    return this;
  }
}
