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
package org.primeframework.mvc.servlet.guice;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.primeframework.mvc.servlet.annotation.HTTPMethod;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * This class is a Guice module for the Prime MVC servlet object support.
 *
 * @author Brian Pontarelli
 */
public class ServletModule extends AbstractModule {
  @Override
  protected void configure() {
    if (ServletObjectsHolder.getServletContext() == null) {
      return;
    }

    // Bind the servlet context
    bind(ServletContext.class).toProvider(new Provider<ServletContext>() {
      public ServletContext get() {
        return ServletObjectsHolder.getServletContext();
      }
    }).in(Singleton.class);

    // Bind the servlet request
    bind(HttpServletRequest.class).toProvider(new Provider<HttpServletRequest>() {
      public HttpServletRequest get() {
        return ServletObjectsHolder.getServletRequest();
      }
    });

    // Bind the servlet request wrapper
    bind(HttpServletRequestWrapper.class).toProvider(new Provider<HttpServletRequestWrapper>() {
      public HttpServletRequestWrapper get() {
        return ServletObjectsHolder.getServletRequest();
      }
    });

    // Bind the servlet response
    bind(HttpServletResponse.class).toProvider(new Provider<HttpServletResponse>() {
      public HttpServletResponse get() {
        return ServletObjectsHolder.getServletResponse();
      }
    });

    // Bind the HTTP method
    bind(String.class).annotatedWith(HTTPMethod.class).toProvider(new Provider<String>() {
      public String get() {
        return ServletObjectsHolder.getServletRequest().getMethod();
      }
    });
  }
}
