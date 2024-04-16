/*
 * Copyright (c) 2013-2019, Inversoft Inc., All Rights Reserved
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

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.primeframework.mvc.config.MVCConfiguration;

/**
 * Guice provider for the Jackson {@link ObjectMapper}.
 *
 * @author Brian Pontarelli
 */
public class ObjectMapperProvider implements Provider<ObjectMapper> {
  private final MVCConfiguration configuration;

  private final Set<Module> jacksonModules;

  @Inject
  public ObjectMapperProvider(Set<Module> jacksonModules, MVCConfiguration configuration) {
    this.configuration = configuration;
    this.jacksonModules = jacksonModules;
  }

  @Override
  public ObjectMapper get() {
    // Use the configured value for allUnknownParameters on each get() request in case this is bound in the request scope and the configuration has changed.
    ObjectMapper objectMapper = new ObjectMapper();

    if (jacksonModules.size() > 0) {
      objectMapper.registerModules(jacksonModules);
    }

    // Bind the Prime-MVC Jackson Module
    objectMapper.registerModule(new JacksonModule());

    objectMapper.registerModule(new com.inversoft.json.JacksonModule());

    return configure(objectMapper);
  }

  protected boolean allowUnknownParameters() {
    return !configuration.allowUnknownParameters();
  }

  protected ObjectMapper configure(ObjectMapper objectMapper) {
    return objectMapper.setSerializationInclusion(Include.NON_NULL)
                       .setDefaultMergeable(true)
                       .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, allowUnknownParameters())
                       .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                       .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                       .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                       .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
  }
}
