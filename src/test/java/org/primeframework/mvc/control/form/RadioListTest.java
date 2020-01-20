/*
 * Copyright (c) 2001-2020, Inversoft Inc., All Rights Reserved
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.example.action.user.EditAction;
import org.example.domain.Address;
import org.example.domain.User;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.primeframework.mvc.util.MapBuilder.lmap;

/**
 * This tests the radio control.
 *
 * @author Brian Pontarelli
 */
public class RadioListTest extends ControlBaseTest {
  @Inject public RadioList radioList;

  @Test
  public void action() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/radio-list", null, null));
    new ControlTester(radioList)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("US", "United States", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__rb_user.addresses[&#39;work&#39;].country\" value=\"\"/>\n");
  }

  @Test
  public void actionLess() {
    ais.setCurrent(new ActionInvocation(null, null, "/radio-list", null, null));
    new ControlTester(radioList)
        .attr("name", "test")
        .attr("class", "css-class")
        .attr("items", asList("one", "two", "three"))
        .go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
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
            "<input type=\"hidden\" name=\"__rb_test\" value=\"\"/>\n");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void expressions() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/radio-list", null, null));

    Pair<String, String> us = Pair.of("US", "United States");
    Pair<String, String> de = Pair.of("DE", "Germany");

    new ControlTester(radioList)
        .attr("name", "user.addresses['work'].country")
        .attr("valueExpr", "left")
        .attr("textExpr", "right")
        .attr("items", ArrayUtils.toArray(us, de))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__rb_user.addresses[&#39;work&#39;].country\" value=\"\"/>\n");
  }

  @Test
  public void fieldErrors() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/radio-list", null, null));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code1", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code2", "fieldError2"));

    new ControlTester(radioList)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("US", "United States", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\"><span class=\"error\">Country (fieldError1, fieldError2)</span></label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__rb_user.addresses[&#39;work&#39;].country\" value=\"\"/>\n");
  }

  @Test
  public void html() {
    Address address = new Address();
    address.setCountry("<US>");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/radio-list", null, null));
    new ControlTester(radioList)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("<US>", "<United States>", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"radio-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" checked=\"checked\" value=\"&lt;US&gt;\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">&lt;United States&gt;</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"radio\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<input type=\"hidden\" name=\"__rb_user.addresses[&#39;work&#39;].country\" value=\"\"/>\n");
  }

  @Test
  public void uncheckedValue() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/radio-list", null, null));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code1", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code2", "fieldError2"));

    new ControlTester(radioList)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("US", "United States", "DE", "Germany"))
        .attr("uncheckedValue", "US")
        .
            go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
                "<div class=\"radio-list input control\">\n" +
                "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\"><span class=\"error\">Country (fieldError1, fieldError2)</span></label></div>\n" +
                "<div class=\"control-container\">\n" +
                "<div class=\"control-item-container\">\n" +
                "<input type=\"radio\" checked=\"checked\" value=\"US\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">United States</span>\n" +
                "</div>\n" +
                "<div class=\"control-item-container\">\n" +
                "<input type=\"radio\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"radio-text\">Germany</span>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "<input type=\"hidden\" name=\"__rb_user.addresses[&#39;work&#39;].country\" value=\"US\"/>\n");
  }
}