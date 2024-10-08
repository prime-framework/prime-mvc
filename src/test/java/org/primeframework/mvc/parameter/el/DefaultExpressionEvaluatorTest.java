/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.el;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.inject.Inject;
import org.apache.commons.lang3.ArrayUtils;
import org.example.action.ExtensionInheritanceAction;
import org.example.domain.Action;
import org.example.domain.ActionField;
import org.example.domain.Address;
import org.example.domain.AddressField;
import org.example.domain.Covariant;
import org.example.domain.CustomObject;
import org.example.domain.GenericBean;
import org.example.domain.NestedDataUnwrappedAction;
import org.example.domain.User;
import org.example.domain.UserField;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests the expression evaluator.
 *
 * @author Brian Pontarelli
 */
public class DefaultExpressionEvaluatorTest extends PrimeBaseTest {
  private static final Logger logger = LoggerFactory.getLogger(DefaultExpressionEvaluatorTest.class);

  DefaultExpressionEvaluator evaluator;

  @Test
  public void charArrayCachePerformance() {
    // On a MacBook Pro Quad core the cache is slower by 5 milliseconds for 1 million iterations.
    String expression = "user.address['work'].city";
    Instant start = Instant.now();
    char c = 0;
    for (int i = 0; i < 1_000_000; i++) {
      char[] ca = expression.toCharArray();
      c = ca[2];
    }

    logger.info(Character.toString(c));
    logger.info("Time for toCharArray: " + Math.abs(Duration.between(Instant.now(), start).toMillis()));

    start = Instant.now();
    c = 0;
//    for (int i = 0; i < 1_000_000; i++) {
    // This method has been removed because it was slow
//      char[] ca = evaluator.toCharArray(expression);
//      c = ca[2];
//    }

    logger.info(Character.toString(c));
    logger.info("Time for cache: " + Math.abs(Duration.between(Instant.now(), start).toMillis()));
  }

  @Test
  public void covariantTypes() {
    Covariant bean = new Covariant();
    bean.value = "value";
    bean.override = "base";

    assertEquals(evaluator.getValue("abstract", bean), "value");
    assertEquals(evaluator.getValue("override", bean), "value");

    evaluator.setValue("abstractNesting.name", bean, "Brian Pontarelli");
    assertEquals(bean.user.getName(), "Brian Pontarelli");
    assertEquals(evaluator.getValue("abstractNesting.name", bean), "Brian Pontarelli");

    evaluator.setValue("interface.name", bean, "Brian Pontarelli");
    assertEquals(evaluator.getValue("interface.name", bean), "Brian Pontarelli");
  }

  @Test
  public void expansion() {
    // Test nested property set and type conversion
    ActionField action = new ActionField();
    action.user = new UserField();
    action.user.name = "Fred";

    String result = evaluator.expand("My name is ${user.name}", action, false);
    assertEquals(result, "My name is Fred");

    // Test nested property set
    action.user.name = "/Fred";
    result = evaluator.expand("My name is ${user.name}", action, true);
    assertEquals(result, "My name is %2FFred");

    // Test type conversion
    action.user.age = 35;
    result = evaluator.expand("My age is ${user.age}", action, true);
    assertEquals(result, "My age is 35");

    // Test null expansion w/out encoding
    action.user.age = null;
    result = evaluator.expand("My age is ${user.age}", action, false);
    assertEquals(result, "My age is ");

    // Test null expansion w/ encoding
    action.user.age = null;
    result = evaluator.expand("My age is ${user.age}", action, true);
    assertEquals(result, "My age is ");
  }

  @Test
  public void fieldGetting() {
    // Test local property null
    ActionField action = new ActionField();
    assertNull(evaluator.getValue("user", action));

    // Test local property get
    UserField user = new UserField();
    action.user = user;
    assertSame(user, evaluator.getValue("user", action));

    // Test nested property get
    action.user.age = 32;
    action.user.name = "Brian";
    action.user.active = true;

    assertEquals(evaluator.getValue("user.age", action), (Integer) 32);
    assertEquals(evaluator.getValue("user.name", action), "Brian");
    assertTrue(evaluator.getValue("user.active", action));

    // Test collection property gets
    AddressField address = new AddressField();
    address.city = "Broomfield";
    address.state = "CO";
    address.street = "Test";
    address.zipcode = "80020";
    action.user.addresses.put("home", address);
    assertNull(evaluator.getValue("user.addresses['work']", action));
    assertEquals(evaluator.getValue("user.addresses['home'].city", action), "Broomfield");
    assertEquals(evaluator.getValue("user.addresses['home'].state", action), "CO");
    assertEquals(evaluator.getValue("user.addresses['home'].street", action), "Test");
    assertEquals(evaluator.getValue("user.addresses['home'].zipcode", action), "80020");

    UserField brother = new UserField();
    brother.name = "Brett";
    brother.age = 34;
    user.siblings.add(brother);
    assertEquals(evaluator.getValue("user.siblings[0].age", action), (Integer) 34);
    assertEquals(evaluator.getValue("user.siblings[0].name", action), "Brett");

    user.securityQuestions = new String[]{"What is your pet's name?", "What is your home town?"};
    assertEquals(evaluator.getValue("user.securityQuestions[0]", action), "What is your pet's name?");
    assertEquals(evaluator.getValue("user.securityQuestions[1]", action), "What is your home town?");
  }

