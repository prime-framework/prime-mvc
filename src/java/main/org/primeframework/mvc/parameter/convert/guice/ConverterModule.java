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
import org.primeframework.mvc.parameter.convert.GlobalConverter;
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
import com.google.inject.multibindings.MapBinder;

/**
 * This class is a guice module for the Prime MVC converters.
 *
 * @author Brian Pontarelli
 */
public class ConverterModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<Class, GlobalConverter> binder = MapBinder.newMapBinder(binder(), Class.class, GlobalConverter.class);
    binder.addBinding(Boolean.class).to(BooleanConverter.class).asEagerSingleton();
    binder.addBinding(boolean.class).to(BooleanConverter.class).asEagerSingleton();
    binder.addBinding(Character.class).to(CharacterConverter.class).asEagerSingleton();
    binder.addBinding(char.class).to(CharacterConverter.class).asEagerSingleton();
    binder.addBinding(Collection.class).to(CollectionConverter.class).asEagerSingleton();
    binder.addBinding(DateTime.class).to(DateTimeConverter.class).asEagerSingleton();
    binder.addBinding(Enum.class).to(EnumConverter.class).asEagerSingleton();
    binder.addBinding(File.class).to(FileConverter.class).asEagerSingleton();
    binder.addBinding(LocalDate.class).to(LocalDateConverter.class).asEagerSingleton();
    binder.addBinding(Locale.class).to(LocaleConverter.class).asEagerSingleton();
    binder.addBinding(Number.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(byte.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(short.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(int.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(long.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(float.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(double.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(BigDecimal.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(BigInteger.class).to(NumberConverter.class).asEagerSingleton();
    binder.addBinding(String.class).to(StringConverter.class).asEagerSingleton();
  }
}
