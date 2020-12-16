/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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
package org.example.action.patch;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.servlet.HTTPMethod;

/**
 * @author Daniel DeGroff
 */
@Action
@JSON
@Status(code = "input", status = 500)
public class TestAction {
  public static PatchData FinalState = new PatchData();

  public static PatchData InitialState = new PatchData();

  @JSONRequest
  public PatchData request = new PatchData();

  @JSONResponse
  public PatchData response;

  public String patch() {
    put();
    // Set the final state
    FinalState = request;
    return "success";
  }

  @PreParameterMethod(httpMethods = {HTTPMethod.PATCH})
  public void patchFetch() {
    request = InitialState;
  }

  public String post() {
    return put();
  }

  public String put() {
    response = new PatchData();
    response = request;
    return "success";
  }
}
