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

import org.example.action.user.Edit;
import org.example.domain.User;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.testng.annotations.Test;

import com.google.inject.Inject;

/**
 * This tests the text control.
 *
 * @author Brian Pontarelli
 */
public class TextTest extends ControlBaseTest {
  @Inject public Text text;

  @Test
  public void actionLess() {
    ais.setCurrent(new ActionInvocation(null, null, "/text", null, null));
    new ControlTester(text).
      attr("name", "test").
      attr("class", "css-class").
      go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
      "<div class=\"css-class-text css-class-input css-class-control text input control\">\n" +
      "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
      "<div class=\"control-container\"><input type=\"text\" class=\"css-class\" id=\"test\" name=\"test\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void action() {
    Edit action = new Edit();
    action.user = new User();
    action.user.setName("Brian");

    ais.setCurrent(new ActionInvocation(action, null, "/text", null, null));
    new ControlTester(text).
      attr("name", "user.name").
      go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
      "<div class=\"text input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
      "<div class=\"control-container\"><input type=\"text\" id=\"user_name\" name=\"user.name\" value=\"Brian\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void html() {
    Edit action = new Edit();
    action.user = new User();
    action.user.setName("<Brian>");

    ais.setCurrent(new ActionInvocation(action, null, "/text", null, null));
    new ControlTester(text).
      attr("name", "user.name").
      go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
      "<div class=\"text input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
      "<div class=\"control-container\"><input type=\"text\" id=\"user_name\" name=\"user.name\" value=\"&lt;Brian&gt;\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void fieldErrors() {
    Edit action = new Edit();
    action.user = new User();
    action.user.setName("Barry");

    ais.setCurrent(new ActionInvocation(action, null, "/text", null, null));

    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.name", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.name", "fieldError2"));

    new ControlTester(text).
      attr("name", "user.name").
      go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
      "<div class=\"text input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_name\" class=\"label\"><span class=\"error\">Your name (fieldError1, fieldError2)</span></label></div>\n" +
      "<div class=\"control-container\"><input type=\"text\" id=\"user_name\" name=\"user.name\" value=\"Barry\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void defaultValue() {
    Edit action = new Edit();

    ais.setCurrent(new ActionInvocation(action, null, "/text", null, null));

    new ControlTester(text).
      attr("name", "user.name").
      attr("defaultValue", "John").
      go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
      "<div class=\"text input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
      "<div class=\"control-container\"><input type=\"text\" id=\"user_name\" name=\"user.name\" value=\"John\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void hardCodedValue() {
    Edit action = new Edit();
    action.user = new User();
    action.user.setName("Brian");

    ais.setCurrent(new ActionInvocation(action, null, "/text", null, null));

    new ControlTester(text).
      attr("name", "user.name").
      attr("value", "Barry").
      go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
      "<div class=\"text input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
      "<div class=\"control-container\"><input type=\"text\" id=\"user_name\" name=\"user.name\" value=\"Barry\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void labelKey() {
    Edit action = new Edit();
    action.user = new User();
    action.user.setName("Brian");

    ais.setCurrent(new ActionInvocation(action, null, "/text", null, null));

    new ControlTester(text).
      attr("name", "user.name").
      attr("labelKey", "label-key").
      go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
      "<div class=\"text input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Foo bar</label></div>\n" +
      "<div class=\"control-container\"><input type=\"text\" id=\"user_name\" name=\"user.name\" value=\"Brian\"/></div>\n" +
      "</div>\n");
  }
}