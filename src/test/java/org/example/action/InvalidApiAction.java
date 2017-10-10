/*
 * Copyright (c) 2013-2016, Inversoft Inc., All Rights Reserved
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

import org.example.domain.UserField;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

/**
 * This is a simple test action that has an invalid URL parameter in the @Action annotation.
 *
 * @author Brian Pontarelli
 */
@Action("{id}")
@JSON
public class InvalidApiAction {
  public ActionType action;

  @JSONRequest
  @JSONResponse
  public UserField user;

  public String uuid;

  public String get() {
    return "success";
  }

  public enum ActionType {
    ADD,
    EDIT
  }
}
