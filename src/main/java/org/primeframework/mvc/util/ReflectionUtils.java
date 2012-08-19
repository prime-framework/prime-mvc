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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

import net.sf.cglib.reflect.FastMethod;

/**
 * Provides support for reflection, bean properties and field access.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ReflectionUtils {
  private static final Map<String, MethodVerifier> verifiers = new HashMap<String, MethodVerifier>();
  private static final Map<Class, Method[]> methods = new WeakHashMap<Class, Method[]>();
  private static final Map<Class, Map<String, PropertyInfo>> propertyCache = new WeakHashMap<Class, Map<String, PropertyInfo>>();
  private static final Map<Class, Map<String, Field>> fieldCache = new WeakHashMap<Class, Map<String, Field>>();

  static {
    verifiers.put("is", new GetMethodVerifier());
    verifiers.put("get", new GetMethodVerifier());
    verifiers.put("set", new SetMethodVerifier());
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
   * Invokes the given method on the given class and handles propagation of runtime exceptions.
   *
   * @param method The method to invoke.
   * @param obj    The object to invoke the methods on.
   * @param params The parameters passed to the method.
   * @return The return from the method invocation.
   */
  public static <T> T invoke(FastMethod method, Object obj, Object... params) {
    try {
      return (T) method.invoke(obj, params);
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
   * Invokes all the given methods on the given object.
   *
   * @param obj     The object to invoke the methods on.
   * @param methods The methods to invoke.
   */
  public static void invokeAll(Object obj, List<Method> methods) {
    for (Method method : methods) {
      try {
        method.invoke(obj);
      } catch (IllegalAccessException e) {
        throw new ExpressionException("Unable to call method [" + method + "]", e);
      } catch (InvocationTargetException e) {
        Throwable target = e.getTargetException();
        if (target instanceof RuntimeException) {
          throw (RuntimeException) target;
        }

        throw new ExpressionException("Unable to call method [" + method + "]", e);
      }

    }
  }

  /**
   * Pulls all of the fields and java bean properties from the given Class and returns the names.
   *
   * @param type The Class to pull the names from.
   * @return The names of all the fields and java bean properties.
   */
  public static Set<String> findAllMembers(Class<?> type) {
    Map<String, Field> fields = findFields(type);
    Map<String, PropertyInfo> map = findPropertyInfo(type);

    // Favor properties by adding fields first
    Set<String> names = new HashSet<String>(fields.keySet());
    names.addAll(map.keySet());
    return names;
  }

  /**
   * Locates all of the members (fields and JavaBean properties) that have the given annotation and returns the name of
   * the member and the annotation itself.
   *
   * @param type       The class to find the member annotations from.
   * @param annotation The annotation type.
   * @param <T>        The annotation type.
   * @return A map of members to annotations.
   */
  public static <T extends Annotation> Map<String, T> findAllMembersWithAnnotation(Class<?> type, Class<T> annotation) {
    Map<String, T> annotations = new HashMap<String, T>();
    List<Field> fields = findAllFieldsWithAnnotation(type, annotation);
    for (Field field : fields) {
      annotations.put(field.getName(), field.getAnnotation(annotation));
    }

    Map<String, PropertyInfo> properties = findPropertyInfo(type);
    for (String property : properties.keySet()) {
      Map<String, Method> methods = properties.get(property).getMethods();
      for (Method method : methods.values()) {
        if (method.isAnnotationPresent(annotation)) {
          annotations.put(property, method.getAnnotation(annotation));
          break;
        }
      }
    }

    return annotations;
  }

  /**
   * Finds all of the methods that have the given annotation on the given Object.
   *
   * @param type       The class to find methods from.
   * @param annotation The annotation.
   */
  public static List<Method> findAllMethodsWithAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
    Method[] methods = findMethods(type);
    List<Method> methodList = new ArrayList<Method>();
    for (Method method : methods) {
      if (method.isAnnotationPresent(annotation)) {
        methodList.add(method);
      }
    }
    return methodList;
  }

  /**
   * Finds all of the fields that have the given annotation on the given Object.
   *
   * @param type       The class to find fields from.
   * @param annotation The annotation.
   */
  public static List<Field> findAllFieldsWithAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
    Map<String, Field> fields = findFields(type);
    List<Field> fieldList = new ArrayList<Field>();
    for (Field field : fields.values()) {
      if (field.isAnnotationPresent(annotation)) {
        fieldList.add(field);
      }
    }
    return fieldList;
  }

  /**
   * Loads and caches the methods of the given Class.
   *
   * @param type The class.
   * @return The methods.
   */
  public static Method[] findMethods(Class<?> type) {
    synchronized (methods) {
      Method[] methodArray = methods.get(type);
      if (methodArray == null) {
        methodArray = type.getMethods();
        methods.put(type, methodArray);
      }
      return methodArray;
    }
  }

  /**
   * Loads or fetches from the cache a Map of {@link PropertyInfo} objects keyed into the Map by the property name they
   * correspond to.
   *
   * @param type The class to grab the property map from.
   * @return The Map, which could be empty if the class has no properties.
   */
  public static Map<String, PropertyInfo> findPropertyInfo(Class<?> type) {
    Map<String, PropertyInfo> propMap;
    synchronized (propertyCache) {
      // Otherwise look for the property Map or create and store
      propMap = propertyCache.get(type);
      if (propMap != null) {
        return propMap;
      }

      propMap = new HashMap<String, PropertyInfo>();
      Set<String> errors = new HashSet<String>();
      Method[] methods = findMethods(type);
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
          info.setKlass(type);
          constructed = true;
        }

        // Unify get and is
        String prefix = name.getPrefix();
        if (prefix.equals("is")) {
          prefix = "get";
        }

        Method existingMethod = info.getMethods().get(prefix);
        if (existingMethod != null) {
          errors.add("Two or more [" + prefix + "] methods exist in the class [" + type + "] and Prime can't determine which to call");
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
        throw new BeanExpressionException("Invalid JavaBean class [" + type + "]. Errors are: \n" + errors);
      }

      propertyCache.put(type, Collections.unmodifiableMap(propMap));
    }

    return propMap;
  }

  /**
   * Loads or fetches from the cache a Map of {@link Field} objects keyed into the Map by the field name they correspond
   * to.
   *
   * @param type The class to grab the fields from.
   * @return The Map, which could be null if the class has no fields.
   */
  public static Map<String, Field> findFields(Class<?> type) {
    Map<String, Field> fieldMap;
    synchronized (fieldCache) {
      // Otherwise look for the property Map or create and store
      fieldMap = fieldCache.get(type);
      if (fieldMap != null) {
        return fieldMap;
      }

      fieldMap = new HashMap<String, Field>();
      Field[] fields = type.getFields();
      for (Field field : fields) {
        fieldMap.put(field.getName(), field);
      }

      fieldCache.put(type, Collections.unmodifiableMap(fieldMap));
    }

    return fieldMap;
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
