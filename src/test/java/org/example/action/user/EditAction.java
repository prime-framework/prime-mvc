/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.example.action.user;

import org.example.domain.User;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;


/**
 * This class is a simple edit action for testing.
 *
 * @author Brian Pontarelli
 */
@Action("{id}")
@Forward
public class EditAction {
  public static final SomeEnum[] values = SomeEnum.values();

  public static boolean getCalled;

  public SomeEnum enumValue = SomeEnum.VALUE1;

  public boolean formPrepared = false;

  public String id;

  public boolean preParameter = false;

  public User user;

  public String execute() {
    return "success";
  }

  @FormPrepareMethod
  public void formPrepare() {
    formPrepared = true;
  }

  public String get() {
    getCalled = true;
    return "success";
  }

  public String post() {
    return "success";
  }

  @PreParameterMethod
  public void prepare() {
    preParameter = true;
  }
}