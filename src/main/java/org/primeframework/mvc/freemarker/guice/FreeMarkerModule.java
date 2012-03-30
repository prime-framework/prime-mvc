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
package org.primeframework.mvc.freemarker.guice;

import org.primeframework.mvc.freemarker.OverridingTemplateLoader;
import org.primeframework.mvc.freemarker.methods.JSONEscape;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * FreeMarker module that provides the {@link Configuration} instance (as a singleton).
 *
 * @author Brian Pontarelli
 */
public class FreeMarkerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TemplateLoader.class).to(OverridingTemplateLoader.class);
    bind(Configuration.class).toProvider(FreeMarkerConfigurationProvider.class).in(Singleton.class);

    TemplateModelBinder templateModelBinder = TemplateModelBinder.newTemplateModelBinder(binder());
    templateModelBinder.add(JSONEscape.class).withName("jsonescape");
  }
}
