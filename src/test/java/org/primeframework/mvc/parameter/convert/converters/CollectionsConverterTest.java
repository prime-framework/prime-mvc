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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.inject.Inject;
import com.google.inject.internal.MoreTypes;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel DeGroff
 */
public class CollectionsConverterTest extends PrimeBaseTest {
  @Inject
  public CollectionConverter converter;

  @Test
  public void fromStrings() {
    withValues("foo", "bar")
        .withExpectedResult(new String[]{"foo", "bar"})
        .assertConversion();

    withValues("foo", "bar,baz")
        .withExpectedResult(new String[]{"foo", "bar,baz"})
        .assertConversion();

    withValues(",foo,", "bar,baz")
        .withExpectedResult(new String[]{",foo,", "bar,baz"})
        .assertConversion();

    withValues(",foo,")
        .withExpectedResult(new String[]{",foo,"})
        .assertConversion();

    withValues("foo,bar")
        .withExpectedResult(new String[]{"foo,bar"}).assertConversion();

    // Expect a single empty string will result in an empty collection
    withValues("")
        .withExpectedResult(new String[]{})
        .assertConversion();

    // Expect a value and an empty value will result in a collection with a single value
    withValues("foo", "")
        .withExpectedResult(new String[]{"foo"}
        ).assertConversion();
  }

  @Test
  public void toStrings() {
    expectException(ConverterStateException.class,
        () -> converter.convertToString(new MoreTypes.ParameterizedTypeImpl(null, Set.class, String.class), null, "variations", new String[]{"foo", "bar"}));
  }

  private Builder withValues(String... values) {
    return new Builder(values);
  }

  private class Builder {
    public Object[] expected;

    public String[] values;

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
      Set<String> set = (Set<String>) converter.convertFromStrings(new MoreTypes.ParameterizedTypeImpl(null, Set.class, String.class), null, "variations", values);
      assertEquals(new TreeSet<>(set), new TreeSet<>(Arrays.asList(expected)));

      // Without a parameterized type
      set = (Set<String>) converter.convertFromStrings(HashSet.class, null, "variations", values);
      assertEquals(new TreeSet<>(set), new TreeSet<>(Arrays.asList(expected)));

      // --> java.util.TreeSet
      TreeSet<String> sortedSet = (TreeSet<String>) converter.convertFromStrings(new MoreTypes.ParameterizedTypeImpl(null, TreeSet.class, String.class), null, "variations", values);
      assertEquals(sortedSet, new TreeSet<>(Arrays.asList(expected)));

      // Without a parameterized type
      sortedSet = (TreeSet<String>) converter.convertFromStrings(TreeSet.class, null, "variations", values);
      assertEquals(sortedSet, new TreeSet<>(Arrays.asList(expected)));

      // --> java.util.List
      List<String> list = (List<String>) converter.convertFromStrings(new MoreTypes.ParameterizedTypeImpl(null, List.class, String.class), null, "variations", values);
      assertEquals(list, new ArrayList<>(Arrays.asList(expected)));

      // Without a parameterized type
      list = (List<String>) converter.convertFromStrings(new MoreTypes.ParameterizedTypeImpl(null, List.class, String.class), null, "variations", values);
      assertEquals(list, new ArrayList<>(Arrays.asList(expected)));
    }
  }
}
