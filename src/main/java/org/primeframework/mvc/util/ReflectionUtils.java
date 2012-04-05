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

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.primeframework.mvc.parameter.el.BeanExpressionException;
import org.primeframework.mvc.parameter.el.CollectionExpressionException;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.el.GetMethodVerifier;
import org.primeframework.mvc.parameter.el.MethodVerifier;
import org.primeframework.mvc.parameter.el.PropertyInfo;
import org.primeframework.mvc.parameter.el.PropertyName;
import org.primeframework.mvc.parameter.el.ReadExpressionException;
import org.primeframework.mvc.parameter.el.SetMethodVerifier;
import org.primeframework.mvc.parameter.el.UpdateExpressionException;

/**
 * Provides support for reflection, bean properties and field access.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ReflectionUtils {
  private static final Map<String, MethodVerifier> verifiers = new HashMap<String, MethodVerifier>();
  private static final Map<Class, Map<String, PropertyInfo>> cache = new WeakHashMap<Class, Map<String, PropertyInfo>>();
  private static final Method ERROR;

  static {
    try {
      ERROR = Object.class.getMethod("hashCode");
    } catch (NoSuchMethodException e) {
      throw new AssertionError("Bad, bad!");
    }

    verifiers.put("is", new GetMethodVerifier());
    verifiers.put("get", new GetMethodVerifier());
    verifiers.put("set", new SetMethodVerifier());
  }

  /**
   * Pulls all of the fields and java bean properties from the given Class and returns the names.
   *
   * @param type The Class to pull the names from.
   * @return The names of all the fields and java bean properties.
   */
  public static Set<String> getAllMembers(Class<?> type) {
    Field[] fields = type.getFields();
    Map<String, PropertyInfo> map = getPropMap(type);

    // Favor properties
    Set<String> names = new HashSet<String>();
    for (Field field : fields) {
      names.add(field.getName());
    }

    names.addAll(map.keySet());
    return names;
  }

  /**
   * This handles invoking the getter method.
   *
   * @param method The method to invoke.
   * @param object The object to invoke the method on.
   * @return The return value of the method.
   * @throws RuntimeException If the target of the InvocationTargetException is a RuntimeException, in which case, it is
   *                          re-thrown.
   * @throws Error            If the target of the InvocationTargetException is an Error, in which case, it is
   *                          re-thrown.
   */
  public static Object invokeGetter(Method method, Object object) throws RuntimeException, Error {
    return invoke(method, object);
  }

  /**
   * This handles invoking the setter method and also will handle a single special case where the setter method takes a
   * single object and the value is a collection with a single value.
   *
   * @param method The method to invoke.
   * @param object The object to invoke the method on.
   * @param value  The value to set into the method.
   * @throws RuntimeException If the target of the InvocationTargetException is a RuntimeException, in which case, it is
   *                          re-thrown.
   * @throws Error            If the target of the InvocationTargetException is an Error, in which case, it is
   *                          re-thrown.
   */
  public static void invokeSetter(Method method, Object object, Object value) throws RuntimeException, Error {
    Class[] types = method.getParameterTypes();
    if (types.length != 1) {
      throw new UpdateExpressionException("Invalid method [" + method + "] it should take a single parameter");
    }

    Class type = types[0];
    if (!type.isInstance(value) && Collection.class.isInstance(value)) {
      // Handle the Collection special case
      Collection c = (Collection) value;
      if (c.size() == 1) {
        value = c.iterator().next();
      } else {
        throw new ExpressionException("Cannot set a Collection that contains multiple values into the method [" +
          method + "] which is not a collection.");
      }
    }

    invoke(method, object, value);
  }

  /**
   * This handles fetching a field value.
   *
   * @param field  The field to get.
   * @param object The object to get he field from.
   * @return The value of the field.
   * @throws ExpressionException If any mishap occurred whilst Reflecting sire. All the exceptions that could be thrown
   *                             whilst invoking will be wrapped inside the ReflectionException.
   */
  public static Object getField(Field field, Object object) throws ExpressionException {
    try {
      // I think we have a winner
      return field.get(object);
    } catch (IllegalAccessException iae) {
      throw new ReadExpressionException("Illegal access for field [" + field + "]", iae);
    } catch (IllegalArgumentException iare) {
      throw new ReadExpressionException("Illegal argument for field [" + field + "]", iare);
    }
  }

  /**
   * This handles setting a value on a field and also will handle a single special case where the setter method takes a
   * single object and the value is a collection with a single value.
   *
   * @param field  The field to set.
   * @param object The object to set the field on.
   * @param value  The value to set into the field.
   * @throws ExpressionException If any mishap occurred whilst Reflecting sire. All the exceptions that could be thrown
   *                             whilst invoking will be wrapped inside the ReflectionException.
   */
  public static void setField(Field field, Object object, Object value) throws ExpressionException {
    Class type = field.getType();
    if (!type.isInstance(value) && Collection.class.isInstance(value)) {
      // Handle the Collection special case
      Collection c = (Collection) value;
      if (c.size() == 1) {
        value = c.iterator().next();
      } else {
        throw new CollectionExpressionException("Cannot set a Collection that contains multiple values into the field [" +
          field + "] which is not a collection.");
      }
    }

    try {
      // I think we have a winner
      field.set(object, value);
    } catch (IllegalAccessException iae) {
      throw new UpdateExpressionException("Illegal access for field [" + field + "]", iae);
    } catch (IllegalArgumentException iare) {
      throw new UpdateExpressionException("Illegal argument for field [" + field + "]", iare);
    }
  }

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
          throw new ExpressionException("Unable to call method [" + method + "] with annotation [" + annotation.getSimpleName() + "]", e);
        } catch (InvocationTargetException e) {
          Throwable target = e.getTargetException();
          if (target instanceof RuntimeException) {
            throw (RuntimeException) target;
          }

          throw new ExpressionException("Unable to call method [" + method + "] with annotation [" + annotation.getSimpleName() + "]", e);
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
  public static <T> T invoke(Method method, Object obj, Object... params) {
    try {
      return (T) method.invoke(obj, params);
    } catch (IllegalAccessException e) {
      throw new ExpressionException("Unable to call method [" + method + "] because it isn't accessible", e);
    } catch (IllegalArgumentException e) {
      throw new ExpressionException("Unable to call method [" + method + "] because the incorrect parameters were passed to it", e);
    } catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      if (target instanceof Error) {
        throw (Error) target;
      }

      throw new ExpressionException("Unable to call method [" + method + "]", e);
    }
  }

  /**
   * Loads or fetches from the cache a Map of {@link PropertyInfo} objects keyed into the Map by the property name they
   * correspond to.
   *
   * @param beanClass The bean class to grab the property map from.
   * @return The Map or null if there were no properties.
   */
  public static Map<String, PropertyInfo> getPropMap(Class<?> beanClass) {
    Map<String, PropertyInfo> propMap;
    synchronized (cache) {
      // Otherwise look for the property Map or create and store
      propMap = cache.get(beanClass);
      if (propMap == null) {
        propMap = new HashMap<String, PropertyInfo>();
      } else {
        return propMap;
      }

      Set<String> errors = new HashSet<String>();
      Method[] methods = beanClass.getMethods();
      for (Method method : methods) {
        // Skip bridge methods (covariant or generics) because the non-bridge method is the one that should be correct
        if (method.isBridge()) {
          continue;
        }

        PropertyName name = getPropertyNames(method);
        if (name == null) {
          continue;
        }

        PropertyInfo info = propMap.get(name.getName());
        boolean constructed = false;
        if (info == null) {
          info = new PropertyInfo();
          info.setName(name.getName());
          info.setKlass(beanClass);
          constructed = true;
        }

        // Unify get and is
        String prefix = name.getPrefix();
        if (prefix.equals("is")) {
          prefix = "get";
        }

        Method existingMethod = info.getMethods().get(prefix);
        if (existingMethod != null) {
          errors.add("Two or more [" + prefix + "] methods exist in the class [" + beanClass + "] and Prime can't determine which to call");
          continue;
        }

        MethodVerifier verifier = verifiers.get(prefix);
        if (verifier != null) {
          String error = verifier.isValid(method, info);
          if (error != null) {
            errors.add(error);
            continue;
          }
        } else {
          continue;
        }

        info.getMethods().put(prefix, method);
        info.setGenericType(verifier.determineGenericType(method));
        info.setIndexed(verifier.isIndexed(method));

        if (constructed) {
          propMap.put(name.getName(), info);
        }
      }

      if (errors.size() > 0) {
        throw new BeanExpressionException("Invalid JavaBean class [" + beanClass + "]. Errors are: \n" + errors);
      }

      cache.put(beanClass, Collections.unmodifiableMap(propMap));
    }

    return propMap;
  }

  /**
   * Using the given Method, it returns the name of the java bean property and the prefix of the method.
   * <p/>
   * <h3>Examples:</h3>
   * <p/>
   * <pre>
   * getFoo -> get, foo
   * getX -> get, x
   * getURL -> get, URL
   * handleBar -> handle, bar
   * </pre>
   *
   * @param method The method to translate.
   * @return The property names or null if this was not a valid property Method.
   */
  private static PropertyName getPropertyNames(Method method) {
    String name = method.getName();
    char[] ca = name.toCharArray();
    int startIndex = -1;
    for (int i = 0; i < ca.length; i++) {
      char c = ca[i];
      if (Character.isUpperCase(c) && i == 0) {
        break;
      } else if (Character.isUpperCase(c)) {
        startIndex = i;
        break;
      }
    }

    if (startIndex == -1) {
      return null;
    }

    String propertyName = Introspector.decapitalize(name.substring(startIndex));
    String prefix = name.substring(0, startIndex);
    return new PropertyName(prefix, propertyName);
  }
}
