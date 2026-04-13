/*
 * Copyright (c) 2026, Inversoft Inc., All Rights Reserved
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
package org.example.action;

import io.fusionauth.http.HTTPValues.Methods;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

/**
 * Concrete child action for testing {@link ValidationMethod} ordering when a child overrides a base method
 * and re-adds the annotation. Because {@code Class.getMethods()} deduplicates overridden methods, only the
 * child's declaration of {@code zzzValidateA} is visible (depth=0). The non-overridden {@code zzzValidateB} from
 * the base remains at depth=1 and executes first.
 * <p>
 * Expected order: [zzzValidateB (base, depth=1), zzzValidateA (child override, depth=0)]
 */
@Action
public class ValidationOrderOverrideChild extends ValidationOrderBase {
  @Override
  @ValidationMethod(httpMethods = Methods.PUT)
  public void zzzValidateA() {
    invocationOrder.add("zzzValidateA-child");
  }

  public String put() {
    return "success";
  }
}
