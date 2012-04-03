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

import java.util.List;
import java.util.Map;

/**
 * This is a test action.
 *
 * @author Brian Pontarelli
 */
@org.primeframework.mvc.action.annotation.Action
public class Action {
  private int[] roleIds;
  private List<Integer> selectionIds;
  private Map<String, List<Integer>> choices;
  private Map<String, Integer[]> associations;

  private User user;

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    this.user = value;
  }

  public int[] getRoleIds() {
    return roleIds;
  }

  public void setRoleIds(int[] roleIds) {
    this.roleIds = roleIds;
  }

  public List<Integer> getSelectionIds() {
    return selectionIds;
  }

  public void setSelectionIds(List<Integer> selectionIds) {
    this.selectionIds = selectionIds;
  }

  public Map<String, List<Integer>> getChoices() {
    return choices;
  }

  public void setChoices(Map<String, List<Integer>> choices) {
    this.choices = choices;
  }

  public Map<String, Integer[]> getAssociations() {
    return associations;
  }

  public void setAssociations(Map<String, Integer[]> associations) {
    this.associations = associations;
  }

  public String execute() {
    return "success";
  }
}