/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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
package org.example.action.patch;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel DeGroff
 */
public class PatchActionRequest {
  public CoolObject data;

  public static class Address {
    public String city;

    public String state;

    public String street;

    public String type;

    public int zipCode;
  }

  public static class CoolObject {
    public List<Address> addresses;

    public Map<String, String> attributes;

    public String config;

    public String email;

    public String name;

    public List<String> preferences;

    public String type;
  }
}

