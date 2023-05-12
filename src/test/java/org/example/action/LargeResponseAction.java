/*
 * Copyright (c) 2001-2023, Inversoft Inc., All Rights Reserved
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

import java.util.concurrent.atomic.AtomicInteger;

import org.example.domain.AddressField;
import org.example.domain.UserField;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

/**
 * This is an action that returns a huge JSON response.
 *
 * @author Brian Pontarelli
 */
@Action
@JSON
public class LargeResponseAction {
  public final static AtomicInteger count = new AtomicInteger(0);

  @JSONRequest
  @JSONResponse
  public UserField user;

  public String get() {
    user = new UserField();

    int bound = count.incrementAndGet();
    for (int i = 0; i < bound; i++) {
      AddressField address = new AddressField();
      address.city = "city" + i;
      address.state = "state" + i;
      address.street = "street" + i;
      address.zipcode = "zip" + i;
      user.addresses.put("address" + i, address);
    }

    return "success";
  }
}
