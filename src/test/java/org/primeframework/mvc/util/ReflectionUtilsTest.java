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
import org.example.action.PostValidationOrderChild;
import org.example.action.PostValidationOrderNoAnnotationChild;
import org.example.action.PostValidationOrderOverrideChild;
import org.example.action.PreValidationOrderChild;
import org.example.action.PreValidationOrderNoAnnotationChild;
import org.example.action.PreValidationOrderOverrideChild;
import org.example.action.ValidationOrderChild;
import org.example.action.ValidationOrderNoAnnotationChild;
import org.example.action.ValidationOrderOverrideChild;
import org.example.domain.InvalidJavaBeanGetter;
import org.example.domain.InvalidJavaBeanSetter;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;
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
  public void validationMethodOrdering_differentNames() throws Exception {
    // Base declares zzzValidateA, zzzValidateB (depth=1); child declares aaaValidateC, aaaValidateD (depth=0).
    // The zzz/aaa prefixes make the depth-first ordering visually obvious: base's zzz methods execute
    // before child's aaa methods even though zzz sorts after aaa alphabetically.
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(ValidationOrderChild.class, ValidationMethod.class);
    assertEquals(methods, asList(
        ValidationOrderChild.class.getMethod("zzzValidateA"),
        ValidationOrderChild.class.getMethod("zzzValidateB"),
        ValidationOrderChild.class.getMethod("aaaValidateC"),
        ValidationOrderChild.class.getMethod("aaaValidateD")
    ));
  }

  @Test
  public void validationMethodOrdering_override_reannotated() throws Exception {
    // Child re-annotates zzzValidateA — getMethods() returns only the child's override (depth=0).
    // zzzValidateB is not overridden so it remains on the base (depth=1) and executes first.
    // Expected: [zzzValidateB (base, depth=1), zzzValidateA (child, depth=0)]
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(ValidationOrderOverrideChild.class, ValidationMethod.class);
    assertEquals(methods, asList(
        ValidationOrderOverrideChild.class.getMethod("zzzValidateB"),
        ValidationOrderOverrideChild.class.getMethod("zzzValidateA")
    ));
  }

  @Test
  public void validationMethodOrdering_override_annotationLost() throws Exception {
    // Child overrides zzzValidateA WITHOUT re-adding @ValidationMethod. Java does not inherit method
    // annotations, so getMethods() returns the child's unannotated override and zzzValidateA disappears
    // from the annotated method list entirely. Only zzzValidateB (base, depth=1) remains.
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(ValidationOrderNoAnnotationChild.class, ValidationMethod.class);
    assertEquals(methods, asList(
        ValidationOrderNoAnnotationChild.class.getMethod("zzzValidateB")
    ));
  }

  @Test
  public void preValidationMethodOrdering_differentNames() throws Exception {
    // Base declares zzzPreA, zzzPreB (depth=1); child declares aaaPreC, aaaPreD (depth=0).
    // The zzz/aaa prefixes make the depth-first ordering visually obvious: base's zzz methods execute
    // before child's aaa methods even though zzz sorts after aaa alphabetically.
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(PreValidationOrderChild.class, PreValidationMethod.class);
    assertEquals(methods, asList(
        PreValidationOrderChild.class.getMethod("zzzPreA"),
        PreValidationOrderChild.class.getMethod("zzzPreB"),
        PreValidationOrderChild.class.getMethod("aaaPreC"),
        PreValidationOrderChild.class.getMethod("aaaPreD")
    ));
  }

  @Test
  public void preValidationMethodOrdering_override_reannotated() throws Exception {
    // Child re-annotates zzzPreA — getMethods() returns only the child's override (depth=0).
    // zzzPreB is not overridden so it remains on the base (depth=1) and executes first.
    // Expected: [zzzPreB (base, depth=1), zzzPreA (child, depth=0)]
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(PreValidationOrderOverrideChild.class, PreValidationMethod.class);
    assertEquals(methods, asList(
        PreValidationOrderOverrideChild.class.getMethod("zzzPreB"),
        PreValidationOrderOverrideChild.class.getMethod("zzzPreA")
    ));
  }

  @Test
  public void preValidationMethodOrdering_override_annotationLost() throws Exception {
    // Child overrides zzzPreA WITHOUT re-adding @PreValidationMethod. Java does not inherit method
    // annotations, so getMethods() returns the child's unannotated override and zzzPreA disappears
    // from the annotated method list entirely. Only zzzPreB (base, depth=1) remains.
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(PreValidationOrderNoAnnotationChild.class, PreValidationMethod.class);
    assertEquals(methods, asList(
        PreValidationOrderNoAnnotationChild.class.getMethod("zzzPreB")
    ));
  }

  @Test
  public void postValidationMethodOrdering_differentNames() throws Exception {
    // Base declares zzzPostA, zzzPostB (depth=1); child declares aaaPostC, aaaPostD (depth=0).
    // The zzz/aaa prefixes make the depth-first ordering visually obvious: base's zzz methods execute
    // before child's aaa methods even though zzz sorts after aaa alphabetically.
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(PostValidationOrderChild.class, PostValidationMethod.class);
    assertEquals(methods, asList(
        PostValidationOrderChild.class.getMethod("zzzPostA"),
        PostValidationOrderChild.class.getMethod("zzzPostB"),
        PostValidationOrderChild.class.getMethod("aaaPostC"),
        PostValidationOrderChild.class.getMethod("aaaPostD")
    ));
  }

  @Test
  public void postValidationMethodOrdering_override_reannotated() throws Exception {
    // Child re-annotates zzzPostA — getMethods() returns only the child's override (depth=0).
    // zzzPostB is not overridden so it remains on the base (depth=1) and executes first.
    // Expected: [zzzPostB (base, depth=1), zzzPostA (child, depth=0)]
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(PostValidationOrderOverrideChild.class, PostValidationMethod.class);
    assertEquals(methods, asList(
        PostValidationOrderOverrideChild.class.getMethod("zzzPostB"),
        PostValidationOrderOverrideChild.class.getMethod("zzzPostA")
    ));
  }

  @Test
  public void postValidationMethodOrdering_override_annotationLost() throws Exception {
    // Child overrides zzzPostA WITHOUT re-adding @PostValidationMethod. Java does not inherit method
    // annotations, so getMethods() returns the child's unannotated override and zzzPostA disappears
    // from the annotated method list entirely. Only zzzPostB (base, depth=1) remains.
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(PostValidationOrderNoAnnotationChild.class, PostValidationMethod.class);
    assertEquals(methods, asList(
        PostValidationOrderNoAnnotationChild.class.getMethod("zzzPostB")
    ));
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
