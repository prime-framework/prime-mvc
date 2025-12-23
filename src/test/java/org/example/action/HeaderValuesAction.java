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

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

/**
 * Copy all the header values into a JSON response.
 * @author Brent Halsey
 */
@Action
@JSON
public class HeaderValuesAction {
  private final HTTPRequest httpRequest;

  @JSONResponse
  public Map<String, Object> headers = new HashMap<>();

  @Inject
  public HeaderValuesAction(HTTPRequest httpRequest) {
    this.httpRequest = httpRequest;
  }

  public String get() {
    headers.putAll(httpRequest.getHeaders());
    return "success";
  }

  public String post() {
    headers.putAll(httpRequest.getHeaders());
    return "success";
  }
}
