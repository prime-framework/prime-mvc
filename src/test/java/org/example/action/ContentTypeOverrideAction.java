/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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

import org.example.domain.Entry;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

/**
 * This is a simple test action.
 *
 * @author Rob Davis
 */
@Action

@JSON.List({
    @JSON(contentType = "application/json+scim"),
    @JSON(code = "error", contentType = "application/json+error", status = 400)
})
public class ContentTypeOverrideAction {
  @JSONResponse
  public Entry response;

  public String status;

  public String execute() {
    response = new Entry();

    if (status != null && status.equals("400")) {
      return "error";
    }

    return "success";
  }
}
