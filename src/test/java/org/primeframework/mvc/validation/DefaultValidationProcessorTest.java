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
import io.fusionauth.http.HTTPMethod;
import org.example.action.PostValidationOrderChild;
import org.example.action.PostValidationOrderNoAnnotationChild;
import org.example.action.PostValidationOrderOverrideChild;
import org.example.action.PreValidationOrderChild;
import org.example.action.PreValidationOrderNoAnnotationChild;
import org.example.action.PreValidationOrderOverrideChild;
import org.example.action.ValidationMethods;
import org.example.action.ValidationOrderChild;
import org.example.action.ValidationOrderNoAnnotationChild;
import org.example.action.ValidationOrderOverrideChild;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocationStore;
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

  @Test
  public void validationMethodOrdering_inheritedDifferentNames() {
    // Base declares zzzValidateA, zzzValidateB (depth=1); child declares aaaValidateC, aaaValidateD (depth=0).
    // The zzz/aaa prefixes make depth-first ordering visually obvious: base's zzz methods fire
    // before child's aaa methods even though zzz sorts after aaa alphabetically.
    request.setMethod(HTTPMethod.PUT);

    ValidationOrderChild action = new ValidationOrderChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzValidateA", "zzzValidateB", "aaaValidateC", "aaaValidateD"));
  }

  @Test
  public void validationMethodOrdering_override_reannotated() {
    // Child re-annotates zzzValidateA. getMethods() deduplicates: child's zzzValidateA is at depth=0,
    // base's zzzValidateB is at depth=1 and fires first.
    // Expected invocation order: [zzzValidateB, zzzValidateA-child]
    request.setMethod(HTTPMethod.PUT);

    ValidationOrderOverrideChild action = new ValidationOrderOverrideChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzValidateB", "zzzValidateA-child"));
  }

  @Test
  public void validationMethodOrdering_override_annotationLost() {
    // Child overrides zzzValidateA WITHOUT re-adding @ValidationMethod. Java does not inherit method
    // annotations — the unannotated child override replaces the annotated base method in getMethods(),
    // so zzzValidateA is silently dropped. Only zzzValidateB (from the base) is invoked.
    request.setMethod(HTTPMethod.PUT);

    ValidationOrderNoAnnotationChild action = new ValidationOrderNoAnnotationChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzValidateB"));
  }

  @Test
  public void preValidationMethodOrdering_inheritedDifferentNames() {
    // Base declares zzzPreA, zzzPreB (depth=1); child declares aaaPreC, aaaPreD (depth=0).
    // The zzz/aaa prefixes make depth-first ordering visually obvious: base's zzz methods fire
    // before child's aaa methods even though zzz sorts after aaa alphabetically.
    request.setMethod(HTTPMethod.PUT);

    PreValidationOrderChild action = new PreValidationOrderChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzPreA", "zzzPreB", "aaaPreC", "aaaPreD"));
  }

  @Test
  public void preValidationMethodOrdering_override_reannotated() {
    // Child re-annotates zzzPreA. getMethods() deduplicates: child's zzzPreA is at depth=0,
    // base's zzzPreB is at depth=1 and fires first.
    // Expected invocation order: [zzzPreB, zzzPreA-child]
    request.setMethod(HTTPMethod.PUT);

    PreValidationOrderOverrideChild action = new PreValidationOrderOverrideChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzPreB", "zzzPreA-child"));
  }

  @Test
  public void preValidationMethodOrdering_override_annotationLost() {
    // Child overrides zzzPreA WITHOUT re-adding @PreValidationMethod. Java does not inherit method
    // annotations — the unannotated child override replaces the annotated base method in getMethods(),
    // so zzzPreA is silently dropped. Only zzzPreB (from the base) is invoked.
    request.setMethod(HTTPMethod.PUT);

    PreValidationOrderNoAnnotationChild action = new PreValidationOrderNoAnnotationChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzPreB"));
  }

  @Test
  public void postValidationMethodOrdering_inheritedDifferentNames() {
    // Base declares zzzPostA, zzzPostB (depth=1); child declares aaaPostC, aaaPostD (depth=0).
    // The zzz/aaa prefixes make depth-first ordering visually obvious: base's zzz methods fire
    // before child's aaa methods even though zzz sorts after aaa alphabetically.
    request.setMethod(HTTPMethod.PUT);

    PostValidationOrderChild action = new PostValidationOrderChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzPostA", "zzzPostB", "aaaPostC", "aaaPostD"));
  }

  @Test
  public void postValidationMethodOrdering_override_reannotated() {
    // Child re-annotates zzzPostA. getMethods() deduplicates: child's zzzPostA is at depth=0,
    // base's zzzPostB is at depth=1 and fires first.
    // Expected invocation order: [zzzPostB, zzzPostA-child]
    request.setMethod(HTTPMethod.PUT);

    PostValidationOrderOverrideChild action = new PostValidationOrderOverrideChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzPostB", "zzzPostA-child"));
  }

  @Test
  public void postValidationMethodOrdering_override_annotationLost() {
    // Child overrides zzzPostA WITHOUT re-adding @PostValidationMethod. Java does not inherit method
    // annotations — the unannotated child override replaces the annotated base method in getMethods(),
    // so zzzPostA is silently dropped. Only zzzPostB (from the base) is invoked.
    request.setMethod(HTTPMethod.PUT);

    PostValidationOrderNoAnnotationChild action = new PostValidationOrderNoAnnotationChild();
    store.setCurrent(makeActionInvocation(action, HTTPMethod.PUT, ""));

    DefaultValidationProcessor processor = new DefaultValidationProcessor(request, store, messageStore);
    processor.validate();

    assertEquals(action.invocationOrder, java.util.List.of("zzzPostB"));
  }
}
