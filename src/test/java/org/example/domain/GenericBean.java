/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * For testing odd generic cases.
 */
public class GenericBean {
  public MapSubclass mapSubclass;

  public MapImplements mapImplements;

  public TypedObject<?, ?, ?> typedObject;

  // S = String
  // T = ParameterType*
  // U = String
  // V = Not used
  public abstract static class BaseTypedObject<S, T, U, V> {
    public Map<UUID, T> mapOfTypes = new HashMap<>();

    public Map<V, T> fullyGenericMapOfTypes = new HashMap<>();

    public List<S> listOfStrings = new ArrayList<>();

    public List<T> listOfTypes = new ArrayList<>();

    public U singleString;

    public T singleType;
  }

  public static class FinalHardTypeOne extends HardTypeOne {
  }

  public static class FinalHardTypeTwo extends HardTypeTwo {
  }

  public static class HardTypeOne extends MovedTypedObject<String, String, ParameterTypeOne> {
  }

  public static class HardTypeTwo extends MovedTypedObject<String, String, ParameterTypeTwo> {
  }

  public abstract static class MovedTypedObject<B, C, A> extends TypedObject<A, B, C> {
  }

  public static class ParameterTypeOne {
    public String one;
  }

  public static class ParameterTypeTwo {
    public String two;
  }

  public abstract static class TypedObject<S, T, U> extends BaseTypedObject<U, S, T, UUID> {
  }
}
