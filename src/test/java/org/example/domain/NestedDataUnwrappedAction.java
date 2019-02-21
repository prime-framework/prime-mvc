/*
 * Copyright (c) 2017-2019, Inversoft Inc., All Rights Reserved
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
package org.example.domain;

import org.primeframework.mvc.parameter.annotation.FieldUnwrapped;

/**
 * @author Daniel DeGroff
 */
@org.primeframework.mvc.action.annotation.Action
public class NestedDataUnwrappedAction {

  // @FieldUnwrapped is on a field inside this object
  public Foo bean1 = new Foo();

  // @FieldUnwrapped is on a field inside this object, and this object is null
  public Foo bean2;

  // Top level
  @FieldUnwrapped
  public NestedConfiguration1 bean3 = new NestedConfiguration1();

  public static class Foo {
    @FieldUnwrapped
    public NestedConfiguration1 data1 = new NestedConfiguration1();

    @FieldUnwrapped
    public NestedConfiguration2 data2;
  }

  public static class NestedConfiguration1 {
    // When unwrapped: foo.bar
    public String bar;

    // When unwrapped: foo.baz
    public String baz;
  }

  public static class NestedConfiguration2 {
    // When unwrapped foo.user
    public User user;
  }
}
