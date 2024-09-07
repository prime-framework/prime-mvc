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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;

import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.result.annotation.XMLStream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
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
public class XMLStreamResultTest extends PrimeBaseTest {
  @Test(dataProvider = "httpMethod")
  public void explicit(HTTPMethod httpMethod) throws IOException {
    String property = "xml";
    String propertyValue = "<xml/>";
    byte[] propertyBytes = propertyValue.getBytes();
    long propertyBytesLen = propertyBytes.length;
    String contentType = "application/xhtml+xml; charset=UTF-8";

    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.getValue(property, action)).andReturn(propertyValue);
    replay(ee);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    HTTPResponse response = new HTTPResponse() {
      @Override
      public OutputStream getOutputStream() {
        return baos;
      }
    };
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(httpMethod, null, null), "/foo", "", null));
    replay(store);

    XMLStream xmlStream = new XMLStreamImpl("success", "xml", 200);
    XMLStreamResult streamResult = new XMLStreamResult(ee, response, store);
    streamResult.execute(xmlStream);

    assertEquals(baos.toString(), httpMethod == HTTPMethod.GET ? "<xml/>" : "");

    verify(ee);

    assertEquals(response.getStatus(), 200);
    assertEquals(response.getContentType(), contentType);
    assertEquals(response.getContentLength().longValue(), propertyBytesLen);
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @DataProvider(name = "httpMethod")
  public Object[][] httpMethod() {
    return new Object[][]{{HTTPMethod.GET}, {HTTPMethod.HEAD}};
  }

  @SuppressWarnings("ClassExplicitlyAnnotation")
  public static class XMLStreamImpl implements XMLStream {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final String property;

    private final int status;

    public XMLStreamImpl(String code, String property, int status) {
      this.cacheControl = "no-cache";
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
