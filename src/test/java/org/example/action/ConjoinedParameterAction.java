/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.example.action;

import java.util.List;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.ConjoinedRequestParameters;

/**
 * @author Daniel DeGroff
 */
@Action("{urlSegment}")
public class ConjoinedParameterAction {
  public String param1;

  public boolean param2;

  public int param3;

  public List<String> param4;

  public String urlSegment;

  // Use the default parameter name
  @ConjoinedRequestParameters
  public String get() {
    return "input";
  }

  @ConjoinedRequestParameters(value = "conjoined")
  public String post() {
    return "input";
  }
}
