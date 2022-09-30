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
  public MapImplements mapImplements;

  public MapSubclass mapSubclass;

  public MapSubclassSubclass mapSubclassSubclass;

  public TypedObject<?, ?, ?> typedObject;

  // S = String
  // T = ParameterType*
  // U = String
  // V = Not used
  public abstract static class BaseTypedObject<S, T, U, V> {
    public Map<V, T> fullyGenericMapOfTypes = new HashMap<>();

    public List<S> listOfStrings = new ArrayList<>();

    public List<T> listOfTypes = new ArrayList<>();

    public Map<UUID, T> mapOfTypes = new HashMap<>();

    public U singleString;

    public T singleType;

    private Map<V, T> privateFullyGenericMapOfTypes = new HashMap<>();

    private List<S> privateListOfStrings = new ArrayList<>();

    private List<T> privateListOfTypes = new ArrayList<>();

    private Map<UUID, T> privateMapOfTypes = new HashMap<>();

    private U privateSingleString;

    private T privateSingleType;

    public Map<V, T> getPrivateFullyGenericMapOfTypes() {
      return privateFullyGenericMapOfTypes;
    }

    public void setPrivateFullyGenericMapOfTypes(Map<V, T> privateFullyGenericMapOfTypes) {
      this.privateFullyGenericMapOfTypes = privateFullyGenericMapOfTypes;
    }

    public List<S> getPrivateListOfStrings() {
      return privateListOfStrings;
    }

    public void setPrivateListOfStrings(List<S> privateListOfStrings) {
      this.privateListOfStrings = privateListOfStrings;
    }

    public List<T> getPrivateListOfTypes() {
      return privateListOfTypes;
    }

    public void setPrivateListOfTypes(List<T> privateListOfTypes) {
      this.privateListOfTypes = privateListOfTypes;
    }

    public Map<UUID, T> getPrivateMapOfTypes() {
      return privateMapOfTypes;
    }

    public void setPrivateMapOfTypes(Map<UUID, T> privateMapOfTypes) {
      this.privateMapOfTypes = privateMapOfTypes;
    }

    public U getPrivateSingleString() {
      return privateSingleString;
    }

    public void setPrivateSingleString(U privateSingleString) {
      this.privateSingleString = privateSingleString;
    }

    public T getPrivateSingleType() {
      return privateSingleType;
    }

    public void setPrivateSingleType(T privateSingleType) {
      this.privateSingleType = privateSingleType;
    }
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
