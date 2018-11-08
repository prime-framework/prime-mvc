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
package org.example.action;

import java.util.UUID;

import org.example.domain.GenericBean.FinalHardTypeOne;
import org.example.domain.GenericBean.FinalHardTypeTwo;
import org.example.domain.GenericBean.TypedObject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;

@Action
public class GenericsAction {
  public static final UUID oneId = UUID.fromString("49e0f299-a2b0-4439-b0d5-3e2cc8949675");

  public static final UUID twoId = UUID.fromString("eee47c8b-4134-4c4d-ab28-cacaeed84cdb");

  @PreParameter
  public String type;

  public TypedObject<?, ?, ?> typedObject;

  public String post() {
    return "success";
  }

  @PreParameterMethod
  public void setupTypedObject() {
    if (type.equals("one")) {
      typedObject = new FinalHardTypeOne();
    } else {
      typedObject = new FinalHardTypeTwo();
    }
  }
}
