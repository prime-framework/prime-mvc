/*
 * Copyright (c) 2012-2019, Inversoft Inc., All Rights Reserved
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
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.primeframework.mock.servlet.MockServletOutputStream;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.result.annotation.XMLStream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the XML Stream result.
 *
 * @author jhumphrey
 */
public class XMLStreamResultTest {
  @Test(dataProvider = "httpMethod")
  public void explicit(HTTPMethod httpMethod) throws IOException, ServletException {
    String property = "xml";
    String propertyValue = "<xml/>";
    byte[] propertyBytes = propertyValue.getBytes();
    int propertyBytesLen = propertyBytes.length;
    String contentType = "application/xhtml+xml";

    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.getValue(property, action)).andReturn(propertyValue);
    replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(200);
    response.setCharacterEncoding("UTF-8");
    response.setContentType(contentType);
    response.setContentLength(propertyBytesLen);
    response.setHeader("Cache-Control", "no-store");
    if (httpMethod == HTTPMethod.GET) {
      expect(response.getOutputStream()).andReturn(sos);
    }
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(httpMethod, null, null), "/foo", "", null));
    replay(store);

    XMLStream xmlStream = new XMLStreamResultTest.XMLStreamImpl("success", "xml", 200);
    XMLStreamResult streamResult = new XMLStreamResult(ee, response, store);
    streamResult.execute(xmlStream);

    assertEquals(sos.toString(), httpMethod == HTTPMethod.GET ? "<xml/>" : "");

    verify(ee, response);
  }

  @DataProvider(name = "httpMethod")
  public Object[][] httpMethod() {
    return new Object[][]{{HTTPMethod.GET}, {HTTPMethod.HEAD}};
  }

  public class XMLStreamImpl implements XMLStream {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final String property;

    private final int status;

    public XMLStreamImpl(String code, String property, int status) {
      this.cacheControl = "no-store";
      this.code = code;
      this.disableCacheControl = false;
      this.property = property;
      this.status = status;
    }

    public Class<? extends Annotation> annotationType() {
      return XMLStream.class;
    }

    @Override
    public String cacheControl() {
      return cacheControl;
    }

    public String code() {
      return code;
    }

    @Override
    public boolean disableCacheControl() {
      return disableCacheControl;
    }

    public String property() {
      return property;
    }

    public int status() {
      return status;
    }
  }
}
