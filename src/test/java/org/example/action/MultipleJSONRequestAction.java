/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.servlet.HTTPMethod;

/**
 * Multiple JSON Request fields
 *
 * @author Daniel DeGroff
 */
@Action
@Status.List({
    @Status(code = "success", status = 200),
    @Status(code = "foo", status = 201),
    @Status(code = "bar", status = 202)
})
public class MultipleJSONRequestAction {
  @JSONRequest(httpMethods = {HTTPMethod.DELETE})
  public Object bar;

  @JSONRequest(httpMethods = {HTTPMethod.POST})
  public Object foo;

  public String delete() {
    if (bar != null) {
      return "bar";
    }

    return "success";
  }

  public String post() {
    if (foo != null) {
      return "foo";
    }

    return "success";
  }
}
