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

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.config.ActionConfigurationBuilder;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.content.binary.BinaryActionConfigurator;
import org.primeframework.mvc.content.json.JacksonActionConfigurator;
import org.primeframework.mvc.parameter.annotation.NamedParameter;
import org.primeframework.mvc.util.DefaultURIBuilder;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Lyle Schemmerling
 */
public class NamedParameterHandlerTest {

  @Test
  public void test() {
    var builder = new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(), new BinaryActionConfigurator())));
    buildExpectException(builder, InvalidGetterAction.class, "The action class [%s] has a NamedParameter annotation with name [getAnnotation] on method [foo] which is not a valid java bean property method.");

    buildExpectException(builder, InvalidSetterAction.class, "The action class [%s] has a NamedParameter annotation with name [setAnnotation] on method [bar] which is not a valid java bean property method.");

    buildExpectException(builder, PropClashWithFieldAction.class, "The action class [%s] has a method [setBar] annotated with NamedParameter for name [target] collides with defined field named [target].");

    buildExpectException(builder, MultiplePropsAction.class, "The action class [%s] has more than one property annotated with NamedParameter for name [target]. You may not use this annotation on more than one property in the same class with the same parameter name.");

    buildExpectException(builder, FieldClashWithFieldAction.class, "The action class [%s] has a NamedParameter annotated field [bar] whose name collides with defined field [foo].");

    builder.build(OkayAction.class);
  }

  private void buildExpectException(ActionConfigurationBuilder builder, Class<?> clazz, String msg) {
    try {
      builder.build(clazz);
    } catch (PrimeException e) {
      assertEquals(String.format(msg, clazz), e.getMessage());
      return;
    }
    fail("Builder should have thrown exception");
  }

  @Action
  private static class FieldClashWithFieldAction {
    @NamedParameter("foo") public String bar;

    public String foo;

    public String get() {
      return "success";
    }
  }

  @Action
  private static class InvalidGetterAction {
    private String target;

    @NamedParameter("getAnnotation")
    public String foo() {
      return target;
    }

    public String get() {
      return "success";
    }
  }

  @Action
  private static class InvalidSetterAction {

    @NamedParameter("setAnnotation")
    public void bar(String target) {
    }

    public String get() {
      return "success";
    }
  }

  @Action
  private static class MultiplePropsAction {

    public String get() {
      return "success";
    }

    @NamedParameter("target")
    public void setBar(String target) {
    }

    @NamedParameter("target")
    public void setFoo(String target) {
    }
  }

  @Action
  private static class OkayAction {
    @NamedParameter("buzz") public String foo;

    private String bar;

    public String get() {
      return "success";
    }

    @NamedParameter("baz")
    public String getBar() {
      return bar;
    }

    @NamedParameter("baz")
    public void setBar(String bar) {
      this.bar = bar;
    }
  }

  @Action
  private static class PropClashWithFieldAction {
    public String target;

    public String get() {
      return "success";
    }

    @NamedParameter("target")
    public void setBar(String target) {
    }
  }
}
