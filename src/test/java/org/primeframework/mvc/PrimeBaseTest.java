/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.util.Locale;

import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * This class is a base test for testing the Prime framework. It isn't recommended to use it outside of the Prime
 * project.
 *
 * @author Brian Pontarelli and James Humphrey
 */
public abstract class PrimeBaseTest {
  protected static Injector injector;
  protected static MockServletContext context;
  protected static MockHttpSession session;
  protected MockHttpServletRequest request;
  protected MockHttpServletResponse response;

  @BeforeSuite
  public static void init() {
    context = new MockServletContext(new File("src/test/web"));
    session = new MockHttpSession(context);
    ServletObjectsHolder.setServletContext(context);

    injector = GuiceBootstrap.initialize(new TestModule());
  }
  
  /**
   * Sets up the servlet objects and injects the test.
   */
  @BeforeMethod
  public void setUp() {
    request = new MockHttpServletRequest("/", Locale.getDefault(), false, "utf-8", session);
    response = new MockHttpServletResponse();

    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(request));
    ServletObjectsHolder.setServletResponse(response);

    injector.injectMembers(this);
  }
  
  @AfterMethod
  public void tearDown() {
    ServletObjectsHolder.clearServletRequest();
    ServletObjectsHolder.clearServletResponse();
  }
  
  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(MVCConfiguration.class).toInstance(new MockConfiguration());
    }
  }
}
