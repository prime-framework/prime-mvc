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
import org.primeframework.mvc.action.result.annotation.Status;

/**
 * This is a simple test action.
 *
 * @author Rob Davis
 */
@Action(baseURI = "/OverrideMe")
@Status
public class OverrideMeAction {
  // Default behavior would be to parse this into /override-me.
  // The classURI allows to override that behavior to match the class name as is.
  public static boolean invoked = false;

  public String execute() {
    invoked = true;
    return "success";
  }
}
