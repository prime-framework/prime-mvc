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
package org.primeframework.mvc.control.form.control;

import org.example.action.user.Edit;
import org.example.domain.User;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.control.form.Password;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.control.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p> This tests the password control. </p>
 *
 * @author Brian Pontarelli
 */
public class PasswordTest extends ControlBaseTest {
  @Inject public Password password;

  @Test
  public void testActionLess() {
    ais.setCurrent(new DefaultActionInvocation(null, "/password", null, null));
    run(password,
      mapNV("name", "test", "class", "css-class", "value", "password", "bundle", "/password-bundle"),
      null, "<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
      "<div class=\"css-class-password css-class-input css-class-control password input control\">\n" +
      "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
      "<div class=\"control-container\"><input type=\"password\" class=\"css-class\" id=\"test\" name=\"test\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void testAction() {
    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setPassword("Test");

    ais.setCurrent(new DefaultActionInvocation(edit, "/password", null, null));
    run(password,
      mapNV("name", "user.password", "value", "password"),
      null, "<input type=\"hidden\" name=\"user.password@param\" value=\"param-value\"/>\n" +
      "<div class=\"password input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_password\" class=\"label\">Password</label></div>\n" +
      "<div class=\"control-container\"><input type=\"password\" id=\"user_password\" name=\"user.password\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void testFieldErrors() {
    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setPassword("Test");

    ais.setCurrent(new DefaultActionInvocation(edit, "/password", null, null));

    messageStore.addFieldError(MessageScope.REQUEST, "user.password", "fieldError1");
    messageStore.addFieldError(MessageScope.REQUEST, "user.password", "fieldError2");

    run(password,
      mapNV("name", "user.password", "value", "password"),
      null, "<input type=\"hidden\" name=\"user.password@param\" value=\"param-value\"/>\n" +
      "<div class=\"password input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_password\" class=\"label\"><span class=\"error\">Password (Password is required, Password must be cool)</span></label></div>\n" +
      "<div class=\"control-container\"><input type=\"password\" id=\"user_password\" name=\"user.password\"/></div>\n" +
      "</div>\n");
  }
}