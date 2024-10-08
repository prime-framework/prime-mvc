/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is a test action with fields.
 *
 * @author Brian Pontarelli
 */
@org.primeframework.mvc.action.annotation.Action
public class ActionField extends ParentField {
  public final List<Locale> locales = new ArrayList<>();

  public final List<String> names = new ArrayList<>();

  public final List<ZoneId> timeZones = new ArrayList<>();

  public boolean bar;

  public boolean foo;

  public List<String> list;

  public Map<String, String> map;

  public String reallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyLongFieldName;

  public UserField user;

  public String post() {
    return null;
  }
}
