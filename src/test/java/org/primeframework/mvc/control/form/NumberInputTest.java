/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.control.form;

import com.google.inject.Inject;
import org.example.action.user.EditAction;
import org.example.domain.User;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.testng.annotations.Test;

/**
 * This tests the number control.
 *
 * @author Daniel DeGroff
 */
public class NumberInputTest extends ControlBaseTest {
  @Inject public NumberInput number;

  @Test
  public void action() {
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAge(42);

    ais.setCurrent(new ActionInvocation(action, null, "/number", null, null));
    new ControlTester(number).
                                 attr("name", "user.age").
                                 go("<input type=\"hidden\" name=\"user.age@param\" value=\"param-value\"/>\n" +
                                     "<div class=\"text input control\">\n" +
                                     "<div class=\"label-container\"><label for=\"user_age\" class=\"label\">Your age</label></div>\n" +
                                     "<div class=\"control-container\"><input type=\"number\" id=\"user_age\" name=\"user.age\" value=\"42\"/></div>\n" +
                                     "</div>\n");
  }

  @Test
  public void actionLess() {
    ais.setCurrent(new ActionInvocation(null, null, "/number", null, null));
    new ControlTester(number).
                                 attr("name", "test").
                                 attr("class", "css-class").
                                 go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
                                     "<div class=\"css-class-text css-class-input css-class-control text input control\">\n" +
                                     "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
                                     "<div class=\"control-container\"><input type=\"number\" class=\"css-class\" id=\"test\" name=\"test\"/></div>\n" +
                                     "</div>\n");
  }

  @Test
  public void defaultValue() {
    EditAction action = new EditAction();

    ais.setCurrent(new ActionInvocation(action, null, "/number", null, null));

    new ControlTester(number).
                                 attr("name", "user.age").
                                 attr("defaultValue", "0").
                                 go("<input type=\"hidden\" name=\"user.age@param\" value=\"param-value\"/>\n" +
                                     "<div class=\"text input control\">\n" +
                                     "<div class=\"label-container\"><label for=\"user_age\" class=\"label\">Your age</label></div>\n" +
                                     "<div class=\"control-container\"><input type=\"number\" id=\"user_age\" name=\"user.age\" value=\"0\"/></div>\n" +
                                     "</div>\n");
  }

  @Test
  public void fieldErrors() {
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAge(42);

    ais.setCurrent(new ActionInvocation(action, null, "/number", null, null));

    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.age", "code1", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.age", "code2", "fieldError2"));

    new ControlTester(number).
                                 attr("name", "user.age").
                                 go("<input type=\"hidden\" name=\"user.age@param\" value=\"param-value\"/>\n" +
                                     "<div class=\"text input control\">\n" +
                                     "<div class=\"label-container\"><label for=\"user_age\" class=\"label\"><span class=\"error\">Your age (fieldError1, fieldError2)</span></label></div>\n" +
                                     "<div class=\"control-container\"><input type=\"number\" id=\"user_age\" name=\"user.age\" value=\"42\"/></div>\n" +
                                     "</div>\n");
  }

  @Test
  public void labelKey() {
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAge(42);

    ais.setCurrent(new ActionInvocation(action, null, "/number", null, null));

    new ControlTester(number).
                                 attr("name", "user.age").
                                 attr("labelKey", "label-key").
                                 go("<input type=\"hidden\" name=\"user.age@param\" value=\"param-value\"/>\n" +
                                     "<div class=\"text input control\">\n" +
                                     "<div class=\"label-container\"><label for=\"user_age\" class=\"label\">Foo bar</label></div>\n" +
                                     "<div class=\"control-container\"><input type=\"number\" id=\"user_age\" name=\"user.age\" value=\"42\"/></div>\n" +
                                     "</div>\n");
  }
}