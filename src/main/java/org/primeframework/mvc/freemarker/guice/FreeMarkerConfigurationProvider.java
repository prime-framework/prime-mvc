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

import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.freemarker.FieldSupportBeansWrapper;

import com.google.inject.Inject;
import com.google.inject.Provider;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * Provides the FreeMarker {@link Configuration} instance.
 *
 * @author Brian Pontarelli
 */
public class FreeMarkerConfigurationProvider implements Provider<Configuration> {
  private final MVCConfiguration configuration;
  private final TemplateLoader loader;

  @Inject
  public FreeMarkerConfigurationProvider(MVCConfiguration configuration, TemplateLoader loader) {
    this.configuration = configuration;
    this.loader = loader;
  }

  @Override
  public Configuration get() {
    int checkSeconds = configuration.templateCheckSeconds();
    Configuration config = new Configuration();
    config.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
    config.setTemplateUpdateDelay(checkSeconds);
    config.setTemplateLoader(loader);
    config.setDefaultEncoding("UTF-8");
    config.setObjectWrapper(FieldSupportBeansWrapper.INSTANCE);
    return config;
  }
}
