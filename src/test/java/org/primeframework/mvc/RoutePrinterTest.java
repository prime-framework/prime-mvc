/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import org.testng.annotations.Test;

/**
 * Example of using the {@link RoutePrinter utility}
 */
@Test
public class RoutePrinterTest extends PrimeBaseTest {
  @Inject
  private RoutePrinter routePrinter;

  @Test
  public void dump_show_everything() {
    // arrange

    // act
    // since the URLs in this project all come from test classes, the 2nd param is not provided, but you usually would want to use
    // something like "build/classes/test", depending on your build tool
    routePrinter.dump(true, true);
  }

  @Test
  public void dump_no_actions() {
    // arrange

    // act

    // assert
    routePrinter.dump(true, false);
  }

  @Test
  public void dump_no_methods_no_actions() {
    // arrange

    // act
    routePrinter.dump(false, false);
  }

  @Test
  public void dump_exclude() {
    // arrange

    // act

    // assert
    routePrinter.dump(true, true, "build/classes/test");
  }
}
