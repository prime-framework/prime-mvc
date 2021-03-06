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
package org.primeframework.mvc.parameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.util.IteratorEnumeration;

/**
 * This class is an HttpServletRequestWrapper that supports new parameters.
 *
 * @author Brian Pontarelli
 */
public class ParameterHttpServletRequest extends HttpServletRequestWrapper {
  private final Map<String, String[]> parameters;

  public ParameterHttpServletRequest(HttpServletRequest httpServletRequest, Map<String, String[]> parameters) {
    super(httpServletRequest);
    this.parameters = parameters;
  }

  public String getParameter(String key) {
    if (parameters != null && parameters.containsKey(key) && parameters.get(key) != null) {
      return parameters.get(key)[0];
    }

    return super.getParameter(key);
  }

  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> complete = new HashMap<>();
    if (parameters != null) {
      complete.putAll(parameters);
    }

    complete.putAll(super.getParameterMap());

    return complete;
  }

  @SuppressWarnings("unchecked")
  public Enumeration<String> getParameterNames() {
    Set<String> names = new HashSet<>();
    if (parameters != null) {
      names.addAll(parameters.keySet());
    }

    names.addAll(super.getParameterMap().keySet());

    return new IteratorEnumeration<>(names.iterator());
  }

  public String[] getParameterValues(String key) {
    if (parameters != null && parameters.containsKey(key) && parameters.get(key) != null) {
      return parameters.get(key);
    }

    return super.getParameterValues(key);
  }
}