  @Test
  public void fieldInheritance() {
    ExtensionInheritanceAction action = new ExtensionInheritanceAction();

    // Manually set the field and then retrieve it from the base class
    action.baseField = "bar";
    assertEquals(evaluator.getValue("baseField", action), "bar");

    // Set value in the base class and retrieve it.
    evaluator.setValue("baseField", action, ArrayUtils.toArray("baz"), null);
    assertEquals(evaluator.getValue("baseField", action), "baz");
  }

  /**
   * Tests getting of fields
   */
  @Test
  public void fieldSetting() {
    // Test nested property set and type conversion
    ActionField action = new ActionField();
    evaluator.setValue("user.age", action, ArrayUtils.toArray("32"), null);
    evaluator.setValue("user.name", action, ArrayUtils.toArray("Brian"), null);
    evaluator.setValue("user.active", action, ArrayUtils.toArray("true"), null);
    assertEquals(action.user.age, (Integer) 32);
    assertEquals(action.user.name, "Brian");
    assertTrue(action.user.active);

    // Test collection property sets
    action.user.addresses = null;
    evaluator.setValue("user.addresses['home'].city", action, ArrayUtils.toArray("Broomfield"), null);
    evaluator.setValue("user.addresses['home'].state", action, ArrayUtils.toArray("CO"), null);
    evaluator.setValue("user.addresses['home'].street", action, ArrayUtils.toArray("Test"), null);
    evaluator.setValue("user.addresses['home'].zipcode", action, ArrayUtils.toArray("80020"), null);
    assertEquals(action.user.addresses.size(), 1);
    assertNull(action.user.addresses.get("work"));
    assertEquals(action.user.addresses.get("home").city, "Broomfield");
    assertEquals(action.user.addresses.get("home").state, "CO");
    assertEquals(action.user.addresses.get("home").street, "Test");
    assertEquals(action.user.addresses.get("home").zipcode, "80020");

    evaluator.setValue("user.siblings[0].age", action, ArrayUtils.toArray("34"), null);
    evaluator.setValue("user.siblings[0].name", action, ArrayUtils.toArray("Brett"), null);
    assertEquals(action.user.siblings.size(), 1);
    assertEquals(action.user.siblings.get(0).age, (Integer) 34);
    assertEquals(action.user.siblings.get(0).name, "Brett");

    evaluator.setValue("user.securityQuestions[0]", action, ArrayUtils.toArray("What is your pet's name?"), null);
    evaluator.setValue("user.securityQuestions[1]", action, ArrayUtils.toArray("What is your home town?"), null);
    assertEquals(action.user.securityQuestions.length, 2);
    assertEquals(action.user.securityQuestions[0], "What is your pet's name?");
    assertEquals(action.user.securityQuestions[1], "What is your home town?");

    action.user.securityQuestions = null;
    evaluator.setValue("user.securityQuestions", action, new String[]{"What is your pet's name?", "What is your home town?"}, null);
    assertEquals(action.user.securityQuestions.length, 2);
    assertEquals(action.user.securityQuestions[0], "What is your pet's name?");
    assertEquals(action.user.securityQuestions[1], "What is your home town?");

    // Test a final collection of Locale objects
    evaluator.setValue("locales", action, ArrayUtils.toArray(Locale.US));
    assertEquals(action.locales.size(), 1);
    assertEquals(action.locales.get(0), Locale.US);

    // Test a final collection of Strings
    evaluator.setValue("names", action, ArrayUtils.toArray("Erlich", "Richard", "Monica"));
    assertEquals(action.names.size(), 3);
    assertEquals(action.names.get(0), "Erlich");
    assertEquals(action.names.get(1), "Richard");
    assertEquals(action.names.get(2), "Monica");

    // Try again using a List instead of an Array
    evaluator.setValue("names", action, new ArrayList<>(Arrays.asList("Erlich", "Richard", "Monica")));
    assertEquals(action.names.size(), 3);
    assertEquals(action.names.get(0), "Erlich");
    assertEquals(action.names.get(1), "Richard");
    assertEquals(action.names.get(2), "Monica");

    // Test a final collection of ZoneId
    evaluator.setValue("timeZones", action, ArrayUtils.toArray(ZoneId.of("America/Denver")));
    assertEquals(action.timeZones.size(), 1);
    assertEquals(action.timeZones.get(0), ZoneId.of("America/Denver"));

    // Try again, using a List instead of an Array
    evaluator.setValue("timeZones", action, new ArrayList<>(Collections.singletonList(ZoneId.of("America/Denver"))));
    assertEquals(action.timeZones.size(), 1);
    assertEquals(action.timeZones.get(0), ZoneId.of("America/Denver"));
  }

