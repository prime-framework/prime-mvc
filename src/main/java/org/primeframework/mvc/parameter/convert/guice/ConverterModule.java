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
package org.primeframework.mvc.parameter.convert.guice;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.primeframework.mvc.guice.AbstractPrimeModule;
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

/**
 * This class is a guice module for the Prime MVC converters.
 *
 * @author Brian Pontarelli
 */
public class ConverterModule extends AbstractPrimeModule {
  @Override
  protected void configure() {
    addGlobalConverter(BooleanConverter.class, Boolean.class, boolean.class);
    addGlobalConverter(CharacterConverter.class, Character.class, char.class);
    addGlobalConverter(CollectionConverter.class, Collection.class);
    addGlobalConverter(DateTimeConverter.class, DateTime.class);
    addGlobalConverter(EnumConverter.class, Enum.class);
    addGlobalConverter(FileConverter.class, File.class);
    addGlobalConverter(LocalDateConverter.class, LocalDate.class);
    addGlobalConverter(LocaleConverter.class, Locale.class);
    addGlobalConverter(NumberConverter.class, Number.class, byte.class, short.class, int.class, long.class, float.class, double.class, BigDecimal.class, BigInteger.class);
    addGlobalConverter(StringConverter.class, String.class);
  }
}
