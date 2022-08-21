/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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

import org.testng.annotations.Test;

/**
 * @author Brian Pontarelli
 */
public class StaticResourceTest extends PrimeBaseTest {
  @Test
  public void get_large_resource() {
    simulator.test("/css/fusionauth-style.css")
             .get()
             .assertStatusCode(200)
             .assertContentType("text/css")
             .assertContentLength(162726);
  }

  @Test
  public void get_resource() {
    simulator.test("/js/test.js")
             .get()
             .assertStatusCode(200)
             .assertContentType("text/javascript")
             .assertBodyContains("{};");
  }
}
