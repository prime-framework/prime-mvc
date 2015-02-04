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
package org.example.action;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

/**
 * This is a simple test action.
 *
 * @author Brian Pontarelli
 */
@Action
@Forward(code = "success")
public class ExtensionInheritance extends Extension {
  public boolean invoked = false;

  public String post() {
    invoked = true;
    return "child";
  }

  @PostParameterMethod
  public void method2() {

  }

  @PostParameterMethod
  public void method3() {

  }
}
