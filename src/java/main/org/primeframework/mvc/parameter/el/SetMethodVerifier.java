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

import net.java.lang.StringTools;

/**
 * This class verifies JavaBean standard setter methods. The forms of the methods are as follows:
 * <p/>
 * <h3>Indexed methods</h3>
 * <p/>
 * <h4>Store</h4>
 * <p/>
 * <pre>
 * public void setFoo(int index, Object obj)
 * public void setBool(int index, boolean bool)
 * </pre>
 * <h3>Normal methods</h3>
 * <p/>
 * <h4>Storage</h4>
 * <p/>
 * <pre>
 * public void setFoo(Object o)
 * </pre>
 *
 * @author Brian Pontarelli
 */
public class SetMethodVerifier implements MethodVerifier {
  /**
   * The string that starts standard Java bean update methods <tt>set</tt>
   */
  public static final String SET_STRING = "set";
  public static final int SET_LENGTH = SET_STRING.length();

  public String isValid(Method method, PropertyInfo info) {
    Method read = info.getMethods().get("get");
    Method write = info.getMethods().get("set");
    if (isValidSetter(method)) {
      if (info.isIndexed()) {
        return "Invalid property named [" + info.getName() + "]. It mixes indexed and " +
          "normal JavaBean methods.";
      }
    } else if (isValidIndexedSetter(method)) {
      if (!info.isIndexed() && read != null) {
        return "Invalid property named [" + info.getName() + "]. It mixes indexed and " +
          "normal JavaBean methods.";
      }
    } else {
      return "Invalid setter method for property named [" + info.getName() + "]";
    }

    if (read != null && write != null &&
      ((info.isIndexed() && read.getReturnType() != write.getParameterTypes()[1]) ||
        (!info.isIndexed() && read.getReturnType() != write.getParameterTypes()[0]))) {
      return "Invalid getter/setter pair for JavaBean property named [" + info.getName() +
        "] in class [" + method.getClass() + "]. The return type and parameter types must be " +
        "identical";
    }

    return null;
  }

  public Class determineType(Method method) {
    Class[] types = method.getParameterTypes();
    if (types.length == 1) {
      return types[0];
    }

    return types[1];
  }

  public Type determineGenericType(Method method) {
    Type[] types = method.getGenericParameterTypes();
    if (types.length == 1) {
      return types[0];
    }

    return types[1];
  }

  public boolean isIndexed(Method method) {
    return (method.getParameterTypes().length == 2);
  }

  /**
   * Check if the method is a proper java bean setter-property method. This means that it starts with set, has the form
   * setFoo or setFOO, takes a single parameter and returns void
   *
   * @param method The method to check.
   * @return True if valid, false otherwise.
   */
  public static boolean isValidSetter(Method method) {
    return (method.getParameterTypes().length == 1 && method.getReturnType() == Void.TYPE);
  }

  /**
   * Check if the method is a proper java bean indexed setter method. This means that it starts with set, has the form
   * setFoo or setFOO, takes a two parameters, an indices and a value and returns void.
   *
   * @param method The method to check.
   * @return True if valid, false otherwise.
   */
  public static boolean isValidIndexedSetter(Method method) {
    return (method.getParameterTypes().length == 2 && method.getReturnType() == Void.TYPE);
  }

  /**
   * Using the propertyName, returns the Java Bean standard setter method name. If the parameter String starts with
   * white space or only contains white space or is empty, the it is simply concatenated to the SET constant of this
   * class. It is the job of the calling code to make certain that the parameter is a properly formatted String if that
   * check is desired.
   *
   * @param propertyName The property name to make into the name of the setter method.
   * @return The name of the setter method or null if propertyName is empty or null.
   * @throws NullPointerException If the parameter is null.
   */
  public static String makeSetter(String propertyName) {
    return SET_STRING + StringTools.capitalize(propertyName);
  }
}