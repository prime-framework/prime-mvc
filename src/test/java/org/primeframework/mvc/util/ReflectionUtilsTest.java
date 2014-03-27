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

import org.example.action.Extension;
import org.example.action.ExtensionInheritance;
import org.example.domain.InvalidJavaBeanGetter;
import org.example.domain.InvalidJavaBeanSetter;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

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
  public void methodOrdering() throws Exception {
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(ExtensionInheritance.class, PostParameterMethod.class);
    assertEquals(methods, asList(Extension.class.getMethod("method"), Extension.class.getMethod("method1"), ExtensionInheritance.class.getMethod("method2"), ExtensionInheritance.class.getMethod("method3")));
  }
}
