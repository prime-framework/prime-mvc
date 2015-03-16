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
package org.primeframework.mvc.validation;

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ValidationMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.ReflectionUtils;

import com.google.inject.Inject;

/**
 * A validator that uses annotations and methods to perform validation.
 *
 * @author Brian Pontarelli
 */
public class DefaultValidationProcessor implements ValidationProcessor {
  private final HTTPMethod httpMethod;

  private final MessageStore messageStore;

  private final ActionInvocationStore store;

  @Inject
  public DefaultValidationProcessor(ActionInvocationStore store, MessageStore messageStore, HTTPMethod httpMethod) {
    this.store = store;
    this.messageStore = messageStore;
    this.httpMethod = httpMethod;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void validate() throws ValidationException {
    ActionInvocation actionInvocation = store.getCurrent();
    Object action = actionInvocation.action;
    ActionConfiguration actionConfiguration = actionInvocation.configuration;
    if (action == null || (actionInvocation.method.validation != null && !actionInvocation.method.validation.enabled())) {
      return;
    }

    // Next, invoke pre methods
    ReflectionUtils.invokeAll(action, actionConfiguration.preValidationMethods);

    if (action instanceof Validatable) {
      ((Validatable) action).validate();
    }

    if (actionConfiguration.validationMethods.size() > 0) {
      for (ValidationMethodConfiguration configuration : actionConfiguration.validationMethods) {
        HTTPMethod[] httpMethods = configuration.annotation.httpMethods();
        if (ArrayUtils.contains(httpMethods, httpMethod)) {
          try {
            ReflectionUtils.invoke(configuration.method, action);
          } catch (ExpressionException e) {
            throw new PrimeException("Unable to invoke @ValidationMethod on the class [" + actionConfiguration.actionClass + "]", e);
          }
        }
      }
    }

    // Finally, invoke post methods
    ReflectionUtils.invokeAll(action, actionConfiguration.postValidationMethods);

    // If there are any messages, throw an exception. This will handle the violations that were transferred to the
    // MessageStore as well as the conversion errors since both are placed into the REQUEST scope. The FLASH scope
    // shouldn't impact this since it is transferred to the REQUEST scope after the validation occurs
    if (messageStore.get(MessageScope.REQUEST).size() > 0) {
      throw new ValidationException();
    }
  }
}
