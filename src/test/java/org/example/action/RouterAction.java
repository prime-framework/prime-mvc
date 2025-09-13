/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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

import org.example.action.store.BaseStoreAction;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Status;

/**
 * @author Daniel DeGroff
 */
@Action("{action}/{counter}")
@Redirect(code = "redirect", uri = "/router/redirect/${counter}")
@Status(code = "stop")
@Forward.List({
    @Forward(code = "submit-form", page = "/submit-form.ftl"),
    @Forward(code = "meta-refresh", page = "/meta-refresh.ftl")
})
public class RouterAction extends BaseStoreAction {
  public String action;

  public int counter;

  public String uri;

  public String get() {
    if ("redirect".equals(action)) {
      counter++;
      return counter < 6
          ? "redirect"
          : "stop";
    }

    uri = "/router/redirect/" + counter;

    if ("submit-form".equals(action)) {
      return "submit-form";
    }

    if ("meta-refresh".equals(action)) {
      return "meta-refresh";
    }

    throw new IllegalStateException();
  }
}
