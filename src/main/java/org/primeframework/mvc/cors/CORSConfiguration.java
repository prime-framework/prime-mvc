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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.primeframework.mvc.http.HTTPMethod;
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

  public void normalize() {
    Set<HTTPMethod> methods = new HashSet<>(allowedMethods);
    allowedMethods.clear();
    allowedMethods.addAll(methods);
  }
}