  /**
   * Tests setting a collection with a single value onto a plain field.
   */
  @Test
  public void fieldSettingWithCollectionSingleValue() {
    // Test nested property set and type conversion
    ActionField action = new ActionField();
    evaluator.setValue("user.name", action, List.of("Brian"));
    evaluator.setValue("user.active", action, ArrayUtils.toArray("true"), null);
    assertEquals(action.user.name, "Brian");
  }

  @Test
  public void fuzzing() {
    // Trying to recreate an exception found in a customer log that looks to be fuzzing.
    GenericBean bean = new GenericBean();

    // Cannot use class in a setValue expression
    try {
      evaluator.setValue("class.method", bean, "foo");
      fail("Expected an [InvalidExpressionException] exception.");
    } catch (InvalidExpressionException e) {
      assertEquals(e.getMessage(), "The expression string [class.method] is invalid.");
    }

    // Cannot use class in a getValue expression
    try {
      evaluator.getValue("class.name", bean);
      fail("Expected an [InvalidExpressionException] exception.");
    } catch (InvalidExpressionException e) {
      assertEquals(e.getMessage(), "The expression string [class.name] is invalid.");
    }
  }

  @Test
  public void genericInheritanceImplements() {
    GenericBean bean = new GenericBean();
    evaluator.setValue("mapSubclass['foo']", bean, "value");
    assertEquals(bean.mapSubclass.get("foo"), "value");

    evaluator.setValue("mapSubclass['foo']", bean, new String[]{"value"}, Collections.emptyMap());
    assertEquals(bean.mapSubclass.get("foo"), "value");

    evaluator.setValue("mapSubclassSubclass['foo']", bean, new String[]{"value"}, Collections.emptyMap());
    assertEquals(bean.mapSubclassSubclass.get("foo"), "value");

    evaluator.setValue("mapImplements['foo']", bean, "value");
    assertEquals(bean.mapImplements.get("foo"), "value");

    evaluator.setValue("mapImplements['foo']", bean, new String[]{"value"}, Collections.emptyMap());
    assertEquals(bean.mapImplements.get("foo"), "value");
  }

  @Test
  public void immutableList() {
    // Expect this to fail nicely w/out a 500 based upon the MVC configuration
    ActionField action = new ActionField();
    //noinspection Java9CollectionFactory
    action.list = Collections.unmodifiableList(new ArrayList<>(Arrays.asList("value1", "value2")));

    try {
      evaluator.setValue("list", action, new String[]{"value3"});
      fail("Expected this to fail!");
    } catch (UnsupportedOperationException expected) {
    }
    assertEquals(action.list, List.of("value1", "value2"));

    // Try again with Java List.of()
    action.list = List.of("value1", "value2");
    try {
      evaluator.setValue("list", action, new String[]{"value3"});
      fail("Expected this to fail!");
    } catch (UnsupportedOperationException expected) {
    }
    assertEquals(action.list, List.of("value1", "value2"));

    // Using this configuration to understand if we can ignore exceptions on read only collections
    configuration.allowUnknownParameters = true;

    try {
      evaluator.setValue("list", action, new String[]{"value3"});
    } catch (UnsupportedOperationException expected) {
      fail("Unexpected exception", expected);
    }
    assertEquals(action.list, List.of("value1", "value2"));

    // Try again with Java List.of()
    action.list = List.of("value1", "value2");
    try {
      evaluator.setValue("list", action, new String[]{"value3"});
    } catch (UnsupportedOperationException expected) {
      fail("Unexpected exception", expected);
    }
    assertEquals(action.list, List.of("value1", "value2"));
  }

