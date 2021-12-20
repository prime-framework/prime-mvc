/*
 * Copyright (c) 2012-2016, Inversoft Inc., All Rights Reserved
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

import java.util.List;

import com.google.inject.Inject;
import org.example.action.ValidationMethods;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests the JSR validation.
 *
 * @author Brian Pontarelli
 */
public class DefaultValidationProcessorTest extends PrimeBaseTest {
  @Inject public MessageStore messageStore;

  @Inject public ActionInvocationStore store;

  @Test
  public void disabled() {
    request.setMethod(HTTPMethod.POST);

    ValidationMethods action = new ValidationMethods(messageStore);
    assertFalse(action.preValidation);
    assertFalse(action.postValidation);

    store.setCurrent(makeActionInvocation(action, HTTPMethod.POST, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();
    assertFalse(action.preValidation);
    assertFalse(action.postValidation);
  }

  @Test
  public void failureFromPrevious() {
    request.setMethod(HTTPMethod.PUT);

    ValidationMethods action = new ValidationMethods(messageStore);
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    // Add a previous error
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "test", "code", "failure"));

    try {
      DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
      processor.validate();
      fail("Should have failed");
    } catch (ValidationException e) {
      assertEquals(messageStore.get(), List.of(new SimpleFieldMessage(MessageType.ERROR, "test", "code", "failure")));
    }
  }

  @Test
  public void success() throws Exception {
    request.setMethod(HTTPMethod.PUT);

    ValidationMethods action = new ValidationMethods(messageStore);
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();
  }

  @Test
  public void validatable() {
    request.setMethod(HTTPMethod.PUT);

    ValidationMethods action = new ValidationMethods(messageStore);
    action.addInterfaceErrors = true;
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    try {
      DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
      processor.validate();
      fail("Should have failed");
    } catch (ValidationException e) {
      // Assert the message store
      assertEquals(messageStore.get(), List.of(
          new SimpleMessage(MessageType.ERROR, "interface-general-code", "interface-general-message"),
          new SimpleFieldMessage(MessageType.ERROR, "interface-field", "interface-field-code", "interface-field-message")));
    }
  }

  @Test
  public void validateGet() {
    for (HTTPMethod method : List.of(HTTPMethod.GET, HTTPMethod.HEAD)) {
      request.setMethod(method);

      ValidationMethods action = new ValidationMethods(messageStore);
      store.setCurrent(makeActionInvocation(action, HTTPMethod.GET, ""));

      DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
      processor.validate();

      assertTrue(action.getValidationCalled);
    }
  }

  @Test
  public void validationMethod() {
    request.setMethod(HTTPMethod.PUT);

    ValidationMethods action = new ValidationMethods(messageStore);
    action.addMethodErrors = true;
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    try {
      DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
      processor.validate();
      fail("Should have failed");
    } catch (ValidationException e) {
      // Assert the message store
      assertEquals(messageStore.get(), List.of(
          new SimpleMessage(MessageType.ERROR, "method-general-code", "method-general-message"),
          new SimpleFieldMessage(MessageType.ERROR, "method-field", "method-field-code", "method-field-message")));
    }
  }

  @Test
  public void validationMethodNotExecutedForGET() {
    request.setMethod(HTTPMethod.GET);

    ValidationMethods action = new ValidationMethods(messageStore);
    action.addMethodErrors = true;
    store.setCurrent(makeActionInvocation(action, HTTPMethod.GET, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();
  }

  @Test
  public void validationMethods() {
    request.setMethod(HTTPMethod.GET);

    ValidationMethods action = new ValidationMethods(messageStore);
    assertFalse(action.preValidation);
    assertFalse(action.postValidation);
    store.setCurrent(makeActionInvocation(action, HTTPMethod.GET, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();
    assertTrue(action.preValidation);
    assertTrue(action.postValidation);

    // Reset
    action = new ValidationMethods(messageStore);
    assertFalse(action.preValidation);
    assertFalse(action.postValidation);

    // Add an error so that the validation method throws an exception
    messageStore.add(new SimpleMessage(MessageType.ERROR, "code", "message"));
    store.setCurrent(makeActionInvocation(action, HTTPMethod.GET, ""));

    try {
      processor.validate();
      fail("Should have thrown");
    } catch (ValidationException e) {
      // Expected
    }

    assertTrue(action.preValidation);
    assertTrue(action.postValidation);
  }
}
