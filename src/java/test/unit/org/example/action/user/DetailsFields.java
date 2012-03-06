/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.example.action.user;

import org.example.domain.AddressField;
import org.example.domain.UserField;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.annotation.Valid;

/**
 * This class is a simple edit action for testing.
 *
 * @author Brian Pontarelli
 */
@Action
public class DetailsFields {
  @Valid
  public UserField user;

  public String execute() {
    user = new UserField();
    user.age = 12;
    user.name = "Frank";
    user.securityQuestions = new String[]{"One", "Two"};
    user.addresses.put("home", new AddressField());
    user.addresses.get("home").street = "123 Main St.";
    user.addresses.get("home").city = "Springfield";
    user.addresses.get("home").state = "IL";
    user.addresses.get("home").zipcode = "00000";
    return "success";
  }
}