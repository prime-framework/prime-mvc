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
package org.example.action;

import org.example.domain.UserField;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.content.json.annotation.JSONRequest;

/**
 * This is a simple test action.
 *
 * @author Brian Pontarelli
 */
@Action
@Forward(contentType = "application/json")
public class Api {
  public ActionType action;

  @JSONRequest
  public UserField user;

  public String post() {
    return "success";
  }

  public static enum ActionType {
    ADD,
    EDIT
  }
}
