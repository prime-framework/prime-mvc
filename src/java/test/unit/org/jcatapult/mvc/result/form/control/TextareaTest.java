/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.result.form.control;

import org.example.action.user.Edit;
import org.example.domain.User;
import org.jcatapult.mvc.action.DefaultActionInvocation;
import org.jcatapult.mvc.message.scope.MessageScope;
import org.jcatapult.mvc.result.control.ControlBaseTest;
import org.junit.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the textarea control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class TextareaTest extends ControlBaseTest {
    @Inject public Textarea textarea;

    @Test
    public void testActionLess() {
        ais.setCurrent(new DefaultActionInvocation(null, "/textarea", null, null));
        run(textarea,
            mapNV("name", "test", "class", "css-class", "bundle", "/textarea-bundle"),
            null, "<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
            "<div class=\"css-class-textarea css-class-input css-class-control textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
            "<div class=\"control-container\"><textarea class=\"css-class\" id=\"test\" name=\"test\"></textarea></div>\n" +
            "</div>\n");
    }

    @Test
    public void testAction() {
        Edit action = new Edit();
        action.user = new User();
        action.user.setName("Brian");

        ais.setCurrent(new DefaultActionInvocation(action, "/textarea", null, null));
        run(textarea,
            mapNV("name", "user.name"),
            null, "<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">Brian</textarea></div>\n" +
            "</div>\n");
    }

    @Test
    public void html() {
        Edit action = new Edit();
        action.user = new User();
        action.user.setName("<b>brian</b>");

        ais.setCurrent(new DefaultActionInvocation(action, "/textarea", null, null));
        run(textarea,
            mapNV("name", "user.name"),
            null, "<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">&lt;b&gt;brian&lt;/b&gt;</textarea></div>\n" +
            "</div>\n");
    }

    @Test
    public void testFieldErrors() {
        Edit action = new Edit();
        action.user = new User();
        action.user.setName("Barry");

        ais.setCurrent(new DefaultActionInvocation(action, "/textarea", null, null));

        messageStore.addFieldError(MessageScope.REQUEST, "user.name", "fieldError1");
        messageStore.addFieldError(MessageScope.REQUEST, "user.name", "fieldError2");

        run(textarea,
            mapNV("name", "user.name"),
            null, "<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\"><span class=\"error\">Your name (Name is required, Name must be cool)</span></label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">Barry</textarea></div>\n" +
            "</div>\n");
    }

    @Test
    public void testDefaultValue() {
        Edit action = new Edit();

        ais.setCurrent(new DefaultActionInvocation(action, "/textarea", null, null));

        run(textarea,
            mapNV("name", "user.name", "defaultValue", "John"),
            null, "<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">John</textarea></div>\n" +
            "</div>\n");
    }

    @Test
    public void testHardCodedValue() {
        Edit action = new Edit();
        action.user = new User();
        action.user.setName("Brian");

        ais.setCurrent(new DefaultActionInvocation(action, "/textarea", null, null));

        run(textarea,
            mapNV("name", "user.name", "value", "Barry"),
            null, "<input type=\"hidden\" name=\"user.name@param\" value=\"param-value\"/>\n" +
            "<div class=\"textarea input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_name\" class=\"label\">Your name</label></div>\n" +
            "<div class=\"control-container\"><textarea id=\"user_name\" name=\"user.name\">Barry</textarea></div>\n" +
            "</div>\n");
    }
}