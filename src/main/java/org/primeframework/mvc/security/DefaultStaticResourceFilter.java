/*
 * Copyright (c) 2023, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.config.MVCConfiguration;

/**
 * The default behavior will be to load all static resources that can be resolved within the configured
 * static resource directory as configured by {@link MVCConfiguration#staticDirectory()}.
 *
 * @author Daniel DeGroff
 */
public class DefaultStaticResourceFilter implements StaticResourceFilter {
  @Override
  public boolean allow(String uri, HTTPRequest request) {
    // Allow all resources to be loaded.
    return true;
  }
}
