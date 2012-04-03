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
package org.primeframework.mvc.validation.jsr303.util;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.*;

/**
 * JSR validation helpers.
 *
 * @author Brian Pontarelli
 */
public class ValidationUtils {
  public static final String LENGTH = "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890";
  public static final String UNICODE;

  static {
    int[] ia = new int[10];
    for (int i = 0; i < 10; i++) {
      ia[i] = 0x10000 + i;
    }
    UNICODE = new String(ia, 0, 10);
  }

  /**
   * Correctly handles JSR Path instances for Prime.
   *
   * @param path The path.
   * @return The path as a String.
   */
  public static String toString(Path path) {
    StringBuilder build = new StringBuilder();
    boolean first = true;
    for (Node node : path) {
      if (node.isInIterable()) {
        addIndex(build, node);
      }

      if (!first) {
        build.append(".");
      }

      build.append(node.getName());

      first = false;
    }

    return build.toString();
  }

  private static void addIndex(StringBuilder build, Node node) {
    Integer index = node.getIndex();
    Object key = node.getKey();

    build.append("[");
    if (index != null) {
      build.append(index);
    } else if (key != null) {
      build.append("'").append(key).append("'");
    }
    build.append("]");
  }

  /**
   * Useful for testing purposes.
   *
   * @param errorHandler The error handler call back.
   * @param violations The actual violations from the JSR.
   * @param errors The expected errors.
   * @param <T> For JSR.
   */
  public static <T> void checkErrors(ErrorHandler errorHandler, Set<ConstraintViolation<T>> violations, ValidationError... errors) {
    Set<ValidationError> actual = new TreeSet<ValidationError>();
    for (ConstraintViolation<T> violation : violations) {
      actual.add(new ValidationError(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
        ValidationUtils.toString(violation.getPropertyPath())));
    }

    Set<ValidationError> expected = new TreeSet<ValidationError>(asList(errors));
    if (!expected.equals(actual)) {
      errorHandler.handle(expected, actual);
    }
  }

  /**
   * Used for checking errors.
   */
  public static interface ErrorHandler {
    void handle(Set<ValidationError> expected, Set<ValidationError> actual);
  }

  /**
   * @author Brian Pontarelli
   */
  public static class ValidationError implements Comparable<ValidationError> {
    public final String code;
    public final String path;

    public ValidationError(String code, String path) {
      this.path = path;
      this.code = code;
    }

    @Override
    public int compareTo(ValidationError o) {
      int result = code.compareTo(o.code);
      if (result == 0) {
        return path.compareTo(o.path);
      }

      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final ValidationError that = (ValidationError) o;

      return code.equals(that.code) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
      int result = code.hashCode();
      result = 31 * result + path.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "ValidationError{" +
        "code='" + code + '\'' +
        ", path='" + path + '\'' +
        '}';
    }
  }
}
