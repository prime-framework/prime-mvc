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
package org.primeframework.mvc.test;

import org.primeframework.mvc.config.AbstractPrimeMVCConfiguration;

/**
 * This is a mock configuration object that delegates to another PrimeMVCConfiguration instance, but also allows
 * specific properties to be mocked out. This can be used in a unit tests via a little glue like this:
 * <pre>
 *
 * protected PrimeMVCConfiguration config;
 *
 * {@code @Inject}
 * public void setConfiguration(PrimeMVCConfiguration config) {
 *   this.config = config;
 * }
 *
 * {@code @Test}
 * protected void mock() {
 *   PrimeMVCConfiguration mock = new MockConfiguration(config);
 *   mock.addParameter("foo", "bar");
 *   ...
 * }
 * </pre>
 *
 * @author Brian Pontarelli
 */
public class MockConfiguration extends AbstractPrimeMVCConfiguration {

  @Override
  public int freemarkerCheckSeconds() {
    return 0;
  }

  @Override
  public int l10nReloadSeconds() {
    return 0;
  }

  @Override
  public boolean allowUnknownParameters() {
    return false;
  }
}