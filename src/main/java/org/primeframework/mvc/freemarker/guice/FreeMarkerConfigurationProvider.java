/*
 * Copyright (c) 2012-2020, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import com.google.inject.Provider;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.TemplateClassResolver;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import org.primeframework.mvc.config.MVCConfiguration;

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
    BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_30);
    builder.setExposeFields(true);
    builder.setSimpleMapWrapper(true);

    int checkSeconds = configuration.templateCheckSeconds();
    Configuration config = new Configuration(Configuration.VERSION_2_3_30);
    config.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
    config.setTemplateUpdateDelayMilliseconds(checkSeconds * 1000L);
    config.setTemplateLoader(loader);
    config.setDefaultEncoding("UTF-8");
    config.setObjectWrapper(builder.build());
    config.setNumberFormat("computer");

    // Security settings
    // Use html output format by default so that we always auto escape (unless explicitly told not to by each template)
    if (configuration.autoHTMLEscapingEnabled()) {
      config.setOutputFormat(HTMLOutputFormat.INSTANCE);
    }

    // 'UNRESTRICTED_RESOLVER' is the default at 2.3.x (Fail!), starting in 2.4 it will be SAFER_RESOLVER, set to Allow Nothing!
    config.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
    // Disable the API built in, should be disabled by default, trust no one.
    config.setAPIBuiltinEnabled(false);

    return config;
  }
}
