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

import org.easymock.EasyMock;
import org.primeframework.mvc.CloseableModule;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.config.AbstractMVCConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import static org.testng.Assert.*;

/**
 * This class tests the GuiceBootstrap.
 *
 * @author Brian Pontarelli
 */
public class GuiceBootstrapTest extends PrimeBaseTest {
  @Test
  public void shutdownAndExplicitModules() {
    Injector injector = GuiceBootstrap.initialize(new AbstractModule() {
      @Override
      protected void configure() {
        bind(MVCConfiguration.class).toInstance(new AbstractMVCConfiguration() {
          @Override
          public int templateCheckSeconds() {
            return 2;
          }

          @Override
          public int l10nReloadSeconds() {
            return 1;
          }

          @Override
          public boolean allowUnknownParameters() {
            return false;
          }
        });
      }
    }, new CloseableModule());
    assertNotNull(injector.getInstance(TestClosable.class));
    assertTrue(injector.getInstance(TestClosable.class).open);

    GuiceBootstrap.shutdown(injector);
    assertFalse(injector.getInstance(TestClosable.class).open);
  }

  @Test
  public void implicitModules() {
    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.replay(context);
    ServletObjectsHolder.setServletContext(context);

    Injector injector = GuiceBootstrap.initialize(new AbstractModule() {
      @Override
      protected void configure() {
        bind(MVCConfiguration.class).toInstance(new AbstractMVCConfiguration() {
          @Override
          public int templateCheckSeconds() {
            return 2;
          }

          @Override
          public int l10nReloadSeconds() {
            return 1;
          }

          @Override
          public boolean allowUnknownParameters() {
            return false;
          }
        });
      }
    });
    assertNotNull(injector.getInstance(TestClass1.class));
    assertNotNull(injector.getInstance(TestInterface1.class));
    assertNotNull(injector.getInstance(TestClass2.class));
    assertNotNull(injector.getInstance(TestInterface2.class));
    assertNotNull(injector.getInstance(TestClass4.class));
    assertNotNull(injector.getInstance(TestInterface4.class));
    assertNotNull(injector.getInstance(ServletContext.class));

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