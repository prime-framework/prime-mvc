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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import org.example.action.Extension;
import org.example.action.ExtensionInheritanceAction;
import org.example.domain.InvalidJavaBeanGetter;
import org.example.domain.InvalidJavaBeanSetter;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
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
    assertEquals(methods, asList(Extension.class.getMethod("method"), Extension.class.getMethod("method1"), ExtensionInheritanceAction.class.getMethod("method2"), ExtensionInheritanceAction.class.getMethod("method3")));
  }
}
