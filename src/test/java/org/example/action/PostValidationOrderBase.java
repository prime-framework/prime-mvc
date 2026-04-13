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

import java.util.ArrayList;
import java.util.List;

import org.primeframework.mvc.validation.annotation.PostValidationMethod;

/**
 * Abstract base action for testing {@link PostValidationMethod} ordering across an inheritance hierarchy.
 * Base methods use a {@code zzz} prefix so they sort last alphabetically — this makes it visually obvious
 * when reading test output that depth (superclass-first) takes priority over alphabetical order.
 */
public abstract class PostValidationOrderBase {
  public List<String> invocationOrder = new ArrayList<>();

  @PostValidationMethod
  public void zzzPostA() {
    invocationOrder.add("zzzPostA");
  }

  @PostValidationMethod
  public void zzzPostB() {
    invocationOrder.add("zzzPostB");
  }
}
