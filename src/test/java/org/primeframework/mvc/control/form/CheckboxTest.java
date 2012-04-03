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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.action.user.Edit;
import org.example.domain.User;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.testng.annotations.Test;

import com.google.inject.Inject;

/**
 * This tests the checkbox control.
 *
 * @author Brian Pontarelli
 */
public class CheckboxTest extends ControlBaseTest {
  @Inject public Checkbox checkbox;

  @Test
  public void actionLess() {
    ais.setCurrent(new DefaultActionInvocation(null, null, "/checkbox", null, null));

    new ControlTester(checkbox).
      attr("name", "test").
      attr("value", "test-value").
      attr("required", true).
      attr("class", "css-class").
      go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
      "<div class=\"css-class-checkbox css-class-input css-class-control checkbox input control\">\n" +
      "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test<span class=\"required\">*</span></label></div>\n" +
      "<div class=\"control-container\"><input type=\"checkbox\" class=\"css-class\" id=\"test\" name=\"test\" value=\"test-value\"/><input type=\"hidden\" name=\"__cb_test\" value=\"\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void action() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, null, "/checkbox", null, null));

    // Test booleans
    action.user = new User();
    action.user.setMale(true);
    testAction("user.male", true, "true");
    action.user.setMale(false);
    testAction("user.male", false, "true");

    // Test arrays
    action.user.setIntIDs(new int[]{1, 2, 3});
    testAction("user.intIDs", true, "1");
    testAction("user.intIDs", true, "2");
    testAction("user.intIDs", true, "3");
    testAction("user.intIDs", false, "4");

    // Test collection with Strings
    Set<String> set = new HashSet<String>();
    set.add("1");
    set.add("2");
    set.add("3");
    action.user.setSetIDs(set);
    testAction("user.setIDs", true, "1");
    testAction("user.setIDs", true, "2");
    testAction("user.setIDs", true, "3");
    testAction("user.setIDs", false, "4");

    // Test collection with HTML
    set = new HashSet<String>();
    set.add("<br>");
    set.add("<li>");
    set.add("<td>");
    action.user.setSetIDs(set);
    testAction("user.setIDs", true, "<br>");
    testAction("user.setIDs", true, "<li>");
    testAction("user.setIDs", true, "<td>");
    testAction("user.setIDs", false, "<table>");

    // Test collection with Integers
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    list.add(3);
    action.user.setListIDs(list);
    testAction("user.listIDs", true, "1");
    testAction("user.listIDs", true, "2");
    testAction("user.listIDs", true, "3");
    testAction("user.listIDs", false, "4");
  }

  protected void testAction(String property, boolean flag, String value) {
    new ControlTester(checkbox).
      attr("name", property).
      attr("value", value).
      go("<input type=\"hidden\" name=\"" + property + "@param\" value=\"param-value\"/>\n" +
      "<div class=\"checkbox input control\">\n" +
      "<div class=\"label-container\"><label for=\"" + property.replace(".", "_") + "\" class=\"label\">Male?</label></div>\n" +
      "<div class=\"control-container\"><input type=\"checkbox\" " + (flag ? "checked=\"checked\" " : "") +
      "id=\"" + property.replace(".", "_") + "\" name=\"" + property + "\" value=\"" + value.replace("<", "&lt;").replace(">", "&gt;") +
      "\"/><input type=\"hidden\" name=\"__cb_" + property + "\" value=\"\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void defaultChecked() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, null, "/checkbox", null, null));

    new ControlTester(checkbox).
      attr("name", "user.maleWrapper").
      attr("defaultChecked", true).
      attr("value", true).
      go("<input type=\"hidden\" name=\"user.maleWrapper@param\" value=\"param-value\"/>\n" +
      "<div class=\"checkbox input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_maleWrapper\" class=\"label\">Male?</label></div>\n" +
      "<div class=\"control-container\"><input type=\"checkbox\" checked=\"checked\" id=\"user_maleWrapper\" name=\"user.maleWrapper\" value=\"true\"/><input type=\"hidden\" name=\"__cb_user.maleWrapper\" value=\"\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void hardCodedChecked() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, null, "/checkbox", null, null));

    new ControlTester(checkbox).
      attr("name", "user.male").
      attr("checked", true).
      attr("value", "true").
      go("<input type=\"hidden\" name=\"user.male@param\" value=\"param-value\"/>\n" +
      "<div class=\"checkbox input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_male\" class=\"label\">Male?</label></div>\n" +
      "<div class=\"control-container\"><input type=\"checkbox\" checked=\"checked\" id=\"user_male\" name=\"user.male\" value=\"true\"/><input type=\"hidden\" name=\"__cb_user.male\" value=\"\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void fieldErrors() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, null, "/checkbox", null, null));

    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.male", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.male", "fieldError2"));

    new ControlTester(checkbox).
      attr("name", "user.male").
      attr("value", "true").
      go("<input type=\"hidden\" name=\"user.male@param\" value=\"param-value\"/>\n" +
      "<div class=\"checkbox input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_male\" class=\"label\"><span class=\"error\">Male? (fieldError1, fieldError2)</span></label></div>\n" +
      "<div class=\"control-container\"><input type=\"checkbox\" id=\"user_male\" name=\"user.male\" value=\"true\"/><input type=\"hidden\" name=\"__cb_user.male\" value=\"\"/></div>\n" +
      "</div>\n");
  }

  @Test
  public void uncheckedValue() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, null, "/checkbox", null, null));

    new ControlTester(checkbox).
      attr("name", "user.maleWrapper").
      attr("value", "true").
      attr("uncheckedValue", "false").
      go("<input type=\"hidden\" name=\"user.maleWrapper@param\" value=\"param-value\"/>\n" +
      "<div class=\"checkbox input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_maleWrapper\" class=\"label\">Male?</label></div>\n" +
      "<div class=\"control-container\"><input type=\"checkbox\" id=\"user_maleWrapper\" name=\"user.maleWrapper\" value=\"true\"/><input type=\"hidden\" name=\"__cb_user.maleWrapper\" value=\"false\"/></div>\n" +
      "</div>\n");
  }
}