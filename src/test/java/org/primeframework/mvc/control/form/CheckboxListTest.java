/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import static java.util.Arrays.asList;
import static org.primeframework.mvc.util.MapBuilder.lmap;

/**
 * This tests the checkboxlist control.
 *
 * @author Brian Pontarelli
 */
public class CheckboxListTest extends ControlBaseTest {
  @Inject public CheckboxList checkboxList;

  @Test
  public void action() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/checkbox-list", null, null));
    new ControlTester(checkboxList)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("US", "United States", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"checkbox-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" checked=\"checked\" value=\"US\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @Test
  public void actionLess() {
    ais.setCurrent(new ActionInvocation(null, null, "/checkbox-list", null, null));
    new ControlTester(checkboxList)
        .attr("name", "test")
        .attr("class", "css-class")
        .attr("items", asList("one", "two", "three"))
        .go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
            "<div class=\"css-class-checkbox-list css-class-input css-class-control checkbox-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" value=\"one\" class=\"css-class\" name=\"test\"/><span class=\"checkbox-text\">one</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" value=\"two\" class=\"css-class\" name=\"test\"/><span class=\"checkbox-text\">two</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" value=\"three\" class=\"css-class\" name=\"test\"/><span class=\"checkbox-text\">three</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void expressions() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/checkbox-list", null, null));

    Pair<String, String> us = Pair.of("US", "United States");
    Pair<String, String> de = Pair.of("DE", "Germany");

    new ControlTester(checkboxList)
        .attr("name", "user.addresses['work'].country")
        .attr("valueExpr", "left")
        .attr("textExpr", "right")
        .attr("items", ArrayUtils.toArray(us, de))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"checkbox-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" checked=\"checked\" value=\"US\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @Test
  public void fieldErrors() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/checkbox-list", null, null));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code1", "Country is required"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code2", "Country must be cool"));

    new ControlTester(checkboxList)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("US", "United States", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"checkbox-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\"><span class=\"error\">Country (Country is required, Country must be cool)</span></label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" checked=\"checked\" value=\"US\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\">United States</span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @Test
  public void html() {
    Address address = new Address();
    address.setCountry("<US>");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/checkbox-list", null, null));
    new ControlTester(checkboxList)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("<US>", "<United States>", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"checkbox-list input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" checked=\"checked\" value=\"&lt;US&gt;\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\"><United States></span>\n" +
            "</div>\n" +
            "<div class=\"control-item-container\">\n" +
            "<input type=\"checkbox\" value=\"DE\" name=\"user.addresses[&#39;work&#39;].country\"/><span class=\"checkbox-text\">Germany</span>\n" +
            "</div>\n" +
            "</div>\n" +
            "</div>\n");
  }
}