  @Test
  public void immutableMap() {
    // Expect this to fail nicely w/out a 500 based upon the MVC configuration
    ActionField action = new ActionField();
    var map = new LinkedHashMap<String, String>();
    map.put("key1", "value1");
    map.put("key2", "value2");
    action.map = Collections.unmodifiableMap(map);

    try {
      evaluator.setValue("map.key1", action, "newValue");
      fail("Expected this to fail!");
    } catch (UnsupportedOperationException expected) {
    }
    assertEquals(action.map.get("key1"), "value1");

    // Try again with Java Map.of()
    action.map = Map.of("key1", "value1", "key2", "value2");
    try {
      evaluator.setValue("map.key1", action, "newValue");
      fail("Expected this to fail!");
    } catch (UnsupportedOperationException expected) {
    }
    assertEquals(action.map.get("key1"), "value1");

    // Using this configuration to understand if we can ignore exceptions on read only collections
    configuration.allowUnknownParameters = true;

    try {
      evaluator.setValue("map.key1", action, "newValue");
    } catch (UnsupportedOperationException expected) {
      fail("Unexpected exception", expected);
    }
    assertEquals(action.map.get("key1"), "value1");

    // Try again with Java Map.of()
    action.map = Map.of("key1", "value1", "key2", "value2");
    try {
      evaluator.setValue("map.key1", action, "newValue");
    } catch (UnsupportedOperationException expected) {
      fail("Unexpected exception", expected);
    }
    assertEquals(action.map.get("key1"), "value1");
  }

  @Test
  public void longToken() {
    ActionField action = new ActionField();
    evaluator.setValue("reallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyLongFieldName", action, ArrayUtils.toArray("value"), null);
    assertEquals(action.reallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyLongFieldName, "value");
  }

  @Test
  public void missingConverter() {
    CustomObject custom = new CustomObject();

    // Missing converter, assert better exception message.
    try {
      evaluator.setValue("custom.value", custom, new String[]{"foo"}, Collections.emptyMap());
      fail("Expected an [ConverterStateException] exception.");
    } catch (ConverterStateException e) {
      assertEquals(e.getMessage(), "While evaluating the expression [custom.value] in class [org.example.domain.CustomObject.Custom]. No type converter found for the type [java.lang.Class].");
    }
  }

  @Test
  public void nonStringKeyForMap() {
    Action action = new Action();
    action.setUser(null);


    evaluator.setValue("user.ids[0]", action, new String[]{"0"}, null);
    assertEquals(action.getUser().getIds().get(0).intValue(), 0);

    ActionField actionField = new ActionField();
    actionField.user = null;

    evaluator.setValue("user.ids[0]", actionField, new String[]{"0"}, null);
    assertEquals(actionField.user.ids.get(0).intValue(), 0);
  }

  @Test
  public void nullMapKeys() {
    ActionField action = new ActionField();
    action.user = new UserField();

    AddressField address1 = new AddressField();
    address1.state = "CO";

    AddressField address2 = new AddressField();
    address2.state = "MN";

    // Expected state, null key, and empty key
    action.user.addresses.put(null, address1);
    action.user.addresses.put("", address2);

    // Get [] == null key, [''] == empty string key
    assertEquals(evaluator.getValue("user.addresses[].state", action), "CO");
    assertEquals(evaluator.getValue("user.addresses[''].state", action), "MN");

    // swap them
    evaluator.setValue("user.addresses[].state", action, "MN");
    evaluator.setValue("user.addresses[''].state", action, "CO");

    assertEquals(evaluator.getValue("user.addresses[].state", action), "MN");
    assertEquals(evaluator.getValue("user.addresses[''].state", action), "CO");
  }

