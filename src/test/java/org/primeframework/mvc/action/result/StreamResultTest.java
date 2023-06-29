/*
 * Copyright (c) 2001-2023, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.easymock.EasyMock;
import org.primeframework.mock.servlet.MockServletOutputStream;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.result.annotation.Stream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the stream result.
 *
 * @author Brian Pontarelli
 */
public class StreamResultTest {
  @Test(dataProvider = "httpMethod")
  public void explicit(HTTPMethod httpMethod, String fileName, String basicEncoded, String utf8Encoded)
      throws IOException {
    Object action = new Object();
    ZonedDateTime lastModified = ZonedDateTime.of(2021, 7, 4, 12, 42, 42, 0, ZoneOffset.UTC);
    ExpressionEvaluator ee = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(ee.expand("10", action, false)).andReturn("10");
    EasyMock.expect(ee.expand(fileName, action, false)).andReturn(fileName);
    EasyMock.expect(ee.expand("application/octet-stream", action, false)).andReturn("application/octet-stream");
    EasyMock.expect(ee.getValue("lastModified", action)).andReturn(lastModified);
    EasyMock.expect(ee.getValue("stream", action)).andReturn(new ByteArrayInputStream("test".getBytes()));
    EasyMock.replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
    response.setStatus(200);
    response.setContentType("application/octet-stream");
    response.setContentLength(10);
    response.setHeader("Content-Disposition", "attachment; filename=\"" + basicEncoded + "\"; filename*=UTF-8''" + utf8Encoded);
    response.setHeader("Last-Modified", "Sun, 04 Jul 2021 12:42:42 GMT");
    response.setHeader("Cache-Control", "no-cache");
    EasyMock.expect(response.getOutputStream()).andReturn(sos);
    EasyMock.replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(httpMethod, null, null), "/foo", "", null));
    replay(store);

    Stream stream = new StreamImpl("success", fileName, "10", "application/octet-stream", "stream", "lastModified");
    StreamResult streamResult = new StreamResult(ee, response, store);
    streamResult.execute(stream);

    if (httpMethod == HTTPMethod.GET) {
      assertEquals(sos.toString(), "test");
      EasyMock.verify(ee, response);
    } else {
      assertEquals(sos.toString(), "");
    }
  }

  @DataProvider(name = "httpMethod")
  public Object[][] httpMethod() {
    return new Object[][]{
        // METHOD, File, Simple escaped, UTF8 Escaped
        {HTTPMethod.GET, "foo.zip", "foo.zip", "foo.zip"}, // normal
        {HTTPMethod.GET, "foo \\bar.zip", "foo \\\\bar.zip", "foo%20%5Cbar.zip"}, // contains back slash
        {HTTPMethod.GET, "foo bar.zip", "foo bar.zip", "foo%20bar.zip"}, // contains space
        {HTTPMethod.GET, "foo \"bar\" baz.zip", "foo \\\"bar\\\" baz.zip", "foo%20%22bar%22%20baz.zip"}, // contains double quote
        {HTTPMethod.GET, "foo \uD83D\uDE00 baz.zip", "foo \uD83D\uDE00 baz.zip", "foo%20%F0%9F%98%80%20baz.zip"}, // emoji

        {HTTPMethod.HEAD, "foo.zip", "foo.zip", "foo.zip"}, // normal
        {HTTPMethod.HEAD, "foo \\bar.zip", "foo \\\\bar.zip", "foo%20%5Cbar.zip"}, // contains back slash
        {HTTPMethod.HEAD, "foo bar.zip", "foo bar.zip", "foo%20bar.zip"}, // contains space
        {HTTPMethod.HEAD, "foo \"bar\" baz.zip", "foo \\\"bar\\\" baz.zip", "foo%20%22bar%22%20baz.zip"}, // contains double quote
        {HTTPMethod.HEAD, "foo \uD83D\uDE00 baz.zip", "foo \uD83D\uDE00 baz.zip", "foo%20%F0%9F%98%80%20baz.zip"} // emoji
    };
  }

  public class StreamImpl implements Stream {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final String lastModifiedProperty;

    private final String length;

    private final String name;

    private final String property;

    private final int status = 200;

    private final String type;

    public StreamImpl(String code, String name, String length, String type, String property,
                      String lastModifiedProperty) {
      this.cacheControl = "no-cache";
      this.code = code;
      this.disableCacheControl = false;
      this.lastModifiedProperty = lastModifiedProperty;
      this.length = length;
      this.name = name;
      this.property = property;
      this.type = type;
    }

    public Class<? extends Annotation> annotationType() {
      return Stream.class;
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

    @Override
    public String lastModifiedProperty() {
      return lastModifiedProperty;
    }

    public String length() {
      return length;
    }

    public String name() {
      return name;
    }

    public String property() {
      return property;
    }

    @Override
    public int status() {
      return status;
    }

    public String type() {
      return type;
    }
  }
}
