/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security.saved;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.fusionauth.http.HTTPMethod;

/**
 * Stores the request information when a user accesses a resource that requires authentication and needs to be returned
 * to the same resource.
 *
 * @author Brian Pontarelli
 */
public class SavedHttpRequest {
  public HTTPMethod method;

  public Map<String, List<String>> parameters = new HashMap<>();

  public String uri;

  // For Jackson
  public SavedHttpRequest() {
  }

  public SavedHttpRequest(HTTPMethod method, String uri, Map<String, List<String>> parameters) {
    this.method = method;
    this.uri = uri;
    if (parameters != null) {
      this.parameters.putAll(parameters);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SavedHttpRequest that = (SavedHttpRequest) o;
    return method == that.method && Objects.equals(parameters, that.parameters) && Objects.equals(uri, that.uri);
  }

  /**
   * Helper for Jackson to properly convert a String to an HTTPMethod object.
   */
  public String getMethod() {
    return method != null ? method.name() : null;
  }

  /**
   * Helper for Jackson to properly convert a String to an HTTPMethod object.
   */
  public void setMethod(String method) {
    this.method = HTTPMethod.of(method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(method, parameters, uri);
  }
}