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
package org.primeframework.mvc.security.guice;

import com.google.inject.AbstractModule;
import org.primeframework.mvc.security.DefaultStaticClasspathResourceFilter;
import org.primeframework.mvc.security.DefaultStaticResourceFilter;
import org.primeframework.mvc.security.StaticClasspathResourceFilter;
import org.primeframework.mvc.security.StaticResourceFilter;

/**
 * @author Daniel DeGroff
 */
public class StaticResourceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(StaticResourceFilter.class).to(DefaultStaticResourceFilter.class);
    bind(StaticClasspathResourceFilter.class).to(DefaultStaticClasspathResourceFilter.class);
  }
}
