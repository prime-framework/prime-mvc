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

import java.util.ArrayList;
import java.util.List;

import io.fusionauth.http.FileInfo;
import org.example.domain.Role;
import org.example.domain.UserField;
import org.example.domain.UserType;
import org.primeframework.mvc.action.annotation.Action;
import static java.util.Arrays.asList;

/**
 * This class is a simple edit action for testing.
 *
 * @author Brian Pontarelli
 */
@Action
public class FullFormAction {
  public List<Integer> ages = new ArrayList<Integer>();
  private static List<Integer> agesFromLastInvocation;

  public FileInfo image;
  private static FileInfo imageFromLastInvocation;

  public List<Integer> roleIds;
  private static List<Integer> roleIdsFromLastInvocation;

  public List<Role> roles = asList(new Role(1, "Admin"), new Role(2, "User"));

  public UserField user;

  public UserType[] userTypes = UserType.values();

  public FullFormAction() {
    for (int i = 1; i < 100; i++) {
      ages.add(i);
    }
  }

  public String get() {
    return "input";
  }

  public String post() {
    roleIdsFromLastInvocation = roleIds;
    imageFromLastInvocation = image;
    agesFromLastInvocation = ages;
    return "success";
  }

  public static List<Integer> getRoleIdsFromLastInvocation() {
    return roleIdsFromLastInvocation;
  }

  public static FileInfo getImageFromLastInvocation() {
    return imageFromLastInvocation;
  }

  public static List<Integer> getAgesFromLastInvocation() {
    return agesFromLastInvocation;
  }

  public static void reset() {
    roleIdsFromLastInvocation = null;
    imageFromLastInvocation = null;
    agesFromLastInvocation = null;
  }
}