  @Test(enabled = false)
  public void performance() throws InterruptedException {
    // Set cases

    Object action = new ActionField();
    long start = System.currentTimeMillis();
    evaluator.setValue("user.age", action, ArrayUtils.toArray("32"), null);
    long end = System.currentTimeMillis();
    System.out.println("Setting field time was " + (end - start));

    start = System.currentTimeMillis();
    evaluator.setValue("user.addresses['home'].zipcode", action, ArrayUtils.toArray("80020"), null);
    end = System.currentTimeMillis();
    System.out.println("Setting field time was " + (end - start));

    action = new Action();
    start = System.currentTimeMillis();
    evaluator.setValue("user.age", action, ArrayUtils.toArray("32"), null);
    end = System.currentTimeMillis();
    System.out.println("Setting property time was " + (end - start));

    start = System.currentTimeMillis();
    evaluator.setValue("user.addresses['home'].zipcode", action, ArrayUtils.toArray("80020"), null);
    end = System.currentTimeMillis();
    System.out.println("Setting proeprty time was " + (end - start));

    // Get cases

    action = new ActionField();
    ((ActionField) action).user = new UserField();
    ((ActionField) action).user.age = 10;
    start = System.currentTimeMillis();
    evaluator.getValue("user.age", action);
    end = System.currentTimeMillis();
    System.out.println("Getting field time was " + (end - start));

    ((ActionField) action).user = new UserField();
    ((ActionField) action).user.addresses.put("home", new AddressField());
    ((ActionField) action).user.addresses.get("home").zipcode = "80020";
    start = System.currentTimeMillis();
    evaluator.getValue("user.addresses['home'].zipcode", action);
    end = System.currentTimeMillis();
    System.out.println("Getting field time was " + (end - start));

    action = new Action();
    ((Action) action).setUser(new User());
    ((Action) action).getUser().setAge(10);
    start = System.currentTimeMillis();
    evaluator.getValue("user.age", action);
    end = System.currentTimeMillis();
    System.out.println("Getting property time was " + (end - start));

    ((Action) action).setUser(new User());
    ((Action) action).getUser().getAddresses().put("home", new Address());
    ((Action) action).getUser().getAddresses().get("home").setZipcode("80020");
    start = System.currentTimeMillis();
    evaluator.getValue("user.addresses['home'].zipcode", action);
    end = System.currentTimeMillis();
    System.out.println("Getting property time was " + (end - start));

    // Loop

    action = new ActionField();
    ((ActionField) action).user = new UserField();
    ((ActionField) action).user.addresses.put("home", new AddressField());
    ((ActionField) action).user.addresses.get("home").zipcode = "80020";
    start = System.currentTimeMillis();
    for (int i = 0; i < 50; i++) {
      evaluator.getValue("user.addresses['home'].zipcode", action);
    }
    end = System.currentTimeMillis();
    System.out.println("50 times was " + (end - start));

    JCThread[] threads = new JCThread[50];
    for (int i = 0; i < 50; i++) {
      threads[i] = new JCThread();
      threads[i].setName("" + i);
      threads[i].start();
    }

    for (int i = 0; i < 50; i++) {
      threads[i].join();
    }
  }

  @Test
  public void propertyGetting() {
    // Test local property null
    Action action = new Action();
    assertNull(evaluator.getValue("user", action));

    // Test local property get
    User user = new User();
    action.setUser(user);
    assertSame(user, evaluator.getValue("user", action));

    // Test nested property get
    action.getUser().setAge(32);
    action.getUser().setName("Brian");
    action.getUser().setActive(true);

    assertEquals(evaluator.getValue("user.age", action), (Integer) 32);
    assertEquals(evaluator.getValue("user.name", action), "Brian");
    assertTrue(evaluator.getValue("user.active", action));

    // Test collection property gets
    Address address = new Address();
    address.setCity("Broomfield");
    address.setState("CO");
    address.setStreet("Test");
    address.setZipcode("80020");
    action.getUser().getAddresses().put("home", address);
    assertNull(evaluator.getValue("user.addresses['work']", action));
    assertEquals(evaluator.getValue("user.addresses['home'].city", action), "Broomfield");
    assertEquals(evaluator.getValue("user.addresses['home'].state", action), "CO");
    assertEquals(evaluator.getValue("user.addresses['home'].street", action), "Test");
    assertEquals(evaluator.getValue("user.addresses['home'].zipcode", action), "80020");

    // Test w/ a space in the key
    action.getUser().getAddresses().put("summer home", address);
    assertEquals(evaluator.getValue("user.addresses['summer home'].city", action), "Broomfield");
    assertEquals(evaluator.getValue("user.addresses['summer home'].state", action), "CO");
    assertEquals(evaluator.getValue("user.addresses['summer home'].street", action), "Test");
    assertEquals(evaluator.getValue("user.addresses['summer home'].zipcode", action), "80020");

    User brother = new User();
    brother.setName("Brett");
    brother.setAge(34);
    user.getSiblings().add(brother);
    assertEquals(evaluator.getValue("user.siblings[0].age", action), (Integer) 34);
    assertEquals(evaluator.getValue("user.siblings[0].name", action), "Brett");

    user.setSecurityQuestions("What is your pet's name?", "What is your home town?");
    assertEquals(evaluator.getValue("user.securityQuestions[0]", action), "What is your pet's name?");
    assertEquals(evaluator.getValue("user.securityQuestions[1]", action), "What is your home town?");

    // Test indexed collection property gets (using the indexed property methoods)
    assertNull(evaluator.getValue("user.address['work']", action));
    assertEquals(evaluator.getValue("user.address['home'].city", action), "Broomfield");
    assertEquals(evaluator.getValue("user.address['home'].state", action), "CO");
    assertEquals(evaluator.getValue("user.address['home'].street", action), "Test");
    assertEquals(evaluator.getValue("user.address['home'].zipcode", action), "80020");

    assertEquals(evaluator.getValue("user.sibling[0].age", action), (Integer) 34);
    assertEquals(evaluator.getValue("user.sibling[0].name", action), "Brett");
  }

