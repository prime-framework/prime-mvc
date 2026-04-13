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

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

/**
 * Concrete child action for testing {@link PostValidationMethod} ordering when a child overrides a base method
 * and re-adds the annotation. Because {@code Class.getMethods()} deduplicates overridden methods, only the
 * child's declaration of {@code zzzPostA} is visible (depth=0). The non-overridden {@code zzzPostB} from
 * the base remains at depth=1 and executes first.
 * <p>
 * Expected order: [zzzPostB (base, depth=1), zzzPostA (child override, depth=0)]
 */
@Action
public class PostValidationOrderOverrideChild extends PostValidationOrderBase {
  @Override
  @PostValidationMethod
  public void zzzPostA() {
    invocationOrder.add("zzzPostA-child");
  }

  public String put() {
    return "success";
  }
}
