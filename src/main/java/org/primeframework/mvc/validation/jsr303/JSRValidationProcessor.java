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
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.util.ArrayBuilder;
import org.primeframework.mvc.validation.ValidationException;
import org.primeframework.mvc.validation.jsr303.util.ValidationUtils;

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
  @SuppressWarnings("unchecked")
  public void validate() throws ValidationException {
    ActionInvocation actionInvocation = store.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      return;
    }

    Set<ConstraintViolation<Object>> violations;
    if (action instanceof Validatable) {
      violations = ((Validatable) action).validate(validator);
    } else {
      Class<?>[] groups = locator.groups();
      violations = validator.validate(action, groups);
    }

    ActionConfiguration actionConfiguration = actionInvocation.configuration;
    if (actionConfiguration.validationMethods.size() > 0) {
      for (Method method : actionConfiguration.validationMethods) {
        try {
          method.invoke(action);
        } catch (IllegalAccessException e) {
          throw new PrimeException("Unable to invoke @ValidationMethod on the class [" + actionConfiguration.actionClass + "]");
        } catch (InvocationTargetException e) {
          Throwable t = e.getTargetException();
          if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
          }

          throw new PrimeException("@ValidationMethod on the class [" + actionConfiguration.actionClass + "] threw a " +
            "checked exception", t);
        }
      }
    }

    // If there are any messages, throw an exception. This will handle the violations that were transferred to the
    // MessageStore as well as the conversion errors since both are placed into the REQUEST scope. The FLASH scope
    // shouldn't impact this since it is transferred to the REQUEST scope after the validation occurs
    if ((violations != null && violations.size() > 0) || messageStore.get(MessageScope.REQUEST).size() > 0) {
      throw new ValidationException(violations);
    }
  }

  @Override
  public void handle(Set<ConstraintViolation<Object>> violations) {
    if (violations == null || violations.isEmpty()) {
      return;
    }

    for (ConstraintViolation<Object> violation : violations) {
      ConstraintDescriptor<?> descriptor = violation.getConstraintDescriptor();
      String constraint = descriptor.getAnnotation().annotationType().getSimpleName();
      if (violation.getPropertyPath() == null || StringUtils.isBlank(violation.getPropertyPath().toString())) {
        throw new PrimeException("Property path undefined for class [" + violation.getLeafBean().getClass().getName() +
          "], constraint [" + constraint + "]");
      }

      String propertyPath = ValidationUtils.toString(violation.getPropertyPath());
      Object invalidValue = violation.getInvalidValue();
      Object[] values = new ArrayBuilder<Object>(Object.class, propertyPath, invalidValue).
        addAll(new TreeMap<String, Object>(descriptor.getAttributes()).values()).
        done();

      String message;
      try {
        message = provider.getMessage("[" + constraint + "]" + propertyPath, (Object[]) values);
      } catch (MissingMessageException e) {
        try {
          message = provider.getMessage("[" + constraint + "]", (Object[]) values);
        } catch (MissingMessageException e1) {
          throw new MissingMessageException("Message could not be found for the URI [" + store.getCurrent().actionURI +
            "] either of the keys {[" + constraint + "]" + propertyPath + "} or {[" + constraint + "]}");
        }
      }

      messageStore.add(new SimpleFieldMessage(MessageType.ERROR, propertyPath, message));
    }
  }
}
