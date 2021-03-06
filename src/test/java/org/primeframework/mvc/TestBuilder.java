/*
 * Copyright (c) 2016-2018, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.primeframework.mvc.test.RequestResult;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.ThrowingCallable;
import org.primeframework.mvc.util.ThrowingRunnable;
import org.testng.Assert;
import static org.primeframework.mvc.scope.ActionSessionScope.ACTION_SESSION_KEY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Daniel DeGroff
 */
public class TestBuilder {
  public Function<ObjectMapper, ObjectMapper> objectMapperFunction;

  public RequestResult requestResult;

  public RequestSimulator simulator;

  public Path tempFile;

  @SuppressWarnings("unchecked")
  public TestBuilder assertActionSessionAttributeIsNull(String actionName, String attributeName) {
    Object object = simulator.container.getSession().getAttribute(ACTION_SESSION_KEY);
    assertNotNull(object);
    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) object;
    Object actionAttributes = map.get(actionName);
    assertNotNull(actionAttributes);
    Map<String, Object> actionAttributesMap = (Map<String, Object>) actionAttributes;
    assertNull(actionAttributesMap.get(attributeName));
    return this;
  }

  @SuppressWarnings("unchecked")
  public TestBuilder assertActionSessionAttributeNotNull(String actionName, String attributeName) {
    Object object = simulator.container.getSession().getAttribute(ACTION_SESSION_KEY);
    assertNotNull(object);
    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) object;
    Object actionAttributes = map.get(actionName);
    assertNotNull(actionAttributes);
    Map<String, Object> actionAttributesMap = (Map<String, Object>) actionAttributes;
    assertNotNull(actionAttributesMap.get(attributeName));
    return this;
  }

  public TestBuilder assertContextAttributeNotNull(String attributeName) {
    assertNotNull(simulator.container.getContext().getAttribute(attributeName));
    return this;
  }

  public TestBuilder assertCookie(String name, String value) {
    Cookie actual = requestResult.container.getUserAgent().getCookies(requestResult.container.getRequest()).stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    assertNotNull(actual);
    assertEquals(actual.getValue(), value);
    return this;
  }

  public TestBuilder assertRequestAttributeIsNull(String attributeName) {
    assertNull(requestResult.request.getAttribute(attributeName));
    return this;
  }

  public TestBuilder assertRequestAttributeNotNull(String attributeName) {
    assertNotNull(requestResult.request.getAttribute(attributeName));
    return this;
  }

  public TestBuilder assertSessionAttributeNotNull(String attributeName) {
    assertNotNull(simulator.container.getSession().getAttribute(attributeName));
    return this;
  }

  public TestBuilder configureObjectMapper(Function<ObjectMapper, ObjectMapper> function) {
    this.objectMapperFunction = function;
    return this;
  }

  public TestBuilder createFile() throws IOException {
    return createFile("Test File");
  }

  public TestBuilder createFile(String contents) throws IOException {
    String tmpdir = System.getProperty("java.io.tmpdir");
    String unique = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()), "UTF-8").substring(0, 5);
    tempFile = Paths.get(tmpdir + "/" + "_prime_testFile_" + unique);
    tempFile.toFile().deleteOnExit();

    Files.write(tempFile, contents.getBytes());
    return this;
  }

  @SuppressWarnings("Duplicates")
  public void expectException(Class<? extends Throwable> throwable, ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      int count = 0;
      Throwable t = e;
      // Attempt to go up to 4 levels deep to find the cause of the exception
      while (count < 4 && t != null) {
        if (t.getClass().isAssignableFrom(throwable)) {
          return;
        }
        count++;
        t = t.getCause();
      }
      Assert.fail("Expected [" + throwable.getName() + "], but caught [" + e.getClass().getName() + "]");
      return;
    }
    Assert.fail("Expected [" + throwable.getName() + "], but no exception was thrown.");
  }

  public <T> TestIterator<T> forEach(T... collection) {
    return new TestIterator<>(this, collection);
  }

  public TestBuilder simulate(ThrowingCallable<RequestResult> callable) throws Exception {
    requestResult = callable.call();
    return this;
  }

  public class TestIterator<T> {
    public Collection<T> collection;

    public TestBuilder testBuilder;

    public TestIterator(TestBuilder testBuilder, T... collection) {
      this.collection = Arrays.asList(collection);
      this.testBuilder = testBuilder;
    }

    public TestBuilder test(ThrowingConsumer<T> consumer) throws Exception {
      for (T t : collection) {
        consumer.accept(t);
      }
      return testBuilder;
    }
  }
}
