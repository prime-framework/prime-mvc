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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.validation.ValidationException;
import org.primeframework.mvc.validation.ValidationProcessor;

import com.google.inject.Inject;

/**
 * A validator that uses the JSR 303 validation API.
 *
 * @author Brian Pontarelli
 */
public class JSRValidationProcessor implements ValidationProcessor {
  private final ActionInvocationStore store;
  private final MessageProvider provider;
  private final MessageStore messageStore;
  private final Validator validator;
  private final GroupLocator locator;

  @Inject
  public JSRValidationProcessor(ActionInvocationStore store, MessageProvider provider, MessageStore messageStore,
                                Validator validator, GroupLocator locator) {
    this.store = store;
    this.provider = provider;
    this.messageStore = messageStore;
    this.validator = validator;
    this.locator = locator;
  }

  @Override
  public void validate() throws ValidationException {
    Object action = store.getCurrent().action();
    if (action == null) {
      return;
    }

    Class<?>[] groups = locator.groups();
    Set<ConstraintViolation<Object>> violations = validator.validate(action, groups);
    for (ConstraintViolation<Object> violation : violations) {
      String constraint = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
      if (violation.getPropertyPath() == null || StringUtils.isBlank(violation.getPropertyPath().toString())) {
        throw new ErrorException("error", "Property path undefined for class [" + violation.getLeafBean().getClass().getName() +
          "], constraint [" + constraint + "]");
      }

      String propertyPath = violation.getPropertyPath().toString();
      Object invalidValue = violation.getInvalidValue();
      FieldMessage message = provider.getFieldMessage(propertyPath, "[" + constraint + "]" + propertyPath, invalidValue);
      messageStore.add(message);
    }
    
    if (violations.size() > 0) {
      throw new ValidationException();
    }
  }
}
