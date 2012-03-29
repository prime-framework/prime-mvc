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
package org.primeframework.mvc.validation.jsr303;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.*;

/**
 * Provides the validation DSL for JSR 303.
 *
 * @author Brian Pontarelli
 */
public interface ValidationBuilder {

  /**
   * Runs the validation on the object.
   *
   * @param object The object.
   * @return The DSL builder object.
   */
  ValidationCriteria validate(Object object);

  /**
   * Builder for modeling validation criteria.  this contains all the information necessary to handle validation
   */
  static class ValidationCriteria {
    private final Validator validator;
    public List<Class<?>> groups = new ArrayList<Class<?>>();
    public Object object;

    public ValidationCriteria(Object object, Validator validator) {
      this.object = object;
      this.validator = validator;
    }

    /**
     * The group to validate
     *
     * @param groups the group
     * @return this
     */
    public ValidationCriteria withGroups(Class<?>... groups) {
      this.groups.addAll(asList(groups));
      return this;
    }

    /**
     * Callback to the ValidationService to execute the validation process
     */
    public Set<ConstraintViolation<Object>> now() {
      return validator.validate(object);
    }
  }
}
