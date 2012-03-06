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
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;

/**
 * This tests the button control.
 *
 * @author Brian Pontarelli
 */
public class ButtonTest extends ControlBaseTest {
  @Inject Button button;

  @Test
  public void actionLess() {
    ais.setCurrent(new DefaultActionInvocation(null, "/button", null, null));
    new ControlTester(button).
      attr("name", "button").
      attr("value", "test-value").
      attr("bundle", "/button-bundle").
      go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
        "<input type=\"hidden\" name=\"__a_button\" value=\"\"/>\n" +
        "<div class=\"button-button button control\">\n" +
        "<div class=\"label-container\"> </div>\n" +
        "<div class=\"control-container\"><input type=\"button\" id=\"button\" name=\"button\" value=\"Button-Bundle\"/></div>\n" +
        "</div>\n");
  }

  @Test
  public void css() {
    ais.setCurrent(new DefaultActionInvocation(new Edit(), "/button", null, null));
    new ControlTester(button).
      attr("name", "button").
      attr("value", "test-value").
      attr("class", "css-class").
      go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
        "<input type=\"hidden\" name=\"__a_button\" value=\"\"/>\n" +
        "<div class=\"css-class-button-button css-class-button css-class-control button-button button control\">\n" +
        "<div class=\"label-container\"> </div>\n" +
        "<div class=\"control-container\"><input type=\"button\" class=\"css-class\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
        "</div>\n");
  }

  @Test
  public void action() {
    ais.setCurrent(new DefaultActionInvocation(new Edit(), "/button", null, null));
    new ControlTester(button).
      attr("name", "button").
      attr("action", "/foo").
      attr("value", "test-value").
      go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
        "<input type=\"hidden\" name=\"__a_button\" value=\"/foo\"/>\n" +
        "<div class=\"button-button button control\">\n" +
        "<div class=\"label-container\"> </div>\n" +
        "<div class=\"control-container\"><input type=\"button\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
        "</div>\n");
  }

  @Test
  public void html() {
    ais.setCurrent(new DefaultActionInvocation(new Edit(), "/button", null, null));
    new ControlTester(button).
      attr("name", "html").
      attr("action", "/foo").
      attr("value", "test-value").
      go("<input type=\"hidden\" name=\"html@param\" value=\"param-value\"/>\n" +
        "<input type=\"hidden\" name=\"__a_html\" value=\"/foo\"/>\n" +
        "<div class=\"button-button button control\">\n" +
        "<div class=\"label-container\"> </div>\n" +
        "<div class=\"control-container\"><input type=\"button\" id=\"html\" name=\"html\" value=\"&lt;Button&gt;\"/></div>\n" +
        "</div>\n");
  }

  @Test
  public void actionContext() {
    request.setContextPath("/context");
    ais.setCurrent(new DefaultActionInvocation(new Edit(), "/button", null, null));
    new ControlTester(button).
      attr("name", "button").
      attr("action", "/foo").
      attr("value", "test-value").
      go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
        "<input type=\"hidden\" name=\"__a_button\" value=\"/context/foo\"/>\n" +
        "<div class=\"button-button button control\">\n" +
        "<div class=\"label-container\"> </div>\n" +
        "<div class=\"control-container\"><input type=\"button\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
        "</div>\n");
  }

  @Test
  public void actionContextRelative() {
    request.setContextPath("/context");
    ais.setCurrent(new DefaultActionInvocation(new Edit(), "/button", null, null));
    new ControlTester(button).
      attr("name", "button").
      attr("action", "foo").
      attr("value", "test-value").
      go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
        "<input type=\"hidden\" name=\"__a_button\" value=\"foo\"/>\n" +
        "<div class=\"button-button button control\">\n" +
        "<div class=\"label-container\"> </div>\n" +
        "<div class=\"control-container\"><input type=\"button\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
        "</div>\n");
  }
}