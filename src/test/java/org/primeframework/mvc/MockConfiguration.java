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
package org.primeframework.mvc;

import org.primeframework.mvc.config.AbstractMVCConfiguration;

/**
 * This is a mock configuration object that delegates to another MVCConfiguration instance, but also allows
 * specific properties to be mocked out.
 *
 * @author Brian Pontarelli
 */
public class MockConfiguration extends AbstractMVCConfiguration {
  private int freemarkerCheckSeconds;
  private int l10nReloadSeconds;
  private boolean allowUnknownParameters;
  
  public MockConfiguration() {
  }

  public MockConfiguration(int freemarkerCheckSeconds, int l10nReloadSeconds, boolean allowUnknownParameters) {
    this.freemarkerCheckSeconds = freemarkerCheckSeconds;
    this.l10nReloadSeconds = l10nReloadSeconds;
    this.allowUnknownParameters = allowUnknownParameters;
  }

  @Override
  public int templateCheckSeconds() {
    return freemarkerCheckSeconds;
  }

  @Override
  public int l10nReloadSeconds() {
    return l10nReloadSeconds;
  }

  @Override
  public boolean allowUnknownParameters() {
    return allowUnknownParameters;
  }
}