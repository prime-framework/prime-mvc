/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope;

import java.lang.annotation.Annotation;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.primeframework.mvc.scope.annotation.ScopeAnnotation;

/**
 * This class implements the scope provider interface and creates scope instances via Guice.
 *
 * @author Brian Pontarelli
 */
public class DefaultScopeProvider implements ScopeProvider {
  private final Injector injector;

  @Inject
  public DefaultScopeProvider(Injector injector) {
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  public Scope lookup(Class<? extends Annotation> scopeAnnotation) {
    ScopeAnnotation ca = scopeAnnotation.getAnnotation(ScopeAnnotation.class);
    return injector.getInstance(ca.value());
  }
}