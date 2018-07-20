/*
 * Copyright (c) 2018, Inversoft Inc., All Rights Reserved
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
package org.example.action.views.entry;

import org.example.domain.Entry;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;


/**
 * Test using a view in the {@link JSONResponse}.
 *
 * @author Daniel DeGroff
 */
@Action
@JSON
public class NoViewDefinedAction {
  // No view defined on the @JSONResponse but all fields in the domain object are annotated for a particular view
  @JSONResponse
  public Entry entry;

  public String get() {
    entry = new Entry();
    return "success";
  }
}
