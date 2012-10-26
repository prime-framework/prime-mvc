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
package org.primeframework.mvc.guice;

import org.primeframework.mvc.action.guice.ActionModule;
import org.primeframework.mvc.action.result.guice.ResultModule;
import org.primeframework.mvc.container.guice.ContainerModule;
import org.primeframework.mvc.control.guice.ControlModule;
import org.primeframework.mvc.freemarker.guice.FreeMarkerModule;
import org.primeframework.mvc.locale.guice.LocaleModule;
import org.primeframework.mvc.message.guice.MessageModule;
import org.primeframework.mvc.message.scope.guice.ScopeModule;
import org.primeframework.mvc.parameter.convert.guice.ConverterModule;
import org.primeframework.mvc.parameter.guice.ParameterModule;
import org.primeframework.mvc.servlet.guice.ServletModule;
import org.primeframework.mvc.util.guice.UtilModule;
import org.primeframework.mvc.validation.guice.ValidationModule;
import org.primeframework.mvc.workflow.guice.WorkflowModule;

import com.google.inject.AbstractModule;

/**
 * A virtual module that installs all of the Prime MVC modules.
 *
 * @author Brian Pontarelli
 */
public class PrimeModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ActionModule());
    install(new ContainerModule());
    install(new ControlModule());
    install(new ConverterModule());
    install(new FreeMarkerModule());
    install(new LocaleModule());
    install(new MessageModule());
    install(new ParameterModule());
    install(new ResultModule());
    install(new ScopeModule());
    install(new org.primeframework.mvc.scope.guice.ScopeModule());
    install(new ServletModule());
    install(new UtilModule());
    install(new ValidationModule());
    install(new WorkflowModule());
  }
}
