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
package org.primeframework.mvc.control.form.control;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;

import org.example.action.user.Edit;
import org.example.action.user.Index;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.control.control.ControlBaseTest;
import org.primeframework.mvc.control.form.Form;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;
import static org.testng.Assert.*;

/**
 * <p> This tests the form control. </p>
 *
 * @author Brian Pontarelli
 */
public class FormTest extends ControlBaseTest {
  @Inject Form form;

  @Test
  public void testNoPrepare() {
    request.setUri("/user/");
    Index index = new Index();
    ais.setCurrent(new DefaultActionInvocation(index, "/user/", null, null));

    run(form,
      mapNV("action", "/user/", "method", "POST"),
      null, "<div class=\"form\">\n" +
      "<form action=\"/user/\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void testNoPrepareRelative() {
    request.setUri("/user/");
    Index index = new Index();
    ais.setCurrent(new DefaultActionInvocation(index, "/user/", null, null));

    run(form,
      mapNV("action", "edit", "method", "POST"),
      null, "<div class=\"form\">\n" +
      "<form action=\"edit\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void testNoPrepareFullyQualified() {
    request.setUri("/user/");
    Index index = new Index();
    ais.setCurrent(new DefaultActionInvocation(index, "/user/", null, null));

    run(form,
      mapNV("action", "https://www.google.com", "method", "POST"),
      null, "<div class=\"form\">\n" +
      "<form action=\"https://www.google.com\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void testNoPrepareContextPath() {
    request.setUri("/context/user/");
    request.setContextPath("/context");
    Index index = new Index();
    ais.setCurrent(new DefaultActionInvocation(index, "/user/", null, null));

    run(form,
      mapNV("action", "/user/", "method", "POST"),
      null, "<div class=\"form\">\n" +
      "<form action=\"/context/user/\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void testRelativeContextPath() {
    request.setUri("/context/user/");
    request.setContextPath("/context");
    Index index = new Index();
    ais.setCurrent(new DefaultActionInvocation(index, "/user/", null, null));

    run(form,
      mapNV("action", "edit", "method", "POST"),
      null, "<div class=\"form\">\n" +
      "<form action=\"/context/user/edit\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void testFullyQualifiedContextPath() {
    request.setUri("/context/user/");
    request.setContextPath("/context");
    Index index = new Index();
    ais.setCurrent(new DefaultActionInvocation(index, "/user/", null, null));

    run(form,
      mapNV("action", "https://www.google.com", "method", "POST"),
      null, "<div class=\"form\">\n" +
      "<form action=\"https://www.google.com\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
  }

  @Test
  public void testPrepare() throws IOException, ServletException {
    request.setUri("/user/edit");
    Edit edit = new Edit();
    ais.setCurrent(new DefaultActionInvocation(edit, "/user/edit", null, null));

    run(form,
      mapNV("action", "/user/edit", "method", "POST"),
      null, "<div class=\"form\">\n" +
      "<form action=\"/user/edit\" method=\"POST\">\n" +
      "</form>\n" +
      "</div>\n");
    assertTrue(edit.formPrepared);
  }

  @Test
  public void testActionIsDifferentURI() throws IOException, ServletException {
    request.setUri("/user/");
    Index index = new Index();
    ais.setCurrent(new DefaultActionInvocation(index, "/user/", null, null));

    StringWriter writer = new StringWriter();
    form.renderStart(writer, mapNV("action", "/user/edit", "method", "POST"), map("param", "param-value"));

    Edit edit = (Edit) ais.getCurrent().action();
    assertTrue(edit.formPrepared);

    form.renderEnd(writer);
    assertSame(Index.class, ais.getCurrent().action().getClass());
    assertEquals(
      "<div class=\"form\">\n" +
        "<form action=\"/user/edit\" method=\"POST\">\n" +
        "</form>\n" +
        "</div>\n", writer.toString());
  }
}