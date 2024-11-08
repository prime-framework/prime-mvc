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
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.testng.annotations.Test;

/**
 * This tests the submit control.
 *
 * @author Brian Pontarelli
 */
public class SubmitTest extends ControlBaseTest {
  @Inject public Submit submit;

  @Test
  public void action() {
    ais.setCurrent(new ActionInvocation(new EditAction(), null, "/button", null, null));
    new ControlTester(submit).
        attr("name", "button").
        attr("value", "test-value").
        go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
           "<div class=\"submit-button button control\">\n" +
           "<div class=\"label-container\"> </div>\n" +
           "<div class=\"control-container\"><input type=\"submit\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
           "</div>\n");
  }

  @Test
  public void actionAttribute() {
    ais.setCurrent(new ActionInvocation(new EditAction(), null, "/button", null, null));
    new ControlTester(submit).
        attr("name", "button").
        attr("action", "/foo").
        attr("value", "test-value").
        go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
           "<div class=\"submit-button button control\">\n" +
           "<div class=\"label-container\"> </div>\n" +
           "<div class=\"control-container\"><input type=\"submit\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
           "</div>\n");
  }

  @Test
  public void actionAttributeContext() {
    request.setContextPath("/context");
    ais.setCurrent(new ActionInvocation(new EditAction(), null, "/button", null, null));
    new ControlTester(submit).
        attr("name", "button").
        attr("action", "/foo").
        attr("value", "test-value").
        go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
           "<div class=\"submit-button button control\">\n" +
           "<div class=\"label-container\"> </div>\n" +
           "<div class=\"control-container\"><input type=\"submit\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
           "</div>\n");
  }

  @Test
  public void actionAttributeContextRelative() {
    request.setContextPath("/context");
    ais.setCurrent(new ActionInvocation(new EditAction(), null, "/button", null, null));
    new ControlTester(submit).
        attr("name", "button").
        attr("action", "foo").
        attr("value", "test-value").
        go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
           "<div class=\"submit-button button control\">\n" +
           "<div class=\"label-container\"> </div>\n" +
           "<div class=\"control-container\"><input type=\"submit\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
           "</div>\n");
  }

  @Test
  public void actionLess() {
    ais.setCurrent(new ActionInvocation(null, null, "/button", null, null));
    new ControlTester(submit).
        attr("name", "button").
        attr("value", "test-value").
        attr("class", "css-class").
        go("<input type=\"hidden\" name=\"button@param\" value=\"param-value\"/>\n" +
           "<div class=\"css-class-submit-button css-class-button css-class-control submit-button button control\">\n" +
           "<div class=\"label-container\"> </div>\n" +
           "<div class=\"control-container\"><input type=\"submit\" class=\"css-class\" id=\"button\" name=\"button\" value=\"Button\"/></div>\n" +
           "</div>\n");
  }

  @Test
  public void html() {
    ais.setCurrent(new ActionInvocation(new EditAction(), null, "/button", null, null));
    new ControlTester(submit).
        attr("name", "html").
        attr("value", "test-value").
        go("<input type=\"hidden\" name=\"html@param\" value=\"param-value\"/>\n" +
           "<div class=\"submit-button button control\">\n" +
           "<div class=\"label-container\"> </div>\n" +
           "<div class=\"control-container\"><input type=\"submit\" id=\"html\" name=\"html\" value=\"&lt;Button&gt;\"/></div>\n" +
           "</div>\n");
  }
}
