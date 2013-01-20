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
import org.joda.time.DateTime;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;

/**
 * This tests the months select control.
 *
 * @author Brian Pontarelli
 */
public class YearSelectTest extends ControlBaseTest {
  @Inject public YearSelect yearSelect;

  @Test
  public void actionLess() {
    int year = DateTime.now().getYear();
    ais.setCurrent(new ActionInvocation(null, null, "/years-select", null, null));
    new ControlTester(yearSelect).
      attr("name", "test").
      attr("class", "css-class").
      go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
      "<div class=\"css-class-select css-class-input css-class-control select input control\">\n" +
      "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
      "<div class=\"control-container\">\n" +
      "<select class=\"css-class\" id=\"test\" name=\"test\">\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year++ + "</option>\n" +
      "<option value=\"" + year + "\">" + year + "</option>\n" +
      "</select>\n" +
      "</div>\n" +
      "</div>\n");
  }

  @Test
  public void action() {
    Edit edit = new Edit();
    edit.user = new User();
    edit.user.setYear(2003);

    ais.setCurrent(new ActionInvocation(edit, null, "/years-select", null, null));
    new ControlTester(yearSelect).
      attr("name", "user.year").
      attr("startYear", 2001).
      attr("endYear", 2006).
      go("<input type=\"hidden\" name=\"user.year@param\" value=\"param-value\"/>\n" +
      "<div class=\"select input control\">\n" +
      "<div class=\"label-container\"><label for=\"user_year\" class=\"label\">Year</label></div>\n" +
      "<div class=\"control-container\">\n" +
      "<select id=\"user_year\" name=\"user.year\">\n" +
      "<option value=\"2001\">2001</option>\n" +
      "<option value=\"2002\">2002</option>\n" +
      "<option value=\"2003\" selected=\"selected\">2003</option>\n" +
      "<option value=\"2004\">2004</option>\n" +
      "<option value=\"2005\">2005</option>\n" +
      "<option value=\"2006\">2006</option>\n" +
      "</select>\n" +
      "</div>\n" +
      "</div>\n");
  }
}
