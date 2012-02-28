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
package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.easymock.EasyMock;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.result.annotation.Stream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.test.servlet.MockServletOutputStream;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p> This class tests the stream result. </p>
 *
 * @author Brian Pontarelli
 */
public class StreamResultTest {
  @Test
  public void testExplicit() throws IOException, ServletException {
    Object action = new Object();
    ExpressionEvaluator ee = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(ee.expand("10", action)).andReturn("10");
    EasyMock.expect(ee.expand("foo.zip", action)).andReturn("foo.zip");
    EasyMock.expect(ee.expand("application/octet-stream", action)).andReturn("application/octet-stream");
    EasyMock.expect(ee.getValue("stream", action)).andReturn(new ByteArrayInputStream("test".getBytes()));
    EasyMock.replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
    response.setContentType("application/octet-stream");
    response.setContentLength(10);
    response.setHeader("Content-Disposition", "attachment; filename=\"foo.zip\"");
    EasyMock.expect(response.getOutputStream()).andReturn(sos);
    EasyMock.replay(response);

    Stream stream = new StreamImpl("success", "foo.zip", "10", "application/octet-stream", "stream");
    StreamResult streamResult = new StreamResult(ee, response);
    streamResult.execute(stream, new DefaultActionInvocation(action, "/foo", "", null));

    assertEquals("test", sos.toString());

    EasyMock.verify(ee, response);
  }

  public class StreamImpl implements Stream {
    private final String code;
    private final String name;
    private final String length;
    private final String type;
    private final String property;

    public StreamImpl(String code, String name, String length, String type, String property) {
      this.code = code;
      this.name = name;
      this.length = length;
      this.type = type;
      this.property = property;
    }

    public String code() {
      return code;
    }

    public String property() {
      return property;
    }

    public String type() {
      return type;
    }

    public String length() {
      return length;
    }

    public String name() {
      return name;
    }

    public Class<? extends Annotation> annotationType() {
      return Stream.class;
    }
  }
}