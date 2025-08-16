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
package org.example.action.meta;

import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;

/**
 * @author Daniel DeGroff
 */
@Action
@Forward.List({
    @Forward(code = "input-lc", page = "/meta/refresh-lc.ftl"),
    @Forward(code = "input-uc", page = "/meta/refresh-uc.ftl")
})
public class RefreshAction {
  public String test = "lc";

  public String get() {
    if (test == null || !(test.equals("lc") || test.equals("uc"))) {
      throw new ErrorException("error");
    }

    return "input-" + test;
  }
}
