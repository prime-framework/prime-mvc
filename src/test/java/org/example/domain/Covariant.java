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
package org.example.domain;

/**
 * @author Brian Pontarelli
 */
public class Covariant extends CovariantBase<User> {
  public String value;
  public User user;

  @Override
  public String getAbstract() {
    return value;
  }

  @Override
  public String getOverride() {
    return value;
  }

  @Override
  public User getAbstractNesting() {
    return user;
  }

  @Override
  public void setAbstractNesting(User user) {
    this.user = user;
  }
}
