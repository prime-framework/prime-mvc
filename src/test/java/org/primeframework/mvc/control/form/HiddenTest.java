/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
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
import org.testng.annotations.Test;

import com.google.inject.Inject;

/**
 * @author Daniel DeGroff
 */
public class HiddenTest extends ControlBaseTest {

  @Inject public Hidden hidden;

  @Test
  public void action_boolean_true() {
    Edit action = new Edit();
    ais.setCurrent(new ActionInvocation(action, null, "/hidden", null, null));

    action.user = new User();
    action.user.setMale(true);
    testAction("user.male", true);
  }

  @Test
  public void action_boolean_false() {
    Edit action = new Edit();
    ais.setCurrent(new ActionInvocation(action, null, "/hidden", null, null));

    action.user = new User();
    action.user.setMale(false);
    testAction("user.male", false);
  }

  @Test
  public void action_boolean_default() {
    Edit action = new Edit();
    ais.setCurrent(new ActionInvocation(action, null, "/hidden", null, null));

    action.user = new User();
    testAction("user.male", false);
  }

  protected void testAction(String property, boolean value) {
    new ControlTester(hidden)
        .attr("name", property)
        .attr("value", value)
        .go("<input type=\"hidden\" name=\"" + property + "@param\" value=\"param-value\"/>\n"
            + "<input type=\"hidden\" id=\"" + property.replace(".", "_") + "\" name=\"" + property + "\" value=\"" + value + "\"/>");
  }
}
