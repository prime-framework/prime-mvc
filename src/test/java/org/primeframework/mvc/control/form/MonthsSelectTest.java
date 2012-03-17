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
import org.testng.annotations.Test;

import com.google.inject.Inject;

/**
 * This tests the months select control.
 *
 * @author Brian Pontarelli
 */
public class MonthsSelectTest extends ControlBaseTest {
  @Inject public MonthsSelect monthsSelect;

  @Test
  public void actionLess() {
    ais.setCurrent(new DefaultActionInvocation(null, "/months-select", null, null));
    new ControlTester(monthsSelect).
      attr("name", "test").
      attr("class", "css-class").
      go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
      "<div class=\"css-class-select css-class-input css-class-control select input control\">\n" +
      "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
      "<div class=\"control-container\">\n" +
      "<select class=\"css-class\" id=\"test\" name=\"test\">\n" +
      "<option value=\"1\">January</option>\n" +
      "<option value=\"2\">February</option>\n" +
      "<option value=\"3\">March</option>\n" +
      "<option value=\"4\">April</option>\n" +
      "<option value=\"5\">May</option>\n" +
      "<option value=\"6\">June</option>\n" +
      "<option value=\"7\">July</option>\n" +
      "<option value=\"8\">August</option>\n" +
      "<option value=\"9\">September</option>\n" +
      "<option value=\"10\">October</option>\n" +
      "<option value=\"11\">November</option>\n" +
      "<option value=\"12\">December</option>\n" +
      "</select>\n" +
      "</div>\n" +
      "</div>\n");
  }

  @Test
  public void action() {
    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setMonth(5);

    ais.setCurrent(new DefaultActionInvocation(edit, "/months-select", null, null));
    new ControlTester(monthsSelect).
      attr("name", "user.month").
      go("<input type=\"hidden\" name=\"user.month@param\" value=\"param-value\"/>\n" +
      "<div class=\"select input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_month\" class=\"label\">Month</label></div>\n" +
      "<div class=\"control-container\">\n" +
      "<select id=\"user_month\" name=\"user.month\">\n" +
      "<option value=\"1\">January</option>\n" +
      "<option value=\"2\">February</option>\n" +
      "<option value=\"3\">March</option>\n" +
      "<option value=\"4\">April</option>\n" +
      "<option value=\"5\" selected=\"selected\">May</option>\n" +
      "<option value=\"6\">June</option>\n" +
      "<option value=\"7\">July</option>\n" +
      "<option value=\"8\">August</option>\n" +
      "<option value=\"9\">September</option>\n" +
      "<option value=\"10\">October</option>\n" +
      "<option value=\"11\">November</option>\n" +
      "<option value=\"12\">December</option>\n" +
      "</select>\n" +
      "</div>\n" +
      "</div>\n");
  }
}