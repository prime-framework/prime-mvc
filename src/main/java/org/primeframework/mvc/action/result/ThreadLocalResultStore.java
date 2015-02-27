/*
 * Copyright (c) 2012-2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

/**
 * The default implementation of the result code store. It uses a ThreadLocal.
 *
 * @author Brian Pontarelli
 */
public class ThreadLocalResultStore implements ResultStore {
  private final static ThreadLocal<String> store = new ThreadLocal<>();
  
  @Override
  public String get() {
    return store.get();
  }

  @Override
  public void set(String resultCode) {
    store.set(resultCode);
  }

  @Override
  public void clear() {
    store.remove();
  }
}
