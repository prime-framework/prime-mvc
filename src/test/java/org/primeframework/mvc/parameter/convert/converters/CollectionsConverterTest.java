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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.primeframework.mvc.PrimeBaseTest;
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

    // We can't really tell the difference between a list of strings with one element that happens to contain a comma and a string
    // that contains one or more comma's that you wish to have marshalled into a collection. So these behave a little differently.
    withValues(",foo,").withExpectedCollectionOf("foo").assertConversion();
    withValues("foo,bar").withExpectedCollectionOf("foo", "bar").assertConversion();
  }

  private Builder withValues(String... values) {
    return new Builder(values);
  }

  private class Builder {
    public String[] values;

    public Collection<String> expected;

    public Builder(String... values) {
      this.values = values;
    }

    public Builder withExpectedCollectionOf(String... expectedValues) {
      this.expected = new ArrayList<>(Arrays.asList(expectedValues));
      return this;
    }

    @SuppressWarnings("unchecked")
    private void assertConversion() {
      // --> java.util.Set
      Set<String> set = (Set<String>) converter.convertFromStrings(ParameterizedTypeImpl.make(Set.class, new Type[]{String.class}, null), null, "variations", values);
      if (expected == null) {
        // ensure we compare sorted collections
        assertEquals(new TreeSet(set), new TreeSet<>(Arrays.asList(values)));
      } else {
        assertEquals(new TreeSet(set), new TreeSet(expected));
      }

      // --> java.util.List
      List<String> list = (List<String>) converter.convertFromStrings(ParameterizedTypeImpl.make(List.class, new Type[]{String.class}, null), null, "variations", values);
      if (expected == null) {
        assertEquals(list, new ArrayList<>(Arrays.asList(values)));
      } else {
        assertEquals(list, expected);
      }
    }
  }
}
