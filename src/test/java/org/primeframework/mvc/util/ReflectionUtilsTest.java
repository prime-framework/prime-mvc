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
package org.primeframework.mvc.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.example.action.Extension;
import org.example.action.ExtensionInheritanceAction;
import org.example.domain.InvalidJavaBeanGetter;
import org.example.domain.InvalidJavaBeanSetter;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * @author Brian Pontarelli
 */
public class ReflectionUtilsTest {
  @Test
  public void badGetter() {
    try {
      ReflectionUtils.findPropertyInfo(InvalidJavaBeanGetter.class);
      fail("Should have thrown an exception");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void badSetter() {
    try {
      ReflectionUtils.findPropertyInfo(InvalidJavaBeanSetter.class);
      fail("Should have thrown an exception");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void findMethodsWithAnnotation() {
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(C1.class, Foo.class);
    assertEquals(methods.get(0).getName(), "method3");
    assertEquals(methods.get(1).getName(), "method1");
    assertEquals(methods.get(2).getName(), "method2");
  }

  @Test
  public void localeIssues() {
    // This is currently expected to fail, we could fix this, but it may be failing correctly.
    // This class has many duplicate get methods such as :
    // - String getDisplayName(Locale inLocale)
    // - String getDisplayName()
    //
    // The Java Bean spec (to me) isn't clear that this is an issue, but it does indicate that a matching 'get<Property Name>' and 'set<Property Name>' indicates
    // a read / write property. It doesn't address exactly what happens when you have a 'get' that takes a parameter.
    //
    // Since a method is invoked using `Method.invoke` which takes a var-args parameter, I would think this is ok to allow. I have tested with a locale selector and it seems to work ok
    // if I modify the validation for the 'existingMethod' to compare argument counts to the existing.
    //
    // https://download.oracle.com/otndocs/jcp/7224-javabeans-1.01-fr-spec-oth-JSpec/
    //
    try {
      ReflectionUtils.findPropertyInfo(Locale.class);
      fail("Should have thrown an exception");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void methodOrdering() throws Exception {
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(ExtensionInheritanceAction.class, PostParameterMethod.class);
    System.out.println(methods);
    assertEquals(methods, asList(Extension.class.getMethod("method"), Extension.class.getMethod("method1"), ExtensionInheritanceAction.class.getMethod("method2"), ExtensionInheritanceAction.class.getMethod("method3")));
  }

  @Test
  public void setNull() throws Exception {
    Field field;
    User user;

    // Set a final SortedSet using a null
    user = new User();
    try {
      field = User.class.getField("roles");
      ReflectionUtils.setField(field, user, null);
      fail("Should have thrown an exception, you can't set a final collection null!");
    } catch (Exception ignore) {
      // Expected
    }

    // Set a non final SortedSet using a null
    field = User.class.getField("notFinalRoles");
    ReflectionUtils.setField(field, user, null);
    assertNull(user.notFinalRoles);
  }

  @Test
  public void setSortedSet() throws Exception {
    User user;
    Field field = User.class.getField("roles");

    // Set a SortedSet using a String array
    user = new User();
    ReflectionUtils.setField(field, user, new String[]{"admin"});
    assertEquals(user.roles.size(), 1);
    assertEquals(user.roles.iterator().next(), "admin");

    // Set a SortedSet using an ArrayList
    user = new User();
    ReflectionUtils.setField(field, user, new ArrayList<>(Collections.singletonList("admin")));
    assertEquals(user.roles.size(), 1);
    assertEquals(user.roles.iterator().next(), "admin");

    // Set a SortedSet using a HashSet
    user = new User();
    ReflectionUtils.setField(field, user, new HashSet<>(Collections.singletonList("admin")));
    assertEquals(user.roles.size(), 1);
    assertEquals(user.roles.iterator().next(), "admin");

    // Set a SortedSet using a TreeSet
    user = new User();
    ReflectionUtils.setField(field, user, new TreeSet<>(Collections.singletonList("admin")));
    assertEquals(user.roles.size(), 1);
    assertEquals(user.roles.iterator().next(), "admin");
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Foo {
  }

  public interface I1 extends I2 {
    @Foo
    default void method1() {
    }
  }

  public interface I2 {
    @Foo
    default void method2() {
    }
  }

  public interface I3 {
    @Foo
    default void method3() {
    }
  }

  public static class C1 extends C2 implements I1 {

  }

  public static class C2 implements I3 {
  }

  private static class User {
    public final SortedSet<String> roles = new TreeSet<>();

    public SortedSet<String> notFinalRoles = new TreeSet<>();
  }
}
