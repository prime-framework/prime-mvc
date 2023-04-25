/*
 * Copyright (c) 2016-2023, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPContext;
import org.example.domain.User;
import org.primeframework.mock.MockUserAgent;
import org.primeframework.mvc.security.MockUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.test.RequestResult;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.ThrowingCallable;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Daniel DeGroff
 */
public class TestBuilder {
  public HTTPContext context;

  public Function<ObjectMapper, ObjectMapper> objectMapperFunction;

  public RequestResult requestResult;

  @Inject public UserLoginSecurityContext securityContext;

  public RequestSimulator simulator;

  public Path tempFile;

  public MockUserAgent userAgent;

  public TestBuilder assertContextAttributeNotNull(String attributeName) {
    assertNotNull(context.getAttribute(attributeName));
    return this;
  }

  public TestBuilder assertCookie(String name, String value) {
    Cookie actual = userAgent.getCookies(requestResult.request)
                             .stream()
                             .filter(c -> c.name.equals(name))
                             .findFirst()
                             .orElse(null);
    assertNotNull(actual);
    assertEquals(actual.value, value);
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
    String unique = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()), StandardCharsets.UTF_8).substring(0, 5);
    tempFile = Path.of(tmpdir + "/" + "_prime_testFile_" + unique);
    tempFile.toFile().deleteOnExit();

    Files.write(tempFile, contents.getBytes());
    return this;
  }

  public <T> TestIterator<T> forEach(T... collection) {
    return new TestIterator<>(this, collection);
  }

  public TestBuilder loginUserWithRole(String... roles) {
    securityContext.logout();
    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext.roles.addAll(Arrays.asList(roles));

    securityContext.login(new User());
    return this;
  }

  public TestBuilder simulate(ThrowingCallable<RequestResult> callable) throws Exception {
    requestResult = callable.call();
    return this;
  }

  public static class TestIterator<T> {
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
