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

import java.util.LinkedHashMap;
import java.util.Map;

import org.example.action.user.Edit;
import org.example.domain.Address;
import org.example.domain.User;
import org.jcatapult.mvc.action.DefaultActionInvocation;
import org.jcatapult.mvc.message.scope.MessageScope;
import org.jcatapult.mvc.result.control.ControlBaseTest;
import org.junit.Test;

import net.java.util.Pair;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the radio control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class RadioListTest extends ControlBaseTest {
    @Inject public RadioList radioList;

    @Test
    public void testActionLess() {
        ais.setCurrent(new DefaultActionInvocation(null, "/radio-list", null, null));
        run(radioList,
            mapNV("name", "test", "class", "css-class", "bundle", "/radio-list-bundle", "items", asList("one", "two", "three")),
            null, "<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
            "<div class=\"css-class-radio-list css-class-input css-class-control radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"one\" class=\"css-class\" name=\"test\"/><span class=\"radio-text\">one</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"two\" class=\"css-class\" name=\"test\"/><span class=\"radio-text\">two</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"three\" class=\"css-class\" name=\"test\"/><span class=\"radio-text\">three</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__jc_rb_test\" value=\"\"/>\n");
    }

    @Test
    public void testAction() {
        Address address = new Address();
        address.setCountry("US");
        Edit action = new Edit();
        action.user = new User();
        action.user.setAddress("work", address);

        ais.setCurrent(new DefaultActionInvocation(action, "/radio-list", null, null));
        run(radioList,
            mapNV("name", "user.addresses['work'].country", "items", lmap("US", "United States", "DE", "Germany")),
            null, "<input type=\"hidden\" name=\"user.addresses['work'].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses['work']_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__jc_rb_user.addresses['work'].country\" value=\"\"/>\n");
    }

    @Test
    public void html() {
        Address address = new Address();
        address.setCountry("<US>");
        Edit action = new Edit();
        action.user = new User();
        action.user.setAddress("work", address);

        ais.setCurrent(new DefaultActionInvocation(action, "/radio-list", null, null));
        run(radioList,
            mapNV("name", "user.addresses['work'].country", "items", lmap("<US>", "<United States>", "DE", "Germany")),
            null, "<input type=\"hidden\" name=\"user.addresses['work'].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses['work']_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"&lt;US&gt;\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\"><United States></span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__jc_rb_user.addresses['work'].country\" value=\"\"/>\n");
    }

    @Test
    public void testExpressions() {
        Address address = new Address();
        address.setCountry("US");
        Edit action = new Edit();
        action.user = new User();
        action.user.setAddress("work", address);

        ais.setCurrent(new DefaultActionInvocation(action, "/radio-list", null, null));

        Pair<String, String> us = new Pair<String, String>("US", "United States");
        Pair<String, String> de = new Pair<String, String>("DE", "Germany");

        run(radioList,
            mapNV("name", "user.addresses['work'].country", "valueExpr", "first", "textExpr", "second", "items", array(us, de)),
            null, "<input type=\"hidden\" name=\"user.addresses['work'].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses['work']_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__jc_rb_user.addresses['work'].country\" value=\"\"/>\n");
    }

    @Test
    public void testFieldErrors() {
        Address address = new Address();
        address.setCountry("US");
        Edit action = new Edit();
        action.user = new User();
        action.user.setAddress("work", address);

        ais.setCurrent(new DefaultActionInvocation(action, "/radio-list", null, null));
        messageStore.addFieldError(MessageScope.REQUEST, "user.addresses['work'].country", "fieldError1");
        messageStore.addFieldError(MessageScope.REQUEST, "user.addresses['work'].country", "fieldError2");

        run(radioList,
            mapNV("name", "user.addresses['work'].country", "items", lmap("US", "United States", "DE", "Germany")),
            null, "<input type=\"hidden\" name=\"user.addresses['work'].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses['work']_country\" class=\"label\"><span class=\"error\">Country (Country is required, Country must be cool)</span></label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__jc_rb_user.addresses['work'].country\" value=\"\"/>\n");
    }

    @Test
    public void testUncheckedValue() {
        Address address = new Address();
        address.setCountry("US");
        Edit action = new Edit();
        action.user = new User();
        action.user.setAddress("work", address);

        ais.setCurrent(new DefaultActionInvocation(action, "/radio-list", null, null));
        messageStore.addFieldError(MessageScope.REQUEST, "user.addresses['work'].country", "fieldError1");
        messageStore.addFieldError(MessageScope.REQUEST, "user.addresses['work'].country", "fieldError2");

        run(radioList,
            mapNV("name", "user.addresses['work'].country", "items", lmap("US", "United States", "DE", "Germany"), "uncheckedValue", "US"),
            null, "<input type=\"hidden\" name=\"user.addresses['work'].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses['work']_country\" class=\"label\"><span class=\"error\">Country (Country is required, Country must be cool)</span></label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses['work'].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__jc_rb_user.addresses['work'].country\" value=\"US\"/>\n");
    }

    public static <T> Map<T, T> lmap(T... values) {
        LinkedHashMap<T, T> map = new LinkedHashMap<T,T>();
        for (int i = 0; i < values.length; i = i + 2) {
            T key = values[i];
            T value = values[i + 1];
            map.put(key, value);
        }

        return map;
    }
}