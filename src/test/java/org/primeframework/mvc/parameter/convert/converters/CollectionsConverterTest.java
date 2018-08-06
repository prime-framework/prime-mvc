/*
 * Copyright (c) 2018, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.convert.converters;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.TestBuilder;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static org.testng.Assert.assertEquals;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author Daniel DeGroff
 */
public class CollectionsConverterTest extends PrimeBaseTest {
  @Inject
  public CollectionConverter converter;

  @Test
  public void fromStrings() {
    withValues("foo", "bar").assertConversion();
    withValues("foo", "bar,baz").assertConversion();
    withValues(",foo,", "bar,baz").assertConversion();

    withValues(",foo,").assertConversion();
    withValues("foo,bar").assertConversion();

    withValues("").withExpectedResult(new String[0]).assertConversion();
    withValues("foo", "").withExpectedResult(new String[]{"foo"}).assertConversion();
  }

  @Test
  public void toStrings() {
    TestBuilder.expectException(ConverterStateException.class,
        () -> converter.convertToString(ParameterizedTypeImpl.make(Set.class, new Type[]{String.class}, null), null, "variations", new String[]{"foo", "bar"}));
  }

  private Builder withValues(String... values) {
    return new Builder(values);
  }

  private class Builder {
    public String[] values;

    public Object[] expected;

    public Builder(String... values) {
      this.values = values;
    }

    public Builder withExpectedResult(Object[] expected) {
      this.expected = expected;
      return this;
    }

    @SuppressWarnings("unchecked")
    private void assertConversion() {
      if (expected == null) {
        expected = values;
      }

      // --> java.util.Set
      Set<String> set = (Set<String>) converter.convertFromStrings(ParameterizedTypeImpl.make(Set.class, new Type[]{String.class}, null), null, "variations", values);
      assertEquals(new TreeSet(set), new TreeSet<>(Arrays.asList(expected)));

      // Without a parameterized type
      set = (Set<String>) converter.convertFromStrings(HashSet.class, null, "variations", values);
      assertEquals(new TreeSet(set), new TreeSet<>(Arrays.asList(expected)));

      // --> java.util.TreeSet
      TreeSet<String> sortedSet = (TreeSet<String>) converter.convertFromStrings(ParameterizedTypeImpl.make(TreeSet.class, new Type[]{String.class}, null), null, "variations", values);
      assertEquals(sortedSet, new TreeSet<>(Arrays.asList(expected)));

      // Without a parameterized type
      sortedSet = (TreeSet<String>) converter.convertFromStrings(TreeSet.class, null, "variations", values);
      assertEquals(sortedSet, new TreeSet<>(Arrays.asList(expected)));

      // --> java.util.List
      List<String> list = (List<String>) converter.convertFromStrings(ParameterizedTypeImpl.make(List.class, new Type[]{String.class}, null), null, "variations", values);
      assertEquals(list, new ArrayList<>(Arrays.asList(expected)));

      // Without a parameterized type
      list = (List<String>) converter.convertFromStrings(ParameterizedTypeImpl.make(List.class, new Type[]{String.class}, null), null, "variations", values);
      assertEquals(list, new ArrayList<>(Arrays.asList(expected)));
    }
  }
}
