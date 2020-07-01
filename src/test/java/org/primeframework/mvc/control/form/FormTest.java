/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.example.action.user.EditAction;
import org.example.action.user.IndexAction;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.control.ControlBaseTest;
import org.primeframework.mvc.util.MapBuilder;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * This tests the form control.
 *
 * @author Brian Pontarelli
 */
public class FormTest extends ControlBaseTest {
  @Inject Form form;

  @Test
  public void noPrepare() {
    request.setUri("/user/");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null,
      new ActionConfiguration(IndexAction.class, null, null, new ArrayList<>(), null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, null, "/user/", null, null)));

    new ControlTester(form).
      attr("action", "/user/").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"/user/\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void jsessionid() {
    request.setUri("/user/;jsessionid=C35A2D9557C051F2854845305B1AB911");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null,
      new ActionConfiguration(IndexAction.class, null, null, new ArrayList<>(), null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, null, "/user/", null, null)));

    new ControlTester(form).
      attr("action", "/user/").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"/user/;jsessionid=C35A2D9557C051F2854845305B1AB911\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void noPrepareRelative() {
    request.setUri("/user/");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null, null));

    new ControlTester(form).
      attr("action", "edit").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"/user/edit\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void noPrepareFullyQualified() {
    request.setUri("/user/");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null,
      new ActionConfiguration(IndexAction.class, null, null, new ArrayList<>(), null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, null, "/user/", null, null)));

    new ControlTester(form).
      attr("action", "https://www.google.com").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"https://www.google.com\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void noPrepareContextPath() {
    request.setUri("/context/user/");
    request.setContextPath("/context");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null,
      new ActionConfiguration(IndexAction.class, null, null, new ArrayList<>(), null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, null, "/user/", null, null)));

    new ControlTester(form).
      attr("action", "/user/").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"/context/user/\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void relativeContextPath() {
    request.setUri("/context/user/");
    request.setContextPath("/context");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null, null));

    new ControlTester(form).
      attr("action", "edit").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"/context/user/edit\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void fullyQualifiedContextPath() {
    request.setUri("/context/user/");
    request.setContextPath("/context");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null,
      new ActionConfiguration(IndexAction.class, null, null, new ArrayList<>(), null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, null, "/user/", null, null)));

    new ControlTester(form).
      attr("action", "https://www.google.com").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"https://www.google.com\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void prepare() throws NoSuchMethodException {
    request.setUri("/user/edit");
    EditAction edit = new EditAction();
    ais.setCurrent(new ActionInvocation(edit, null, "/user/edit", null,
      new ActionConfiguration(IndexAction.class, null, null, asList(EditAction.class.getMethod("formPrepare")), null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, null, "/user/", null, null)));

    new ControlTester(form).
      attr("action", "/user/edit").
      attr("method", "POST").
      go("<div class=\"form\">\n" +
      "<form action=\"/user/edit\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
    assertTrue(edit.formPrepared);
  }

  @Test
  public void actionIsDifferentURI() {
    request.setUri("/user/");
    IndexAction index = new IndexAction();
    ais.setCurrent(new ActionInvocation(index, null, "/user/", null, null));

    StringWriter writer = new StringWriter();
    form.renderStart(writer, MapBuilder.map("action", (Object) "/user/edit").put("method", "POST").done(), MapBuilder.map("param", "param-value").done());

    EditAction edit = (EditAction) ais.getCurrent().action;
    assertTrue(edit.formPrepared);

    form.renderEnd(writer);
    assertSame(IndexAction.class, ais.getCurrent().action.getClass());
    assertEquals(
      "<div class=\"form\">\n" +
        "<form action=\"/user/edit\" method=\"POST\">\n" +
        "</form>\n" +
        "</div>\n", writer.toString());
  }
}