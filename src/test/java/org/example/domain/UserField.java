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
package org.example.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is a test user with fields.
 *
 * @author Brian Pontarelli
 */
public class UserField {
  public boolean active;
  public Map<String, AddressField> addresses = new HashMap<String, AddressField>();
  public Integer age;
  public Integer favoriteMonth;
  public Integer favoriteYear;
  public Integer id;
  public Map<Integer, Integer> ids = new HashMap<Integer, Integer>();
  public String lifeStory;
  public Locale locale;
  public String name;
  public String password;
  public String[] securityQuestions;
  public List<UserField> siblings = new ArrayList<UserField>();
  public UserType type;

  public UserField() {
  }

  public UserField(String name) {
    this.name = name;
  }
}
