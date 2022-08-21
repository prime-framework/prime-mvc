/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a test user.
 *
 * @author Brian Pontarelli
 */
public class User {
  private boolean active;

  private Map<String, Address> addresses = new HashMap<>();

  private Integer age;

  private String email;

  private Map<Integer, Integer> ids = new HashMap<>();

  private int[] intIDs;

  private List<Integer> listIDs;

  private boolean male;

  private Boolean maleWrapper;

  private int month;

  private String name;

  private String password;

  private File profile;

  private String[] securityQuestions;

  private Set<String> setIDs;

  private List<User> siblings = new ArrayList<>();

  private int year;

  public Address getAddress(String type) {
    return addresses.get(type);
  }

  public Map<String, Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(Map<String, Address> addresses) {
    this.addresses = addresses;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getEmail() {
    return email;
  }

  private void setEmail(String email) {
    this.email = email;
  }

  public Map<Integer, Integer> getIds() {
    return ids;
  }

  public void setIds(Map<Integer, Integer> ids) {
    this.ids = ids;
  }

  public int[] getIntIDs() {
    return intIDs;
  }

  public void setIntIDs(int[] intIDs) {
    this.intIDs = intIDs;
  }

  public List<Integer> getListIDs() {
    return listIDs;
  }

  public void setListIDs(List<Integer> listIDs) {
    this.listIDs = listIDs;
  }

  public Boolean getMaleWrapper() {
    return maleWrapper;
  }

  public void setMaleWrapper(Boolean maleWrapper) {
    this.maleWrapper = maleWrapper;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public File getProfile() {
    return profile;
  }

  public void setProfile(File profile) {
    this.profile = profile;
  }

  public String[] getSecurityQuestions() {
    return securityQuestions;
  }

  public void setSecurityQuestions(String... securityQuestions) {
    this.securityQuestions = securityQuestions;
  }

  public Set<String> getSetIDs() {
    return setIDs;
  }

  public void setSetIDs(Set<String> setIDs) {
    this.setIDs = setIDs;
  }

  public User getSibling(int index) {
    if (index >= this.siblings.size()) {
      return null;
    }

    return siblings.get(index);
  }

  public List<User> getSiblings() {
    return siblings;
  }

  public void setSiblings(List<User> siblings) {
    this.siblings = siblings;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isMale() {
    return male;
  }

  public void setMale(boolean male) {
    this.male = male;
  }

  public void setAddress(String type, Address address) {
    this.addresses.put(type, address);
  }

  public void setSibling(int index, User sibling) {
    if (index >= this.siblings.size()) {
      for (int i = this.siblings.size(); i <= index; i++) {
        this.siblings.add(null);
      }
    }
    this.siblings.set(index, sibling);
  }
}
