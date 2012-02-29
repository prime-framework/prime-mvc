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
package org.primeframework.mvc.guice;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.FileConfiguration;
import org.easymock.EasyMock;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.testng.annotations.Test;

import com.google.inject.Injector;
import static org.testng.Assert.*;

/**
 * <p> This class tests the GuiceContainer. </p>
 *
 * @author Brian Pontarelli
 */
public class GuiceContainerTest {
//  @Test
//  public void testExplicitModules() {
//    GuiceContainer.setGuiceModules();
//    GuiceContainer.setExcludeGuiceModules(null);
//    GuiceContainer.setGuiceModulesNames("org.jcatapult.guice.TestModule1 , org.jcatapult.guice.TestModule2");
//    GuiceContainer.setLoadFromClasspath(false);
//    GuiceContainer.initialize();
//    Injector injector = GuiceContainer.getInjector();
//    assertNotNull(injector.getInstance(TestClass1.class));
//    assertNotNull(injector.getInstance(TestClass2.class));
//  }
//
//  @Test
//  public void shutdown() {
//    GuiceContainer.setGuiceModules();
//    GuiceContainer.setExcludeGuiceModules(null);
//    GuiceContainer.setGuiceModulesNames("org.jcatapult.guice.ClosableModule");
//    GuiceContainer.setLoadFromClasspath(false);
//    GuiceContainer.initialize();
//    Injector injector = GuiceContainer.getInjector();
//    assertNotNull(injector.getInstance(TestClosable.class));
//    assertTrue(injector.getInstance(TestClosable.class).open);
//
//    GuiceContainer.shutdown();
//    assertFalse(injector.getInstance(TestClosable.class).open);
//    assertNull(GuiceContainer.getInjector());
//  }

  @Test
  public void testImplicitModules() {
    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.replay(context);
    ServletObjectsHolder.setServletContext(context);

    GuiceContainer.setGuiceModules();
    GuiceContainer.setExcludeGuiceModules(null);
    GuiceContainer.setGuiceModulesNames(null);
    GuiceContainer.initialize();
    Injector injector = GuiceContainer.getInjector();
    assertNotNull(injector.getInstance(TestClass1.class));
    assertNotNull(injector.getInstance(TestInterface1.class));
    assertNotNull(injector.getInstance(TestClass2.class));
    assertNotNull(injector.getInstance(TestInterface2.class));
    assertNotNull(injector.getInstance(TestClass4.class));
    assertNotNull(injector.getInstance(TestInterface4.class));
    assertNotNull(injector.getInstance(ServletContext.class));

    try {
      injector.getInstance(FileConfiguration.class);
      fail("Should have failed");
    } catch (Exception e) {
      // Expected since Guice throws exceptions for missing bindings
    }

    try {
      injector.getInstance(TestInterface3.class);
      fail("Should have failed");
    } catch (Exception e) {
      // Expected since Guice throws exceptions for missing bindings
    }

    try {
      injector.getInstance(TestInterface3.class);
      fail("Should have failed");
    } catch (Exception e) {
      // Expected since Guice throws exceptions for missing bindings
    }
  }
}
