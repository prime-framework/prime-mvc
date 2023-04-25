/*
 * Copyright (c) 2023, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter;

import java.util.Arrays;
import java.util.HashSet;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.content.binary.BinaryActionConfigurator;
import org.primeframework.mvc.content.json.JacksonActionConfigurator;
import org.primeframework.mvc.parameter.annotation.FieldName;
import org.primeframework.mvc.parameter.el.BeanExpressionException;
import org.primeframework.mvc.util.DefaultURIBuilder;
import org.testng.annotations.Test;

/**
 * @author Lyle Schemmerling
 */
public class FieldNameTest {
  @Test
  public void test() {
    var builder = new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(), new BinaryActionConfigurator())));

    expectException(DuplicateSetters1.class, "Invalid JavaBean class [class org.primeframework.mvc.parameter.FieldNameTest$DuplicateSetters1]. Errors are:\n" +
        "[Two or more [set] methods named [setBar] exist. Rename or remove the duplicate method.]");

    expectException(DuplicateSetters2.class, "Invalid JavaBean class [class org.primeframework.mvc.parameter.FieldNameTest$DuplicateSetters2]. Errors are:\n" +
        "[Two or more [set] methods named [setBar] exist. Rename or remove the duplicate method.]");

    expectException(DuplicateFields1.class, "Invalid JavaBean class [class org.primeframework.mvc.parameter.FieldNameTest$DuplicateFields1]. Errors are:\n" +
        "[A field annotated with FieldName] and value of [foo] effectively duplicates the existing field of the same name. Rename or remove the duplicate field.]");

    // No exceptions
    builder.build(OkayAction.class);
  }

  private void expectException(Class<?> clazz, String message) {
    PrimeBaseTest.expectException(
        BeanExpressionException.class,
        () -> new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(), new BinaryActionConfigurator())))
            .build(clazz),
        String.format(message, clazz));
  }

  @Action
  private static class DuplicateFields1 {
    @FieldName("foo")
    public String bar;

    public String foo;

    public String get() {
      return "success";
    }

    public void setFoo(String foo) {
      this.foo = foo;
    }
  }

  @Action
  private static class DuplicateSetters1 {
    public String target;

    public String get() {
      return "success";
    }

    // This is ok, we'll always prefer a setter over a property. This works already if you just make a method named setTarget().
    @FieldName("target")
    public void setBar(String target) {
      this.target = target;
    }

    // This is not valid, because we now have two methods with the same name.
    public void setTarget(String target) {
      this.target = target;
    }
  }

  @Action
  private static class DuplicateSetters2 {
    public String get() {
      return "success";
    }

    @FieldName("target")
    public void setBar(String target) {
    }

    @FieldName("target")
    public void setFoo(String target) {
    }
  }

  @Action
  private static class OkayAction {
    @FieldName("buzz")
    public String foo;

    private String bar;

    public String get() {
      return "success";
    }


    @FieldName("baz")
    public void setBar(String bar) {
      this.bar = bar;
    }
  }
}
