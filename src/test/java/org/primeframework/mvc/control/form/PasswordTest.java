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
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.testng.annotations.Test;

import com.google.inject.Inject;

/**
 * This tests the password control.
 *
 * @author Brian Pontarelli
 */
public class PasswordTest extends ControlBaseTest {
  @Inject public Password password;

  @Test
  public void actionLess() {
    ais.setCurrent(new DefaultActionInvocation(null, "/password", null, null));
    new ControlTester(password).
      attr("name", "test").
      attr("class", "css-class").
      attr("value", "password").
      go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
        "<div class=\"css-class-password css-class-input css-class-control password input control\">\n" +
        "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
        "<div class=\"control-container\"><input type=\"password\" class=\"css-class\" id=\"test\" name=\"test\"/></div>\n" +
        "</div>\n");
  }

  @Test
  public void action() {
    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setPassword("Test");

    ais.setCurrent(new DefaultActionInvocation(edit, "/password", null, null));
    new ControlTester(password).
      attr("name", "user.password").
      attr("value", "password").
      go("<input type=\"hidden\" name=\"user.password@param\" value=\"param-value\"/>\n" +
        "<div class=\"password input control\">\n" +
        "<div class=\"label-container\"><label for=\"user_password\" class=\"label\">Password</label></div>\n" +
        "<div class=\"control-container\"><input type=\"password\" id=\"user_password\" name=\"user.password\"/></div>\n" +
        "</div>\n");
  }

  @Test
  public void fieldErrors() {
    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setPassword("Test");

    ais.setCurrent(new DefaultActionInvocation(edit, "/password", null, null));

    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.password", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.password", "fieldError2"));

    new ControlTester(password).
      attr("name", "user.password").
      attr("value", "password").
      go("<input type=\"hidden\" name=\"user.password@param\" value=\"param-value\"/>\n" +
        "<div class=\"password input control\">\n" +
        "<div class=\"label-container\"><label for=\"user_password\" class=\"label\"><span class=\"error\">Password (fieldError1, fieldError2)</span></label></div>\n" +
        "<div class=\"control-container\"><input type=\"password\" id=\"user_password\" name=\"user.password\"/></div>\n" +
        "</div>\n");
  }
}