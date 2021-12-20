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

import org.example.domain.AddressField;
import org.example.domain.UserField;
import org.primeframework.mvc.action.annotation.Action;

/**
 * This is a simple test action.
 *
 * @author Brian Pontarelli
 */
@Action
public class PostAction {
  public static boolean invoked = false;

  public UserField user;

  public String post() {
    invoked = true;
    user = new UserField();
    user.age = 35;
    user.name = "Brian Pontarelli";
    user.addresses.put("home", new AddressField());
    user.addresses.get("home").city = "Broomfield";
    user.addresses.get("home").state = "CO";
    return "success";
  }
}
