/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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

import java.util.Set;

import com.google.inject.Inject;
import org.apache.commons.lang3.ArrayUtils;
import org.example.action.user.EditAction;
import org.example.action.user.SomeEnum;
import org.example.domain.Address;
import org.example.domain.User;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.util.DefaultURIBuilder;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.primeframework.mvc.util.MapBuilder.lmap;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * This tests the select control.
 *
 * @author Brian Pontarelli
 */
public class SelectTest extends ControlBaseTest {
  private static final ActionConfiguration userEditActionConfiguration;

  @Inject public Select select;

  @Test
  public void action() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/select", null, userEditActionConfiguration));

    new ControlTester(select)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("US", "United States", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"select input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select id=\"user_addresses[&#39;work&#39;]_country\" name=\"user.addresses[&#39;work&#39;].country\">\n" +
            "<option value=\"US\" selected=\"selected\">United States</option>\n" +
            "<option value=\"DE\">Germany</option>\n" +
            "</select>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @Test
  public void actionLess() {
    ais.setCurrent(new ActionInvocation(null, null, "/select", null, userEditActionConfiguration));
    new ControlTester(select)
        .attr("name", "test")
        .attr("class", "css-class")
        .attr("items", asList("one", "two", "three"))
        .go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
            "<div class=\"css-class-select css-class-input css-class-control select input control\">\n" +
            "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select class=\"css-class\" id=\"test\" name=\"test\">\n" +
            "<option value=\"one\">one</option>\n" +
            "<option value=\"two\">two</option>\n" +
            "<option value=\"three\">three</option>\n" +
            "</select>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @Test
  public void enums() {
    EditAction action = new EditAction();

    ais.setCurrent(new ActionInvocation(action, null, "/select", null, userEditActionConfiguration));

    new ControlTester(select)
        .attr("name", "enumValue")
        .attr("items", SomeEnum.values())
        .go("<input type=\"hidden\" name=\"enumValue@param\" value=\"param-value\"/>\n" +
            "<div class=\"select input control\">\n" +
            "<div class=\"label-container\"><label for=\"enumValue\" class=\"label\">Enum</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select id=\"enumValue\" name=\"enumValue\">\n" +
            "<option value=\"VALUE1\" selected=\"selected\">Value 1</option>\n" +
            "<option value=\"VALUE2\">Value 2</option>\n" +
            "<option value=\"VALUE3\">VALUE3</option>\n" +
            "</select>\n" +
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

    ais.setCurrent(new ActionInvocation(action, null, "/select", null, userEditActionConfiguration));

    Pair us = new Pair("US", "United States");
    Pair de = new Pair("DE", "Germany");

    new ControlTester(select)
        .attr("name", "user.addresses['work'].country")
        .attr("valueExpr", "left")
        .attr("textExpr", "right")
        .attr("items", ArrayUtils.toArray(us, de))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"select input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select id=\"user_addresses[&#39;work&#39;]_country\" name=\"user.addresses[&#39;work&#39;].country\">\n" +
            "<option value=\"US\" selected=\"selected\">United States</option>\n" +
            "<option value=\"DE\">Germany</option>\n" +
            "</select>\n" +
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

    ais.setCurrent(new ActionInvocation(action, null, "/select", null, userEditActionConfiguration));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code1", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code2", "fieldError2"));

    new ControlTester(select)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("US", "United States", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"select input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\"><span class=\"error\">Country (fieldError1, fieldError2)</span></label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select id=\"user_addresses[&#39;work&#39;]_country\" name=\"user.addresses[&#39;work&#39;].country\">\n" +
            "<option value=\"US\" selected=\"selected\">United States</option>\n" +
            "<option value=\"DE\">Germany</option>\n" +
            "</select>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @Test
  public void headerOption() {
    ais.setCurrent(new ActionInvocation(null, null, "/select", null, userEditActionConfiguration));
    new ControlTester(select)
        .attr("name", "test")
        .attr("headerValue", "zero")
        .attr("items", asList("one", "two", "three"))
        .go("<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
            "<div class=\"select input control\">\n" +
            "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select id=\"test\" name=\"test\">\n" +
            "<option value=\"zero\"></option>\n" +
            "<option value=\"one\">one</option>\n" +
            "<option value=\"two\">two</option>\n" +
            "<option value=\"three\">three</option>\n" +
            "</select>\n" +
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

    ais.setCurrent(new ActionInvocation(action, null, "/select", null, userEditActionConfiguration));

    new ControlTester(select)
        .attr("name", "user.addresses['work'].country")
        .attr("items", lmap("<US>", "<United States>", "DE", "Germany"))
        .go("<input type=\"hidden\" name=\"user.addresses[&#39;work&#39;].country@param\" value=\"param-value\"/>\n" +
            "<div class=\"select input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_addresses[&#39;work&#39;]_country\" class=\"label\">Country</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select id=\"user_addresses[&#39;work&#39;]_country\" name=\"user.addresses[&#39;work&#39;].country\">\n" +
            "<option value=\"&lt;US&gt;\" selected=\"selected\">&lt;United States&gt;</option>\n" +
            "<option value=\"DE\">Germany</option>\n" +
            "</select>\n" +
            "</div>\n" +
            "</div>\n");
  }

  @Test
  public void optionalAttributeValidationFailure() {
    Address address = new Address();
    address.setCountry("US");
    EditAction action = new EditAction();
    action.user = new User();
    action.user.setAddress("work", address);

    ais.setCurrent(new ActionInvocation(action, null, "/select", null, userEditActionConfiguration));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code1", "fieldError1"));
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "user.addresses['work'].country", "code2", "fieldError2"));

    try {
      new ControlTester(select)
          .attr("name", "user.addresses['work'].country")
          .attr("items", lmap("US", "United States", "DE", "Germany"))
          .attr("multiple", "multiple")
          .go(null);

      fail("Should have failed because the multiple attribute should be a boolean");
    } catch (PrimeException e) {
      // Expected
      assertTrue(e.getMessage().contains("multiple"));
    }
  }

  static {
    userEditActionConfiguration = new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), Set.of())
        .build(EditAction.class);
  }
}
