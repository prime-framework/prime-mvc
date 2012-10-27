/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.convert;

import java.util.Map;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.parameter.convert.converters.BooleanConverter;
import org.primeframework.mvc.parameter.convert.converters.CharacterConverter;
import org.primeframework.mvc.parameter.convert.converters.NumberConverter;
import org.primeframework.mvc.parameter.convert.converters.StringConverter;
import org.testng.annotations.Test;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import static org.testng.Assert.*;

/**
 * This class tests the converter registry.
 *
 * @author Brian Pontarelli
 */
public class DefaultConverterProviderTest extends PrimeBaseTest {
  /**
   * Test the lookup of converters.
   */
  @Test
  public void lookups() {
    Injector injector = GuiceBootstrap.initialize(new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    ConverterProvider provider = new DefaultConverterProvider(injector, injector.getInstance(Key.get(new TypeLiteral<Map<Class<?>, GlobalConverter>>(){})));
    GlobalConverter tc = provider.lookup(Character.class);
    assertSame(CharacterConverter.class, tc.getClass());
    tc = provider.lookup(Character.TYPE);
    assertSame(CharacterConverter.class, tc.getClass());
    tc = provider.lookup(Character[].class);
    assertSame(CharacterConverter.class, tc.getClass());
    tc = provider.lookup(char[].class);
    assertSame(CharacterConverter.class, tc.getClass());

    tc = provider.lookup(Byte.class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Byte.TYPE);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Byte[].class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(byte[].class);
    assertSame(NumberConverter.class, tc.getClass());

    tc = provider.lookup(Short.class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Short.TYPE);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Short[].class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(short[].class);
    assertSame(NumberConverter.class, tc.getClass());

    tc = provider.lookup(Integer.class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Integer.TYPE);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Integer[].class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(int[].class);
    assertSame(NumberConverter.class, tc.getClass());

    tc = provider.lookup(Long.class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Long.TYPE);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Long[].class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(long[].class);
    assertSame(NumberConverter.class, tc.getClass());

    tc = provider.lookup(Float.class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Float.TYPE);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Float[].class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(float[].class);
    assertSame(NumberConverter.class, tc.getClass());

    tc = provider.lookup(Double.class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Double.TYPE);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(Double[].class);
    assertSame(NumberConverter.class, tc.getClass());
    tc = provider.lookup(double[].class);
    assertSame(NumberConverter.class, tc.getClass());

    tc = provider.lookup(Boolean.class);
    assertSame(BooleanConverter.class, tc.getClass());
    tc = provider.lookup(Boolean.TYPE);
    assertSame(BooleanConverter.class, tc.getClass());
    tc = provider.lookup(Boolean[].class);
    assertSame(BooleanConverter.class, tc.getClass());
    tc = provider.lookup(boolean[].class);
    assertSame(BooleanConverter.class, tc.getClass());

    tc = provider.lookup(String.class);
    assertSame(StringConverter.class, tc.getClass());
    tc = provider.lookup(String[].class);
    assertSame(StringConverter.class, tc.getClass());

    // Failure
    tc = provider.lookup(this.getClass());
    assertNull(tc);
  }
}