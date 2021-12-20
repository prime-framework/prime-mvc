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
package org.example.action;

import org.primeframework.mvc.http.HTTPResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.google.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.NoOp;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;

/**
 * @author Daniel DeGroff
 */
@Action
@Forward(code = "forward")
@JSON(code = "json")
@NoOp(code = "noop")
public class PreRenderMethodAction {
  public static boolean forwardCalled;

  public static boolean jsonCalled;

  public static boolean noopCalled;

  private final HTTPResponse response;

  @JSONResponse
  public String json;

  public String result;

  @Inject
  public PreRenderMethodAction(HTTPResponse response) {
    this.response = response;
  }

  public String get() {
    return result;
  }

  @PreRenderMethod(Forward.class)
  public void preFoward() {
    forwardCalled = true;
  }

  @PreRenderMethod(JSON.class)
  public void preJSON() {
    jsonCalled = true;
    json = "trust me it is json";
  }

  @PreRenderMethod(NoOp.class)
  public void preNoop() throws IOException {
    noopCalled = true;
    response.setStatus(201);
    response.setContentType("application/potato");
    OutputStream os = response.getOutputStream();
    byte[] bytes = "You've been no-oped!".getBytes(StandardCharsets.UTF_8);
    response.setContentLength((long) bytes.length);
    os.write(bytes);
    os.flush();
  }
}
