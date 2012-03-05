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
package org.primeframework.mvc.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class provides some helpers for common tasks regarding methods.
 *
 * @author Brian Pontarelli
 */
public class MethodTools {
  /**
   * Invokes all of the methods that have the given annotation on the given Object.
   *
   * @param obj        The object to invoke the methods on.
   * @param annotation The annotation.
   */
  public static void invokeAllWithAnnotation(Object obj, Class<? extends Annotation> annotation) {
    Class<?> actionClass = obj.getClass();
    Method[] methods = actionClass.getMethods();
    for (Method method : methods) {
      if (method.getAnnotation(annotation) != null) {
        try {
          method.invoke(obj);
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Unable to call method [" + method + "] with annotation [" +
            annotation.getSimpleName() + "]", e);
        } catch (InvocationTargetException e) {
          Throwable target = e.getTargetException();
          if (target instanceof RuntimeException) {
            throw (RuntimeException) target;
          }

          throw new RuntimeException("Unable to call method [" + method + "] with annotation [" +
            annotation.getSimpleName() + "]", e);
        }
      }
    }
  }

  /**
   * Invokes the given method on the given class and handles propagation of runtime exceptions.
   *
   * @param method The method to invoke.
   * @param obj    The object to invoke the methods on.
   * @return The return from the method invocation.
   */
  @SuppressWarnings("unchecked")
  public static <T> T invoke(Method method, Object obj) {
    try {
      return (T) method.invoke(obj);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to call method [" + method + "] because it isn't accessible", e);
    } catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }

      throw new RuntimeException("Unable to call method [" + method + "]", e);
    }
  }
}
