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
package org.primeframework.mvc.content.guice;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.primeframework.mvc.action.config.ActionConfigurator;
import org.primeframework.mvc.content.ContentWorkflow;
import org.primeframework.mvc.content.DefaultContentWorkflow;
import org.primeframework.mvc.content.json.JacksonActionConfigurator;
import org.primeframework.mvc.content.json.JacksonContentHandler;
import org.primeframework.mvc.content.json.converter.PrimeJacksonModule;

/**
 * This class is a Guice module that configures the ContentHandlerFactory and the default ContentHandlers.
 *
 * @author Brian Pontarelli
 */
public class ContentModule extends AbstractModule {
  protected void bindJSON() {
    // Bind the Jackson objects and content handler
    ContentHandlerFactory.addContentHandler(binder(), "application/json", JacksonContentHandler.class);
    Multibinder.newSetBinder(binder(), ActionConfigurator.class).addBinding().to(JacksonActionConfigurator.class);

    // Setup the Jackson Module bindings and the provider for the ObjectMapper
    Multibinder<Module> moduleBinder = Multibinder.newSetBinder(binder(), Module.class);
    moduleBinder.addBinding().to(JodaModule.class);
    moduleBinder.addBinding().to(PrimeJacksonModule.class);

    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).asEagerSingleton();
  }

  @Override
  protected void configure() {
    bind(ContentWorkflow.class).to(DefaultContentWorkflow.class);
    bind(ContentHandlerFactory.class);

    bindJSON();
  }
}