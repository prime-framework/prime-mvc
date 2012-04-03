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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.action.user.Edit;
import org.example.domain.Address;
import org.example.domain.User;
import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.validation.ValidationException;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static org.testng.Assert.*;

/**
 * Tests the JSR validation.
 *
 * @author Brian Pontarelli
 */
public class JSRValidationProcessorTest extends PrimeBaseTest {
  @Inject public JSRValidationProcessor processor;
  @Inject public ActionInvocationStore store;
  @Inject public MessageStore messageStore;

  @Test
  public void success() throws NoSuchMethodException {
    request.setMethod(Method.POST);

    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setAge(35);
    edit.user.setName("Brian Pontarelli");
    edit.user.setSecurityQuestions("One", "Two");
    Address address = new Address();
    address.setCity("Broomfield");
    address.setCountry("US");
    address.setState("CO");
    address.setStreet("7050 W. 120th Suite 202");
    address.setZipcode("80020");
    edit.user.setAddress("home", address);

    store.setCurrent(new DefaultActionInvocation(edit, Edit.class.getMethod("execute"), "/user/edit", "", new DefaultActionConfiguration(Edit.class, "/user/edit")));
    processor.validate();
  }

  @Test
  public void annotationGroups() throws NoSuchMethodException {
    request.setMethod(Method.POST);

    Edit edit = new Edit();
    edit.user.setMonth(10);
    store.setCurrent(new DefaultActionInvocation(edit, Edit.class.getMethod("post"), "/user/edit", "", new DefaultActionConfiguration(Edit.class, "/user/edit")));
    try {
      processor.validate();
      fail("Should have failed");
    } catch (ValidationException e) {
      List<Message> messages = messageStore.get();
      Map<String, FieldMessage> map = new HashMap<String, FieldMessage>();
      for (Message message : messages) {
        map.put(((FieldMessage) message).getField(), (FieldMessage) message);
      }
      assertEquals(map.size(), 1);
      assertEquals(map.get("user.month").toString(), "Month delete yay!");
    }
  }

  @Test
  public void failure() throws NoSuchMethodException {
    request.setMethod(Method.POST);

    Edit edit = new Edit();
    edit.user = new User();
    Address address = new Address();
    edit.user.setAddress("home", address);

    store.setCurrent(new DefaultActionInvocation(edit, Edit.class.getMethod("execute"), "/user/edit", "", new DefaultActionConfiguration(Edit.class, "/user/edit")));
    try {
      processor.validate();
      fail("Should have failed");
    } catch (ValidationException e) {
      List<Message> messages = messageStore.get();
      Map<String, FieldMessage> map = new HashMap<String, FieldMessage>();
      for (Message message : messages) {
        map.put(((FieldMessage) message).getField(), (FieldMessage) message);
      }
      assertEquals(map.size(), 8);
      assertEquals(map.get("user.age").toString(), "Age is required");
      assertEquals(map.get("user.name").toString(), "Name is required");
      assertEquals(map.get("user.securityQuestions").toString(), "Security questions required");
      assertEquals(map.get("user.addresses['home'].city").toString(), "City is required");
      assertEquals(map.get("user.addresses['home'].country").toString(), "user.addresses['home'].country is required");
      assertEquals(map.get("user.addresses['home'].state").toString(), "user.addresses['home'].state is required");
      assertEquals(map.get("user.addresses['home'].street").toString(), "user.addresses['home'].street is required");
      assertEquals(map.get("user.addresses['home'].zipcode").toString(), "user.addresses['home'].zipcode is required");
    }
  }

  @Test
  public void failureFromPrevious() throws NoSuchMethodException {
    request.setMethod(Method.POST);

    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setAge(35);
    edit.user.setName("Brian Pontarelli");
    edit.user.setSecurityQuestions("One", "Two");
    Address address = new Address();
    address.setCity("Broomfield");
    address.setCountry("US");
    address.setState("CO");
    address.setStreet("7050 W. 120th Suite 202");
    address.setZipcode("80020");
    edit.user.setAddress("home", address);

    // Add a previous error
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "test", "failure"));

    store.setCurrent(new DefaultActionInvocation(edit, Edit.class.getMethod("execute"), "/user/edit", "", new DefaultActionConfiguration(Edit.class, "/user/edit")));
    try {
      processor.validate();
      fail("Should have failed");
    } catch (ValidationException e) {
      List<Message> messages = messageStore.get();
      Map<String, FieldMessage> map = new HashMap<String, FieldMessage>();
      for (Message message : messages) {
        map.put(((FieldMessage) message).getField(), (FieldMessage) message);
      }
      assertEquals(map.size(), 1);
      assertEquals(map.get("test").toString(), "failure");
    }
  }
}
