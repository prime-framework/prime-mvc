/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import com.google.inject.Module;

/**
 * A test implementation of the BasePrimeMain.
 *
 * @author Brian Pontarelli
 */
public class TestPrimeMain extends BasePrimeMain {
  private final Module[] modules;

  private final int port;

  public TestPrimeMain(int port, Module... modules) {
    this.port = port;
    this.modules = modules;
  }

  @Override
  public int determinePort() {
    return port;
  }

  @Override
  protected Module[] modules() {
    return modules;
  }
}
