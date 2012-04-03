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
package org.example.action.user;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.util.Set;

import org.example.domain.User;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.validation.jsr303.Validatable;
import org.primeframework.mvc.validation.jsr303.group.Create;
import org.primeframework.mvc.validation.jsr303.group.Update;

import com.google.inject.Inject;

/**
 * @author Brian Pontarelli
 */
public class ValidatableAction implements Validatable<ValidatableAction> {
  private final HTTPMethod method;

  @Valid
  public User user = new User();

  @Inject
  public ValidatableAction(HTTPMethod method) {
    this.method = method;
  }

  @Override
  public Set<ConstraintViolation<ValidatableAction>> validate(Validator validator) {
    if (method == HTTPMethod.POST) {
      return validator.validate(this, Create.class);
    }

    return validator.validate(this, Update.class);
  }

  public String post() {
    return "success";
  }
}