  @Test
  public void propertyGettingAndSettingInUnwrappedField() {
    NestedDataUnwrappedAction action = new NestedDataUnwrappedAction();

    // Manually set values and retrieve them
    action.bean1.data1.bar = "bing";
    action.bean1.data1.baz = "boom";
    assertEquals(evaluator.getValue("bean1.bar", action), "bing");
    assertEquals(evaluator.getValue("bean1.baz", action), "boom");

    // Manually set values on a top level and retrieve them
    action.bean3.bing = "kabam";
    assertEquals(evaluator.getValue("bing", action), "kabam");

    // Manually set values on a top level in a base class and retrieve them
    action.bean4.bam = "kapow";
    assertEquals(evaluator.getValue("bam", action), "kapow");

    // Set values using the evaluator and retrieve them.
    evaluator.setValue("bean1.bar", action, ArrayUtils.toArray("bada-bing"), null);
    evaluator.setValue("bean1.baz", action, ArrayUtils.toArray("bada-boom"), null);
    assertEquals(evaluator.getValue("bean1.bar", action), "bada-bing");
    assertEquals(evaluator.getValue("bean1.baz", action), "bada-boom");

    // Retrieve initial values of null fields
    assertNull(evaluator.getValue("bean2.user", action));
    assertNull(evaluator.getValue("bean2.user.name", action));
    assertNull(evaluator.getValue("bean2.bar", action));
    assertNull(evaluator.getValue("bean2.baz", action));

    // Set values of null fields and retrieve them
    evaluator.setValue("bean2.user.name", action, ArrayUtils.toArray("Art Vandelay"), null);
    evaluator.setValue("bean2.bar", action, ArrayUtils.toArray("bing"), null);
    evaluator.setValue("bean2.baz", action, ArrayUtils.toArray("boom"), null);
    assertEquals(evaluator.getValue("bean2.user.name", action), "Art Vandelay");
    assertEquals(evaluator.getValue("bean2.bar", action), "bing");
    assertEquals(evaluator.getValue("bean2.baz", action), "boom");

    // Set top level fields using unwrapped
    evaluator.setValue("bing", action, ArrayUtils.toArray("Art Vandelay"), null);
    assertEquals(evaluator.getValue("bing", action), "Art Vandelay");

    // Set top level fields in base class using unwrapped
    evaluator.setValue("bam", action, ArrayUtils.toArray("Art Vandelay"), null);
    assertEquals(evaluator.getValue("bam", action), "Art Vandelay");

    // Set top level fields in base class using unwrapped that is a null object
    try {
      evaluator.setValue("womp", action, ArrayUtils.toArray("Art Vandelay"), null);
      assertEquals(evaluator.getValue("womp", action), "Art Vandelay");
      fail("This should not happen - this is failing, it would be nice if it did not. But it may be a large change.");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  /**
   * /** Tests setting of bean properties
   */
  @Test
  public void propertySetting() {
    // Test nested property set and type conversion
    Action action = new Action();
    action.setUser(null);

    evaluator.setValue("user.age", action, ArrayUtils.toArray("32"), null);
    evaluator.setValue("user.name", action, ArrayUtils.toArray("Brian"), null);
    evaluator.setValue("user.active", action, ArrayUtils.toArray("true"), null);
    evaluator.setValue("user.male", action, null, null);
    assertEquals(action.getUser().getAge(), (Integer) 32);
    assertEquals(action.getUser().getName(), "Brian");
    assertTrue(action.getUser().isActive());
    assertFalse(action.getUser().isMale());

    // Test collection property sets
    action.getUser().setAddresses(null);
    evaluator.setValue("user.addresses['home'].city", action, ArrayUtils.toArray("Broomfield"), null);
    evaluator.setValue("user.addresses['home'].state", action, ArrayUtils.toArray("CO"), null);
    evaluator.setValue("user.addresses['home'].street", action, ArrayUtils.toArray("Test"), null);
    evaluator.setValue("user.addresses['home'].zipcode", action, ArrayUtils.toArray("80020"), null);
    assertEquals(action.getUser().getAddresses().size(), 1);
    assertNull(action.getUser().getAddresses().get("work"));
    assertEquals(action.getUser().getAddresses().get("home").getCity(), "Broomfield");
    assertEquals(action.getUser().getAddresses().get("home").getState(), "CO");
    assertEquals(action.getUser().getAddresses().get("home").getStreet(), "Test");
    assertEquals(action.getUser().getAddresses().get("home").getZipcode(), "80020");

    // Test collection property sets with flat names (i.e. JSON conversions)
    action.getUser().setAddresses(null);
    evaluator.setValue("user.addresses.home.city", action, ArrayUtils.toArray("Broomfield"), null);
    evaluator.setValue("user.addresses.home.state", action, ArrayUtils.toArray("CO"), null);
    evaluator.setValue("user.addresses.home.street", action, ArrayUtils.toArray("Test"), null);
    evaluator.setValue("user.addresses.home.zipcode", action, ArrayUtils.toArray("80020"), null);
    assertEquals(action.getUser().getAddresses().size(), 1);
    assertNull(action.getUser().getAddresses().get("work"));
    assertEquals(action.getUser().getAddresses().get("home").getCity(), "Broomfield");
    assertEquals(action.getUser().getAddresses().get("home").getState(), "CO");
    assertEquals(action.getUser().getAddresses().get("home").getStreet(), "Test");
    assertEquals(action.getUser().getAddresses().get("home").getZipcode(), "80020");

    // Test empty is null
    evaluator.setValue("user.addresses['home'].zipcode", action, ArrayUtils.toArray(""), null);
    assertNull(action.getUser().getAddresses().get("home").getZipcode());

    // Test special value is a null key
    evaluator.setValue("user.addresses[].zipcode", action, ArrayUtils.toArray("80020"), null);
    assertEquals(action.getUser().getAddresses().get(null).getZipcode(), "80020");

    // Test empty string key, same as null key
    evaluator.setValue("user.addresses[''].zipcode", action, ArrayUtils.toArray("80020"), null);
    assertEquals(action.getUser().getAddresses().get(null).getZipcode(), "80020");

    action.getUser().setSiblings(null);
    evaluator.setValue("user.siblings[0].age", action, ArrayUtils.toArray("34"), null);
    evaluator.setValue("user.siblings[0].name", action, ArrayUtils.toArray("Brett"), null);
    assertTrue(action.getUser().getSiblings() instanceof ArrayList);
    assertEquals(action.getUser().getSiblings().size(), 1);
    assertEquals(action.getUser().getSiblings().get(0).getAge(), (Integer) 34);
    assertEquals(action.getUser().getSiblings().get(0).getName(), "Brett");

    evaluator.setValue("user.securityQuestions[0]", action, ArrayUtils.toArray("What is your pet's name?"), null);
    evaluator.setValue("user.securityQuestions[1]", action, ArrayUtils.toArray("What is your home town?"), null);
    assertEquals(action.getUser().getSecurityQuestions().length, 2);
    assertEquals(action.getUser().getSecurityQuestions()[0], "What is your pet's name?");
    assertEquals(action.getUser().getSecurityQuestions()[1], "What is your home town?");

    action.getUser().setSecurityQuestions();
    evaluator.setValue("user.securityQuestions", action, new String[]{"What is your pet's name?", "What is your home town?"}, null);
    assertEquals(action.getUser().getSecurityQuestions()[0], "What is your pet's name?");
    assertEquals(action.getUser().getSecurityQuestions()[1], "What is your home town?");

    // Test indexed collection property sets (using the indexed property methoods)
    action.getUser().setAddresses(new HashMap<>());
    evaluator.setValue("user.address['home'].city", action, ArrayUtils.toArray("Broomfield"), null);
    evaluator.setValue("user.address['home'].state", action, ArrayUtils.toArray("CO"), null);
    evaluator.setValue("user.address['home'].street", action, ArrayUtils.toArray("Test"), null);
    evaluator.setValue("user.address['home'].zipcode", action, ArrayUtils.toArray("80020"), null);
    assertEquals(action.getUser().getAddresses().size(), 1);
    assertNull(action.getUser().getAddresses().get("work"));
    assertEquals(action.getUser().getAddresses().get("home").getCity(), "Broomfield");
    assertEquals(action.getUser().getAddresses().get("home").getState(), "CO");
    assertEquals(action.getUser().getAddresses().get("home").getStreet(), "Test");
    assertEquals(action.getUser().getAddresses().get("home").getZipcode(), "80020");

    action.getUser().setSiblings(new ArrayList<>());
    evaluator.setValue("user.sibling[0].age", action, ArrayUtils.toArray("34"), null);
    evaluator.setValue("user.sibling[0].name", action, ArrayUtils.toArray("Brett"), null);
    assertEquals(action.getUser().getSiblings().size(), 1);
    assertEquals(action.getUser().getSiblings().get(0).getAge(), (Integer) 34);
    assertEquals(action.getUser().getSiblings().get(0).getName(), "Brett");

    // Test arrays and complex maps
    evaluator.setValue("roleIds", action, ArrayUtils.toArray("1", "2", "3"), null);
    evaluator.setValue("choices['ids']", action, ArrayUtils.toArray("1", "2", "3"), null);
    assertEquals(action.getRoleIds().length, 3);
    assertEquals(action.getRoleIds()[0], 1);
    assertEquals(action.getRoleIds()[1], 2);
    assertEquals(action.getRoleIds()[2], 3);
    assertEquals(action.getChoices().get("ids").size(), 3);
    assertEquals((int) action.getChoices().get("ids").get(0), 1);
    assertEquals((int) action.getChoices().get("ids").get(1), 2);
    assertEquals((int) action.getChoices().get("ids").get(2), 3);

    // Test arrays inside maps
    evaluator.setValue("associations['ids']", action, ArrayUtils.toArray("1", "2", "3"), null);
    assertEquals(action.getAssociations().get("ids").length, 3);
    assertEquals((int) action.getAssociations().get("ids")[0], 1);
    assertEquals((int) action.getAssociations().get("ids")[1], 2);
    assertEquals((int) action.getAssociations().get("ids")[2], 3);
  }

  @Inject
  public void setEvaluator(DefaultExpressionEvaluator evaluator) {
    this.evaluator = evaluator;
  }

//    @Test
//    public void testMvel() throws InterruptedException {
//        // Set cases
//
//        // Get cases
//
//        // Get cases
//
//        Object action = new ActionField();
//        ((ActionField) action).user = new UserField();
//        ((ActionField) action).user.age = 10;
//        long start = System.currentTimeMillis();
//        MVEL.eval("user.age", action);
//        long end = System.currentTimeMillis();
//        System.out.println("MVEL Getting field time was " + (end - start));
//
//        ((ActionField) action).user = new UserField();
//        ((ActionField) action).user.addresses.put("home", new AddressField());
//        ((ActionField) action).user.addresses.get("home").zipcode = "80020";
//        start = System.currentTimeMillis();
//        MVEL.eval("user.addresses['home'].zipcode", action);
//        end = System.currentTimeMillis();
//        System.out.println("MVEL Getting field time was " + (end - start));
//
//        action = new Action();
//        ((Action) action).setUser(new User());
//        ((Action) action).getUser().setAge(10);
//        start = System.currentTimeMillis();
//        MVEL.eval("user.age", action);
//        end = System.currentTimeMillis();
//        System.out.println("MVEL Getting property time was " + (end - start));
//
//        ((Action) action).setUser(new User());
//        ((Action) action).getUser().getAddresses().put("home", new Address());
//        ((Action) action).getUser().getAddresses().get("home").setZipcode("80020");
//        start = System.currentTimeMillis();
//        MVEL.eval("user.addresses['home'].zipcode", action);
//        end = System.currentTimeMillis();
//        System.out.println("MVEL Getting property time was " + (end - start));
//
//        // Loop
//
//        action = new ActionField();
//        ((ActionField) action).user = new UserField();
//        ((ActionField) action).user.addresses.put("home", new AddressField());
//        ((ActionField) action).user.addresses.get("home").zipcode = "80020";
//        start = System.currentTimeMillis();
//        for (int i = 0; i < 50; i++) {
//            MVEL.eval("user.addresses['home'].zipcode", action);
//        }
//        end = System.currentTimeMillis();
//        System.out.println("MVEL 50 times was " + (end - start));
//
//        MVELThread[] threads = new MVELThread[50];
//        for (int i = 0; i < 50; i++) {
//            threads[i] = new MVELThread();
//            threads[i].setName("" + i);
//            threads[i].start();
//        }
//
//        for (int i = 0; i < 50; i++) {
//            threads[i].join();
//        }
//    }

  public class JCThread extends Thread {
    @Override
    public void run() {

      Object action = new ActionField();
      ((ActionField) action).user = new UserField();
      ((ActionField) action).user.addresses.put("home", new AddressField());
      ((ActionField) action).user.addresses.get("home").zipcode = "80020";
      long start = System.currentTimeMillis();
      for (int i = 0; i < 50000; i++) {
        evaluator.getValue("user.addresses['home'].zipcode", action);
      }
      long end = System.currentTimeMillis();
      System.out.println("50000 times for " + this.getName() + " was " + (end - start) + " should be around 6500ms");
    }
  }

//    public class MVELThread extends Thread {
//        @Override
//        public void run() {
//
//            Object action = new ActionField();
//            ((ActionField) action).user = new UserField();
//            ((ActionField) action).user.addresses.put("home", new AddressField());
//            ((ActionField) action).user.addresses.get("home").zipcode = "80020";
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < 50000; i++) {
//                MVEL.eval("user.addresses['home'].zipcode", action);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("MVEL 50000 times for " + this.getName() + " was " + (end - start));
//        }
//    }
}
