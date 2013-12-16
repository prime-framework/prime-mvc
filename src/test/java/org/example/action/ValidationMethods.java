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
package org.example.action;

import com.google.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.validation.Validatable;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;
import org.primeframework.mvc.validation.Validation;

/**
 * Action for testing the PreValidationMethod and PostValidationMethod annotations
 *
 * @author Brian Pontarelli
 */
@Action
public class ValidationMethods implements Validatable {
  private final MessageStore messageStore;

  public boolean addInterfaceErrors = false;

  public boolean addMethodErrors = false;

  public boolean preValidation = false;

  public boolean postValidation = false;

  @Inject
  public ValidationMethods(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  @PreValidationMethod
  public void toggleOn() {
    this.preValidation = true;
  }

  @PostValidationMethod
  public void toggleOff() {
    this.postValidation = true;
  }

  @Override
  public void validate() {
    if (addInterfaceErrors) {
      messageStore.add(new SimpleMessage(MessageType.ERROR, "interface-general-code", "interface-general-message"));
      messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "interface-field", "interface-field-code", "interface-field-message"));
    }
  }

  @ValidationMethod(httpMethods = HTTPMethod.PUT)
  public void validateMethod() {
    if (addMethodErrors) {
      messageStore.add(new SimpleMessage(MessageType.ERROR, "method-general-code", "method-general-message"));
      messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "method-field", "method-field-code", "method-field-message"));
    }
  }

  public String get() {
    return "success";
  }

  @Validation(enabled = false)
  public String post() {
    return null;
  }

  public String put() {
    return null;
  }
}
