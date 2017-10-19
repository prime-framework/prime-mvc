/*
 * Copyright (c) 2012-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.servlet.guice;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.servlet.ServletObjectsHolder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * This class is a Guice module for the Prime MVC servlet object support.
 *
 * @author Brian Pontarelli
 */
public class ServletModule extends AbstractModule {
  @Override
  protected void configure() {
    // bind to a provider so we can override w/out getting a null binding.
    bind(ServletContext.class).toProvider(ServletObjectsHolder::getServletContext);
  }
  
  @Provides
  public HttpServletRequest request() {
    return ServletObjectsHolder.getServletRequest();
  }

  @Provides
  public HttpServletRequestWrapper requestWrapper() {
    return ServletObjectsHolder.getServletRequest();
  }

  @Provides
  public HttpServletResponse response() {
    return ServletObjectsHolder.getServletResponse();
  }

  @Provides
  public HTTPMethod method() {
    return HTTPMethod.valueOf(ServletObjectsHolder.getServletRequest().getMethod().toUpperCase());
  }
}
