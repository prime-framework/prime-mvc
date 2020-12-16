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
import java.util.Objects;

/**
 * @author Daniel DeGroff
 */
public class PatchData {
  public Integer integer;

  public List<Object> list;

  public Map<String, Object> obj;

  public String string;

  public String type;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatchData patchData = (PatchData) o;
    return Objects.equals(integer, patchData.integer) && Objects.equals(list, patchData.list) && Objects.equals(obj, patchData.obj) && Objects.equals(string, patchData.string) && Objects.equals(type, patchData.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(integer, list, obj, string, type);
  }

  public void reset() {
    this.string = null;
    this.integer = null;
    this.list = null;
    this.obj = null;
    this.type = null;
  }
}

