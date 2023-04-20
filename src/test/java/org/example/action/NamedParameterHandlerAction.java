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
package org.example.action;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.NamedParameter;

/**
 * Get named parameters that don't follow bean spec
 *
 * @author Daniel DeGroff
 */
@Action
public class NamedParameterHandlerAction {
  @NamedParameter(name = "x-field")
  public String fieldA;

  @NamedParameter(name = "secondField")
  public String fieldB;

  public String methodA;

  private String methodB;


  @NamedParameter(name = "x-method")
  public void setToken(String method) {
    methodA = method;
  }

  @NamedParameter(name = "secondMethod")
  public String getMethodB() {
    return methodB;
  }

  public void setMethodB(String method) {
    this.methodB = method;
  }

  public String post() {
    return "success";
  }
}
