/*
 * Copyright (c) 2019-2020, Inversoft Inc., All Rights Reserved
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
package org.example.action.freemarker;

import org.primeframework.mvc.action.annotation.Action;

/**
 * @author Tyler Scott
 */
@Action
public class EscapeAction {
  public String listSeparator = ",\u0020";

  public String mode = "message";

  public String selectionRequired = "Select\u2026";

  public String warning = "<p>Are you sure?</p>";

  public String welcome = "Hello, to access your account go to <a href=\"https://foo.com\">foo.com</a>.";

  public String get() {
    return "success";
  }
}
