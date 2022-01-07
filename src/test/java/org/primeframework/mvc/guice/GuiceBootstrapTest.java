/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import org.primeframework.mvc.CloseableModule;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.NoCORSConfigurationProvider;
import org.primeframework.mvc.security.MockUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.workflow.MVCWorkflow;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the GuiceBootstrap.
 *
 * @author Brian Pontarelli
 */
public class GuiceBootstrapTest extends PrimeBaseTest {
  @Test
  public void overrideModules() {
    Injector injector = GuiceBootstrap.initialize(Modules.override(new MVCModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(MVCWorkflow.class).to(TestMVCWorkflow.class);
        bind(MVCConfiguration.class).to(MockConfiguration.class);
        bind(UserLoginSecurityContext.class).to(MockUserLoginSecurityContext.class);
      }
    }));

    assertSame(injector.getInstance(MVCWorkflow.class).getClass(), TestMVCWorkflow.class);
  }

  @Test
  public void shutdownAndExplicitModules() {
    Injector injector = GuiceBootstrap.initialize(new MVCModule() {
      @Override
      protected void configure() {
        super.configure();

        bind(MVCConfiguration.class).toInstance(new MockConfiguration(2, 1, false, false));
        bind(UserLoginSecurityContext.class).to(MockUserLoginSecurityContext.class);
        bind(CORSConfigurationProvider.class).to(NoCORSConfigurationProvider.class);

        install(new CloseableModule());
      }
    });
    assertNotNull(injector.getInstance(TestClosable.class));
    assertTrue(injector.getInstance(TestClosable.class).open);

    GuiceBootstrap.shutdown(injector);
    assertFalse(injector.getInstance(TestClosable.class).open);
  }

  public static class TestMVCWorkflow implements MVCWorkflow {
    @Override
    public void perform(WorkflowChain workflowChain) throws IOException {
    }
  }
}
