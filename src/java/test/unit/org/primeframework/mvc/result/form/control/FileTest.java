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
package org.primeframework.mvc.result.form.control;

import org.example.action.user.Edit;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.result.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p> This tests the file control. </p>
 *
 * @author Brian Pontarelli
 */
public class FileTest extends ControlBaseTest {
  @Inject public File file;

  @Test
  public void testActionLess() {
    ais.setCurrent(new DefaultActionInvocation(null, "/file", null, null));

    run(file,
      mapNV("name", "test", "class", "css-class", "bundle", "/file-bundle"),
      null, "<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
      "<div class=\"css-class-file css-class-input css-class-control file input control\">\n" +
      "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
      "<div class=\"control-container\"><input type=\"file\" class=\"css-class\" id=\"test\" name=\"test\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void testAction() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/file", null, null));

    run(file,
      mapNV("name", "user.profile"),
      null, "<input type=\"hidden\" name=\"user.profile@param\" value=\"param-value\"/>\n" +
      "<div class=\"file input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_profile\" class=\"label\">Your profile</label></div>\n" +
      "<div class=\"control-container\"><input type=\"file\" id=\"user_profile\" name=\"user.profile\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void htmlLabel() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/file", null, null));

    // This verifies that HTML is left in for labels. That way people can style their labels in the message properties files
    run(file,
      mapNV("name", "user.name"),
      null, "<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
      "<div class=\"file input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_name\" class=\"label\"><Name></label></div>\n" +
      "<div class=\"control-container\"><input type=\"file\" id=\"user_name\" name=\"user.name\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void testFieldErrors() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/file", null, null));

    messageStore.addFieldError(MessageScope.REQUEST, "user.profile", "fieldError1");
    messageStore.addFieldError(MessageScope.REQUEST, "user.profile", "fieldError2");

    run(file,
      mapNV("name", "user.profile"),
      null, "<input type=\"hidden\" name=\"user.profile@param\" value=\"param-value\"/>\n" +
      "<div class=\"file input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_profile\" class=\"label\"><span class=\"error\">Your profile (Profile is required, Profile must be cool)</span></label></div>\n" +
      "<div class=\"control-container\"><input type=\"file\" id=\"user_profile\" name=\"user.profile\"/></div>\n" +
      "</div>\n");
  }
}