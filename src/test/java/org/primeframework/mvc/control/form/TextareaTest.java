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
import org.example.domain.User;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.testng.annotations.Test;

/**
 * This tests the textarea control.
 *
 * @author Brian Pontarelli
 */
public class TextareaTest extends ControlBaseTest {
  @Inject public Textarea textarea;

  @Test
  public void action() {
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setName("Brian");

    ais.setCurrent(new ActionInvocation(action, null, "/textarea", null, null));
    new ControlTester(textarea).
        attr("name", "user.name").
        go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">Brian</textarea></div>\n" +
            "</div>\n");
  }

  @Test
  public void actionLess() {
    ais.setCurrent(new ActionInvocation(null, null, "/textarea", null, null));
    new ControlTester(textarea).
        attr("name", "test").
        attr("class", "css-class").
        go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
            "<div class=\"css-class-textarea css-class-input css-class-control textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
            "<div class=\"control-container\"><textarea class=\"css-class\" id=\"test\" name=\"test\"></textarea></div>\n" +
            "</div>\n");
  }

  @Test
  public void defaultValue() {
    EditAction action = new EditAction();

    ais.setCurrent(new ActionInvocation(action, null, "/textarea", null, null));

    new ControlTester(textarea).
        attr("name", "user.name").
        attr("defaultValue", "John").
        go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">John</textarea></div>\n" +
            "</div>\n");
  }

  @Test
  public void fieldErrors() {
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setName("Barry");

    ais.setCurrent(new ActionInvocation(action, null, "/textarea", null, null));

    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.name", "code1", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.name", "code2", "fieldError2"));

    new ControlTester(textarea).
        attr("name", "user.name").
        go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\"><span class=\"error\">Your name (fieldError1, fieldError2)</span></label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">Barry</textarea></div>\n" +
            "</div>\n");
  }

  @Test
  public void hardCodedValue() {
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setName("Brian");

    ais.setCurrent(new ActionInvocation(action, null, "/textarea", null, null));

    new ControlTester(textarea).
        attr("name", "user.name").
        attr("value", "Barry").
        go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">Barry</textarea></div>\n" +
            "</div>\n");
  }

  @Test
  public void html() {
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setName("<b>brian</b>");

    ais.setCurrent(new ActionInvocation(action, null, "/textarea", null, null));
    new ControlTester(textarea).
        attr("name", "user.name").
        go("<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">&lt;b&gt;brian&lt;/b&gt;</textarea></div>\n" +
            "</div>\n");
  }
}