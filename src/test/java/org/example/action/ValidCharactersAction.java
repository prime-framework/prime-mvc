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


import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

/**
 * @author Daniel DeGroff
 */
@Action("{path}")
@Redirect(uri = "${uri}")
public class ValidCharactersAction {
  public String path;

  public String q;

  public String uri;

  public String get() {
    uri = "/";

    if (path != null) {
      uri += path;
    }

    if (q != null) {
      uri += "q=" + q;
    }
    return "success";
  }
}
