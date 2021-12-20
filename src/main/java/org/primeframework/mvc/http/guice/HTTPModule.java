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
package org.primeframework.mvc.http.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.http.HTTPContext;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;

/**
 * This class is a Guice module for the Prime MVC HTTP object support.
 *
 * @author Brian Pontarelli
 */
public class HTTPModule extends AbstractModule {
  @Provides
  public HTTPMethod method() {
    return HTTPObjectsHolder.getRequest().getMethod();
  }

  @Provides
  public HTTPRequest request() {
    return HTTPObjectsHolder.getRequest();
  }

  @Provides
  public HTTPResponse response() {
    return HTTPObjectsHolder.getResponse();
  }

  @Provides
  @Singleton
  protected HTTPContext configure(MVCConfiguration configuration) {
    return new HTTPContext(configuration.baseDirectory());
  }
}
