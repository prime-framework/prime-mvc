/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.parameter.el;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import static net.java.util.CollectionTools.*;
import org.example.domain.Action;
import org.example.domain.ActionField;
import org.example.domain.Address;
import org.example.domain.AddressField;
import org.example.domain.User;
import org.example.domain.UserField;
import org.jcatapult.test.JCatapultBaseTest;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <p>
 * This class contains tests for the expression evaluator.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultExpressionEvaluatorTest extends JCatapultBaseTest {
    DefaultExpressionEvaluator evaluator;

    @Inject
    public void setEvaluator(DefaultExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Tests getting of bean properties
     */
    @Test
    public void testPropertyGetting() {
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

        assertEquals(32, evaluator.getValue("user.age", action));
        assertEquals("Brian", evaluator.getValue("user.name", action));
        assertTrue((Boolean) evaluator.getValue("user.active", action));

        // Test collection property gets
        Address address = new Address();
        address.setCity("Broomfield");
        address.setState("CO");
        address.setStreet("Test");
        address.setZipcode("80020");
        action.getUser().getAddresses().put("home", address);
        assertNull(evaluator.getValue("user.addresses['work']", action));
        assertEquals("Broomfield", evaluator.getValue("user.addresses['home'].city", action));
        assertEquals("CO", evaluator.getValue("user.addresses['home'].state", action));
        assertEquals("Test", evaluator.getValue("user.addresses['home'].street", action));
        assertEquals("80020", evaluator.getValue("user.addresses['home'].zipcode", action));

        User brother = new User();
        brother.setName("Brett");
        brother.setAge(34);
        user.getSiblings().add(brother);
        assertEquals(34, evaluator.getValue("user.siblings[0].age", action));
        assertEquals("Brett", evaluator.getValue("user.siblings[0].name", action));

        user.setSecurityQuestions(new String[]{"What is your pet's name?", "What is your home town?"});
        assertEquals("What is your pet's name?", evaluator.getValue("user.securityQuestions[0]", action));
        assertEquals("What is your home town?", evaluator.getValue("user.securityQuestions[1]", action));

        // Test indexed collection property gets (using the indexed property methoods)
        assertNull(evaluator.getValue("user.address['work']", action));
        assertEquals("Broomfield", evaluator.getValue("user.address['home'].city", action));
        assertEquals("CO", evaluator.getValue("user.address['home'].state", action));
        assertEquals("Test", evaluator.getValue("user.address['home'].street", action));
        assertEquals("80020", evaluator.getValue("user.address['home'].zipcode", action));

        assertEquals(34, evaluator.getValue("user.sibling[0].age", action));
        assertEquals("Brett", evaluator.getValue("user.sibling[0].name", action));
    }

    /**
     * Tests getting of fields
     */
    @Test
    public void testFieldGetting() {
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

        assertEquals(32, evaluator.getValue("user.age", action));
        assertEquals("Brian", evaluator.getValue("user.name", action));
        assertTrue((Boolean) evaluator.getValue("user.active", action));

        // Test collection property gets
        AddressField address = new AddressField();
        address.city = "Broomfield";
        address.state = "CO";
        address.street = "Test";
        address.zipcode = "80020";
        action.user.addresses.put("home", address);
        assertNull(evaluator.getValue("user.addresses['work']", action));
        assertEquals("Broomfield", evaluator.getValue("user.addresses['home'].city", action));
        assertEquals("CO", evaluator.getValue("user.addresses['home'].state", action));
        assertEquals("Test", evaluator.getValue("user.addresses['home'].street", action));
        assertEquals("80020", evaluator.getValue("user.addresses['home'].zipcode", action));

        UserField brother = new UserField();
        brother.name = "Brett";
        brother.age = 34;
        user.siblings.add(brother);
        assertEquals(34, evaluator.getValue("user.siblings[0].age", action));
        assertEquals("Brett", evaluator.getValue("user.siblings[0].name", action));

        user.securityQuestions = new String[]{"What is your pet's name?", "What is your home town?"};
        assertEquals("What is your pet's name?", evaluator.getValue("user.securityQuestions[0]", action));
        assertEquals("What is your home town?", evaluator.getValue("user.securityQuestions[1]", action));
    }

    /**
     * Tests setting of bean properties
     */
    @Test
    public void testPropertySetting() {
        // Test nested property set and type conversion
        Action action = new Action();
        action.setUser(null);

        evaluator.setValue("user.age", action, array("32"), null);
        evaluator.setValue("user.name", action, array("Brian"), null);
        evaluator.setValue("user.active", action, array("true"), null);
        evaluator.setValue("user.male", action, null, null);
        assertEquals((Integer) 32, action.getUser().getAge());
        assertEquals("Brian", action.getUser().getName());
        assertTrue(action.getUser().isActive());
        assertFalse(action.getUser().isMale());

        // Test collection property sets
        action.getUser().setAddresses(null);
        evaluator.setValue("user.addresses['home'].city", action, array("Broomfield"), null);
        evaluator.setValue("user.addresses['home'].state", action, array("CO"), null);
        evaluator.setValue("user.addresses['home'].street", action, array("Test"), null);
        evaluator.setValue("user.addresses['home'].zipcode", action, array("80020"), null);
        assertEquals(1, action.getUser().getAddresses().size());
        assertNull(action.getUser().getAddresses().get("work"));
        assertEquals("Broomfield", action.getUser().getAddresses().get("home").getCity());
        assertEquals("CO", action.getUser().getAddresses().get("home").getState());
        assertEquals("Test", action.getUser().getAddresses().get("home").getStreet());
        assertEquals("80020", action.getUser().getAddresses().get("home").getZipcode());

        // Test empty is null
        evaluator.setValue("user.addresses['home'].zipcode", action, array(""), null);
        assertNull(action.getUser().getAddresses().get("home").getZipcode());

        action.getUser().setSiblings(null);
        evaluator.setValue("user.siblings[0].age", action, array("34"), null);
        evaluator.setValue("user.siblings[0].name", action, array("Brett"), null);
        assertTrue(action.getUser().getSiblings() instanceof ArrayList);
        assertEquals(1, action.getUser().getSiblings().size());
        assertEquals((Integer) 34, action.getUser().getSiblings().get(0).getAge());
        assertEquals("Brett", action.getUser().getSiblings().get(0).getName());

        evaluator.setValue("user.securityQuestions[0]", action, array("What is your pet's name?"), null);
        evaluator.setValue("user.securityQuestions[1]", action, array("What is your home town?"), null);
        assertEquals(2, action.getUser().getSecurityQuestions().length);
        assertEquals("What is your pet's name?", action.getUser().getSecurityQuestions()[0]);
        assertEquals("What is your home town?", action.getUser().getSecurityQuestions()[1]);

        action.getUser().setSecurityQuestions(null);
        evaluator.setValue("user.securityQuestions", action, new String[]{"What is your pet's name?", "What is your home town?"}, null);
        assertEquals("What is your pet's name?", action.getUser().getSecurityQuestions()[0]);
        assertEquals("What is your home town?", action.getUser().getSecurityQuestions()[1]);

        // Test indexed collection property sets (using the indexed property methoods)
        action.getUser().setAddresses(new HashMap<String, Address>());
        evaluator.setValue("user.address['home'].city", action, array("Broomfield"), null);
        evaluator.setValue("user.address['home'].state", action, array("CO"), null);
        evaluator.setValue("user.address['home'].street", action, array("Test"), null);
        evaluator.setValue("user.address['home'].zipcode", action, array("80020"), null);
        assertEquals(1, action.getUser().getAddresses().size());
        assertNull(action.getUser().getAddresses().get("work"));
        assertEquals("Broomfield", action.getUser().getAddresses().get("home").getCity());
        assertEquals("CO", action.getUser().getAddresses().get("home").getState());
        assertEquals("Test", action.getUser().getAddresses().get("home").getStreet());
        assertEquals("80020", action.getUser().getAddresses().get("home").getZipcode());

        action.getUser().setSiblings(new ArrayList<User>());
        evaluator.setValue("user.sibling[0].age", action, array("34"), null);
        evaluator.setValue("user.sibling[0].name", action, array("Brett"), null);
        assertEquals(1, action.getUser().getSiblings().size());
        assertEquals((Integer) 34, action.getUser().getSiblings().get(0).getAge());
        assertEquals("Brett", action.getUser().getSiblings().get(0).getName());

        // Test arrays and complex maps
        evaluator.setValue("roleIds", action, array("1", "2", "3"), null);
        evaluator.setValue("choices['ids']", action, array("1", "2", "3"), null);
        assertEquals(3, action.getRoleIds().length);
        assertEquals(1, action.getRoleIds()[0]);
        assertEquals(2, action.getRoleIds()[1]);
        assertEquals(3, action.getRoleIds()[2]);
        assertEquals(3, action.getChoices().get("ids").size());
        assertEquals(1, (int) action.getChoices().get("ids").get(0));
        assertEquals(2, (int) action.getChoices().get("ids").get(1));
        assertEquals(3, (int) action.getChoices().get("ids").get(2));

        // Test arrays inside maps
        evaluator.setValue("associations['ids']", action, array("1", "2", "3"), null);
        assertEquals(3, action.getAssociations().get("ids").length);
        assertEquals(1, (int) action.getAssociations().get("ids")[0]);
        assertEquals(2, (int) action.getAssociations().get("ids")[1]);
        assertEquals(3, (int) action.getAssociations().get("ids")[2]);
    }

    /**
     * Tests getting of fields
     */
    @Test
    public void testFieldSetting() {
        // Test nested property set and type conversion
        ActionField action = new ActionField();
        evaluator.setValue("user.age", action, array("32"), null);
        evaluator.setValue("user.name", action, array("Brian"), null);
        evaluator.setValue("user.active", action, array("true"), null);
        assertEquals((Integer) 32, action.user.age);
        assertEquals("Brian", action.user.name);
        assertTrue(action.user.active);

        // Test collection property sets
        action.user.addresses = null;
        evaluator.setValue("user.addresses['home'].city", action, array("Broomfield"), null);
        evaluator.setValue("user.addresses['home'].state", action, array("CO"), null);
        evaluator.setValue("user.addresses['home'].street", action, array("Test"), null);
        evaluator.setValue("user.addresses['home'].zipcode", action, array("80020"), null);
        assertEquals(1, action.user.addresses.size());
        assertNull(action.user.addresses.get("work"));
        assertEquals("Broomfield", action.user.addresses.get("home").city);
        assertEquals("CO", action.user.addresses.get("home").state);
        assertEquals("Test", action.user.addresses.get("home").street);
        assertEquals("80020", action.user.addresses.get("home").zipcode);

        evaluator.setValue("user.siblings[0].age", action, array("34"), null);
        evaluator.setValue("user.siblings[0].name", action, array("Brett"), null);
        assertEquals(1, action.user.siblings.size());
        assertEquals((Integer) 34, action.user.siblings.get(0).age);
        assertEquals("Brett", action.user.siblings.get(0).name);

        evaluator.setValue("user.securityQuestions[0]", action, array("What is your pet's name?"), null);
        evaluator.setValue("user.securityQuestions[1]", action, array("What is your home town?"), null);
        assertEquals(2, action.user.securityQuestions.length);
        assertEquals("What is your pet's name?", action.user.securityQuestions[0]);
        assertEquals("What is your home town?", action.user.securityQuestions[1]);

        action.user.securityQuestions = null;
        evaluator.setValue("user.securityQuestions", action, new String[]{"What is your pet's name?", "What is your home town?"}, null);
        assertEquals(2, action.user.securityQuestions.length);
        assertEquals("What is your pet's name?", action.user.securityQuestions[0]);
        assertEquals("What is your home town?", action.user.securityQuestions[1]);
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

    /**
     * Tests setting a collection with a single value onto a plain field.
     */
    @Test
    public void testFieldSettingWithCollectionSingleValue() {
        // Test nested property set and type conversion
        ActionField action = new ActionField();
        evaluator.setValue("user.name", action, asList("Brian"));
        evaluator.setValue("user.active", action, array("true"), null);
        assertEquals("Brian", action.user.name);
    }

    /**
     * Test expansion.
     */
    @Test
    public void testExpansion() {
        // Test nested property set and type conversion
        ActionField action = new ActionField();
        action.user = new UserField();
        action.user.name = "Fred";

        String result = evaluator.expand("My name is ${user.name}", action);
        assertEquals("My name is Fred", result);
    }

    @Test
    @Ignore
    public void testPerformance() throws InterruptedException {
        // Set cases

        Object action = new ActionField();
        long start = System.currentTimeMillis();
        evaluator.setValue("user.age", action, array("32"), null);
        long end = System.currentTimeMillis();
        System.out.println("Setting field time was " + (end - start));

        start = System.currentTimeMillis();
        evaluator.setValue("user.addresses['home'].zipcode", action, array("80020"), null);
        end = System.currentTimeMillis();
        System.out.println("Setting field time was " + (end - start));

        action = new Action();
        start = System.currentTimeMillis();
        evaluator.setValue("user.age", action, array("32"), null);
        end = System.currentTimeMillis();
        System.out.println("Setting property time was " + (end - start));

        start = System.currentTimeMillis();
        evaluator.setValue("user.addresses['home'].zipcode", action, array("80020"), null);
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
