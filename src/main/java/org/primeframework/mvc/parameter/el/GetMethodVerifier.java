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
package org.primeframework.mvc.parameter.el;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * This class verifies JavaBean standard getter methods. The forms of the methods are as follows:
 * <p/>
 * <h3>Indexed methods</h3>
 * <p/>
 * <h4>Retrieval</h4>
 * <p/>
 * <pre>
 * public Object getFoo(int index)
 * public boolean isFoo(int index)
 * </pre>
 * <p/>
 * <h3>Normal methods</h3>
 * <p/>
 * <h4>Retrieval</h4>
 * <p/>
 * <pre>
 * public Object getFoo()
 * public boolean isFoo()
 * </pre>
 * <p/>
 * All <b>is</b> methods must have a return type of boolean regardless of being indexed or not.
 *
 * @author Brian Pontarelli
 */
public class GetMethodVerifier implements MethodVerifier {
  /**
   * Verifies that the method is either a get, set or is method. It must follow the standard JavaBean convention. If the
   * method is indexed it can have an int parameter. See the class comment for all of the forms of the methods.
   * <p/>
   * This method not only checks the method given, but it also checks that the property is correctly defined with
   * respect to the other methods. For example a read/write property must have both a get/is and a set method and the
   * methods of each must both be indexed or not.
   *
   * @param method The method to verify.
   * @param info   Used to determine if the property is indexed or not and verify the methods are correct.
   * @return Null if the method if correct, false otherwise.
   */
  public String isValid(Method method, PropertyInfo info) {
    Method write = info.getMethods().get("set");
    if (isValidGetter(method)) {
      if (info.isIndexed()) {
        return "Invalid property named [" + info.getName() + "]. It mixes indexed and " +
          "normal JavaBean methods.";
      }
    } else if (isValidIndexedGetter(method)) {
      if (!info.isIndexed() && write != null) {
        return "Invalid property named [" + info.getName() + "]. It mixes indexed and " +
          "normal JavaBean methods.";
      }
    } else {
      return "Invalid getter method for property named [" + info.getName() + "]";
    }

    Method read = info.getMethods().get("get");
    if (read != null && write != null &&
      ((info.isIndexed() && read.getReturnType() != write.getParameterTypes()[1]) ||
        (!info.isIndexed() && read.getReturnType() != write.getParameterTypes()[0]))) {
      return "Invalid getter/setter pair for JavaBean property named [" + info.getName() +
        "] in class [" + method.getClass() + "]. The return type and parameter types must be " +
        "identical";
    }

    return null;
  }

  /**
   * Returns the return type of the method.
   */
  public Class determineType(Method method) {
    return method.getReturnType();
  }

  /**
   * @param method The method to get the generic type from.
   * @return Returns the return type of the method.
   */
  public Type determineGenericType(Method method) {
    return method.getGenericReturnType();
  }

  public boolean isIndexed(Method method) {
    return isValidIndexedGetter(method);
  }

  /**
   * Check if the method is a proper java bean getter-property method. This means that it starts with get, has the form
   * getFoo or getFOO, has no parameters and returns a non-void value.
   *
   * @param method The method to check.
   * @return True if valid, false otherwise.
   */
  public static boolean isValidGetter(Method method) {
    return (method.getParameterTypes().length == 0 && method.getReturnType() != Void.TYPE);
  }

  /**
   * Check if the method is a proper java bean indexed getter method. This means that it starts with get, has the form
   * getFoo or getFOO, has one parameter, an indices, and returns a non-void value.
   *
   * @param method The method to check.
   * @return True if valid, false otherwise.
   */
  public static boolean isValidIndexedGetter(Method method) {
    return (method.getParameterTypes().length == 1 && method.getReturnType() != Void.TYPE);
  }
}