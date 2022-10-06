/*
 * Copyright (c) 2017-2018, Inversoft Inc., All Rights Reserved
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
package org.example.action.scope;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

/**
 * This action does NOT take the URL segment and sets it to 'id' field.
 *
 * @author Daniel DeGroff
 */
@Action
public class PageTwoAction {
  public String formPrepareMethodCalled;

  public String postParameterMethodCalled;

  public String searchText;

  public String searchType;

  @FormPrepareMethod
  public void formPrepareMethod() {
    if (postParameterMethodCalled == null) {
      formPrepareMethodCalled = "first";
    } else {
      formPrepareMethodCalled = "second";
    }
  }

  public String get() {
    return "input";
  }

  public String post() {
    return "success";
  }

  @PostParameterMethod
  public void postParameterMethod() {
    if (formPrepareMethodCalled == null) {
      postParameterMethodCalled = "first";
    } else {
      postParameterMethodCalled = "second";
    }
  }
}
