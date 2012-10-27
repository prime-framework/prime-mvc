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
import javax.servlet.ServletException;

import java.io.IOException;

import org.easymock.EasyMock;
import org.primeframework.mvc.CloseableModule;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.config.AbstractMVCConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.primeframework.mvc.workflow.MVCWorkflow;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import static org.testng.Assert.*;

/**
 * This class tests the GuiceBootstrap.
 *
 * @author Brian Pontarelli
 */
public class GuiceBootstrapTest extends PrimeBaseTest {
  @Test
  public void shutdownAndExplicitModules() {
    Injector injector = GuiceBootstrap.initialize(new MVCModule() {
      @Override
      protected void configure() {
        super.configure();

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

        install(new CloseableModule());
      }
    });
    assertNotNull(injector.getInstance(TestClosable.class));
    assertTrue(injector.getInstance(TestClosable.class).open);

    GuiceBootstrap.shutdown(injector);
    assertFalse(injector.getInstance(TestClosable.class).open);
  }

  @Test
  public void overrideModules() {
    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    EasyMock.replay(context);
    ServletObjectsHolder.setServletContext(context);

    Injector injector = GuiceBootstrap.initialize(Modules.override(new MVCModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(MVCWorkflow.class).to(TestMVCWorkflow.class);
        bind(MVCConfiguration.class).to(MockConfiguration.class);
      }
    }));

    assertSame(injector.getInstance(MVCWorkflow.class).getClass(), TestMVCWorkflow.class);
  }

  public static class TestMVCWorkflow implements MVCWorkflow {
    @Override
    public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    }
  }
}
