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

  public String methodE;

  public String methodF;

  private String methodB;

  private String methodC;

  private String methodD;

  @NamedParameter(name = "secondMethod")
  public String getMethodB() {
    return methodB;
  }

  public void setMethodB(String method) {
    this.methodB = method;
  }

  public String getSomethingElse1() {
    return methodC;
  }

  public String getSomethingElse2() {
    return methodD;
  }

  public String getSomethingElse3() {
    return methodE;
  }

  public String getSomethingElse4() {
    return methodF;
  }

  public String post() {
    return "success";
  }

  @NamedParameter(name = "setfoo")
  public void setSomething1(String value) {
    methodC = value;
  }

  @NamedParameter(name = "setBar")
  public void setSomething2(String value) {
    methodD = value;
  }

  @NamedParameter(name = "getBaz")
  public void setSomething3(String value) {
    methodE = value;
  }

  @NamedParameter(name = "getboom")
  public void setSomething4(String value) {
    methodF = value;
  }

  @NamedParameter(name = "x-method")
  public void setToken(String method) {
    methodA = method;
  }
}
