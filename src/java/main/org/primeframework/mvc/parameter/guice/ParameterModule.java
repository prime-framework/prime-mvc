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
package org.primeframework.mvc.parameter.guice;

import org.primeframework.mvc.parameter.convert.DefaultConverterProvider;
import org.primeframework.mvc.parameter.convert.converters.BooleanConverter;
import org.primeframework.mvc.parameter.convert.converters.CharacterConverter;
import org.primeframework.mvc.parameter.convert.converters.CollectionConverter;
import org.primeframework.mvc.parameter.convert.converters.DateTimeConverter;
import org.primeframework.mvc.parameter.convert.converters.EnumConverter;
import org.primeframework.mvc.parameter.convert.converters.FileConverter;
import org.primeframework.mvc.parameter.convert.converters.LocalDateConverter;
import org.primeframework.mvc.parameter.convert.converters.LocaleConverter;
import org.primeframework.mvc.parameter.convert.converters.NumberConverter;
import org.primeframework.mvc.parameter.convert.converters.StringConverter;

import com.google.inject.AbstractModule;

/**
 * This class is a guice module for the Prime MVC parameter support.
 *
 * @author Brian Pontarelli
 */
public class ParameterModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(BooleanConverter.class);
    bind(CharacterConverter.class);
    bind(CollectionConverter.class);
    bind(DateTimeConverter.class);
    bind(EnumConverter.class);
    bind(FileConverter.class);
    bind(LocalDateConverter.class);
    bind(LocaleConverter.class);
    bind(NumberConverter.class);
    bind(StringConverter.class);

    // Inject the registry so that the Class to Class mapping is setup
    requestStaticInjection(DefaultConverterProvider.class);
  }
}
