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
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

/**
 * Concrete child action for testing the annotation-loss footgun with {@link PreValidationMethod}.
 * The child overrides {@code zzzPreA()} WITHOUT re-adding {@code @PreValidationMethod}. Because Java does
 * not inherit method annotations, and because {@code Class.getMethods()} returns only the child's
 * (unannotated) override, the {@code zzzPreA} entry is silently wiped from the pre-validation method list.
 * <p>
 * Expected result: only [zzzPreB] is found — zzzPreA is gone entirely despite being annotated on the base.
 */
@Action
public class PreValidationOrderNoAnnotationChild extends PreValidationOrderBase {
  @Override
  public void zzzPreA() {
    invocationOrder.add("zzzPreA-child");
  }

  public String put() {
    return "success";
  }
}
