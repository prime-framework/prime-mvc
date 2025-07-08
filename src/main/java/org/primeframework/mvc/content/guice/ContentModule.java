/*
` * Copyright (c) 2012-2025, Inversoft Inc., All Rights Reserved
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
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.primeframework.mvc.action.config.ActionConfigurator;
import org.primeframework.mvc.content.ContentWorkflow;
import org.primeframework.mvc.content.DefaultContentHandler;
import org.primeframework.mvc.content.DefaultContentWorkflow;
import org.primeframework.mvc.content.ServerHandledContentHandler;
import org.primeframework.mvc.content.binary.BinaryActionConfigurator;
import org.primeframework.mvc.content.binary.BinaryContentHandler;
import org.primeframework.mvc.content.json.JacksonActionConfigurator;
import org.primeframework.mvc.content.json.JacksonContentHandler;
import org.primeframework.mvc.content.json.JacksonPatchContentHandler;

/**
 * This class is a Guice module that configures the ContentHandlerFactory and the default ContentHandlers.
 *
 * @author Brian Pontarelli
 */
public class ContentModule extends AbstractModule {
  protected void bindContentHandlers() {
    // Bind the Jackson objects and content handler
    ContentHandlerFactory.addContentHandler(binder(), "application/json", JacksonContentHandler.class);
    ContentHandlerFactory.addContentHandler(binder(), "application/json-patch+json", JacksonPatchContentHandler.class);
    ContentHandlerFactory.addContentHandler(binder(), "application/merge-patch+json", JacksonPatchContentHandler.class);
    ContentHandlerFactory.addContentHandler(binder(), "application/octet-stream", BinaryContentHandler.class);
    // Note that multipart and form data are handled by the server.
    ContentHandlerFactory.addContentHandler(binder(), "multipart/form-data", ServerHandledContentHandler.class);
    ContentHandlerFactory.addContentHandler(binder(), "application/x-www-form-urlencoded", ServerHandledContentHandler.class);

    // Default exploding handler to handle missing Content-Type header, or un-supported values.
    ContentHandlerFactory.addContentHandler(binder(), "", DefaultContentHandler.class);

    Multibinder<ActionConfigurator> multiBinder = Multibinder.newSetBinder(binder(), ActionConfigurator.class);
    multiBinder.addBinding().to(JacksonActionConfigurator.class);
    multiBinder.addBinding().to(BinaryActionConfigurator.class);

    // Set up the Jackson Module bindings and the provider for the ObjectMapper
    Multibinder.newSetBinder(binder(), Module.class);
    bindObjectMapper();
  }

  protected void bindObjectMapper() {
    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).asEagerSingleton();
  }

  @Override
  protected void configure() {
    bind(ContentWorkflow.class).to(DefaultContentWorkflow.class);
    bind(ContentHandlerFactory.class);

    bindContentHandlers();
  }
}
