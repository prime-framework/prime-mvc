/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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

import java.util.HashMap;
import java.util.Map;

import org.primeframework.mvc.security.SecurityScheme;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Factory for building SecurityScheme instances based on the name of the scheme.
 *
 * @author Brian Pontarelli
 */
public class SecuritySchemeFactory {
  private static final Map<String, Class<? extends SecurityScheme>> bindings = new HashMap<>();

  private final Injector injector;

  @Inject
  public SecuritySchemeFactory(Injector injector) {
    this.injector = injector;
  }

  /**
   * Adds a binding to a SecurityScheme so that it can be created on demand rather than using a multi-binder.
   *
   * @param binder             The Guice binder to bind the SecurityScheme class for faster injection.
   * @param scheme             The name of the SecurityScheme.
   * @param securitySchemeType The security scheme class.
   */
  public static void addSecurityScheme(Binder binder, String scheme, Class<? extends SecurityScheme> securitySchemeType) {
    binder.bind(securitySchemeType);
    bindings.put(scheme, securitySchemeType);
  }

  /**
   * Builds a SecurityScheme instance.
   *
   * @param scheme The name of the SecurityScheme to build.
   * @return The the SecurityScheme or null if the scheme is invalid.
   */
  public SecurityScheme build(String scheme) {
    Class<? extends SecurityScheme> securitySchemeType = bindings.get(scheme);
    if (securitySchemeType == null) {
      return null;
    }

    return injector.getInstance(securitySchemeType);
  }
}
