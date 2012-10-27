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
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;
import org.primeframework.mvc.validation.jsr303.Validation;

/**
 * Action for testing the PreValidtionMethod and PostValidationMethod annotations
 *
 * @author Troy Hill
 */
@Action
public class ValidationMethods {

  public boolean preValidation = false;
  public boolean postValidation = false;

  @PreValidationMethod
  public void toggleOn() {
    this.preValidation = true;
  }

  @PostValidationMethod
  public void toggleOff() {
    this.postValidation = true;
  }

  public String get() {
    return "success";
  }

  @Validation(enabled = false)
  public String post() {
    return null;
  }
}