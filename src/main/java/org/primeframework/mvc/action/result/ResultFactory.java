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
package org.primeframework.mvc.action.result;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Binds and builds results for faster access.
 *
 * @author Brian Pontarelli
 */
public class ResultFactory {
  private final static Map<Class<? extends Annotation>, Class<? extends Result>> bindings = new HashMap<Class<? extends Annotation>, Class<? extends Result>>();
  private final Injector injector;

  /**
   * Binds the given result type for the given annotation type.
   *
   * @param annotationType The result annotation.
   * @param resultType The result type.
   * @param <T> The annotation type.
   */
  public static <T extends Annotation> void addResult(Binder binder, Class<T> annotationType, Class<? extends Result<T>> resultType) {
    binder.bind(resultType);
    bindings.put(annotationType, resultType);
  }

  @Inject
  public ResultFactory(Injector injector) {
    this.injector = injector;
  }

  /**
   * Builds the Result for the given annotation type.
   *
   * @param annotationType The annotation type.
   * @return The Result instance.
   */
  public Result build(Class<? extends Annotation> annotationType) {
    return injector.getInstance(bindings.get(annotationType));
  }
}
