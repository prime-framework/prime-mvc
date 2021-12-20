/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import java.lang.reflect.Array;

public class ArrayUtils {
  public static String[] EMPTY_STRING_ARRAY = new String[0];

  public static char[] clone(final char[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  public static boolean isNotEmpty(final Object[] array) {
    return !isEmpty(array);
  }

  public static boolean isEmpty(final Object[] array) {
    return getLength(array) == 0;
  }

  public static int getLength(final Object array) {
    if (array == null) {
      return 0;
    }
    return Array.getLength(array);
  }

  public static byte[] clone(final byte[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  public static <T> T[] toArray(final T... items) {
    return items;
  }
}
