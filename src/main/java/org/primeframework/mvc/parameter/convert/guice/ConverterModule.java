/*
 * Copyright (c) 2012-2018, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.convert.guice;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import org.primeframework.mvc.parameter.convert.converters.BooleanConverter;
import org.primeframework.mvc.parameter.convert.converters.CharacterConverter;
import org.primeframework.mvc.parameter.convert.converters.CollectionConverter;
import org.primeframework.mvc.parameter.convert.converters.EnumConverter;
import org.primeframework.mvc.parameter.convert.converters.FileConverter;
import org.primeframework.mvc.parameter.convert.converters.LocalDateConverter;
import org.primeframework.mvc.parameter.convert.converters.LocaleConverter;
import org.primeframework.mvc.parameter.convert.converters.NumberConverter;
import org.primeframework.mvc.parameter.convert.converters.StringConverter;
import org.primeframework.mvc.parameter.convert.converters.URIConverter;
import org.primeframework.mvc.parameter.convert.converters.URLConverter;
import org.primeframework.mvc.parameter.convert.converters.UUIDConverter;
import org.primeframework.mvc.parameter.convert.converters.ZoneIdConverter;
import org.primeframework.mvc.parameter.convert.converters.ZonedDateTimeConverter;

import com.google.inject.AbstractModule;

/**
 * This class is a guice module for the Prime MVC converters.
 *
 * @author Brian Pontarelli
 */
public class ConverterModule extends AbstractModule {
  @Override
  protected void configure() {
    GlobalConverterBinder binder = GlobalConverterBinder.newGlobalConverterBinder(binder());
    binder.add(BooleanConverter.class).forTypes(Boolean.class, boolean.class);
    binder.add(CharacterConverter.class).forTypes(Character.class, char.class);
    binder.add(CollectionConverter.class).forTypes(Collection.class);
    binder.add(ZonedDateTimeConverter.class).forTypes(ZonedDateTime.class);
    binder.add(EnumConverter.class).forTypes(Enum.class);
    binder.add(FileConverter.class).forTypes(File.class);
    binder.add(LocalDateConverter.class).forTypes(LocalDate.class);
    binder.add(LocaleConverter.class).forTypes(Locale.class);
    binder.add(NumberConverter.class).forTypes(Number.class, byte.class, short.class, int.class, long.class, float.class, double.class, BigDecimal.class, BigInteger.class);
    binder.add(StringConverter.class).forTypes(String.class);
    binder.add(URIConverter.class).forTypes(URI.class);
    binder.add(URLConverter.class).forTypes(URL.class);
    binder.add(UUIDConverter.class).forTypes(UUID.class);
    binder.add(ZoneIdConverter.class).forTypes(ZoneId.class);
  }
}
