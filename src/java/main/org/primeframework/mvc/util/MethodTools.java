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

import org.primeframework.mvc.ErrorException;

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
   * @param params The parameters passed to the method.
   * @return The return from the method invocation.
   */
  @SuppressWarnings("unchecked")
  public static <T> T invoke(Method method, Object obj, Object... params) {
    try {
      return (T) method.invoke(obj, params);
    } catch (IllegalAccessException e) {
      throw new ErrorException("Unable to call method [" + method + "] because it isn't accessible", e);
    } catch (IllegalArgumentException e) {
      throw new ErrorException("Unable to call method [" + method + "] because the incorrect parameters were passed to it", e);
    } catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      if (target instanceof Error) {
        throw (Error) target;
      }

      throw new ErrorException("Unable to call method [" + method + "]", e);
    }
  }
}
