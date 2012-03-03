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

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.primeframework.mvc.parameter.convert.ConverterProvider;

import static net.java.lang.reflect.ReflectionTools.*;

/**
 * This class provides member access.
 *
 * @author Brian Pontarelli
 */
public class MemberAccessor extends Accessor {
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

  Field field;
  final PropertyInfo propertyInfo;

  public MemberAccessor(ConverterProvider converterProvider, MemberAccessor accessor) {
    super(converterProvider, accessor);
    this.field = accessor.field;
    this.propertyInfo = accessor.propertyInfo;
  }

  public MemberAccessor(ConverterProvider converterProvider, Class<?> declaringClass, String name) {
    super(converterProvider);
    Map<String, PropertyInfo> map = getPropMap(declaringClass);
    PropertyInfo bpi = map.get(name);
    if (bpi == null) {
      this.propertyInfo = null;
      this.field = findField(declaringClass, name);
    } else {
      try {
        this.field = declaringClass.getDeclaredField(name);
      } catch (NoSuchFieldException e) {
        // We did our best and now we have to bail on the field
        this.field = null;
      }

      this.propertyInfo = bpi;
    }

    if (this.field == null && this.propertyInfo == null) {
      throw new ExpressionException("Invalid property or field [" + name + "] for class [" + declaringClass + "]");
    }

    this.declaringClass = declaringClass;
    super.type = (bpi != null) ? bpi.getGenericType() : field.getGenericType();
  }

  public boolean isIndexed() {
    return propertyInfo != null && propertyInfo.isIndexed();
  }

  public Object get(Context context) {
    if (propertyInfo != null) {
      Method getter = propertyInfo.getMethods().get("get");
      if (getter == null) {
        throw new ExpressionException("Missing getter for property [" + propertyInfo.getName() +
          "] in class [" + declaringClass + "]");
      }
      return invokeGetter(getter, this.object);
    }

    return getField(field, this.object);
  }

  public void set(String[] values, Context context) {
    set(convert(context, field, values), context);
  }

  public void set(Object value, Context context) {
    if (propertyInfo != null) {
      Method setter = propertyInfo.getMethods().get("set");
      if (setter == null) {
        throw new ExpressionException("Missing setter for property [" + propertyInfo.getName() +
          "] in class [" + declaringClass + "]");
      }
      invokeSetter(setter, object, value);
    } else {
      setField(field, object, value);
    }
  }

  /**
   * This first checks for the annotation on the method and then the field. If this member is a field it doesn't check
   * for any getter or setter.
   *
   * @param type The annotation type.
   * @return The annotation or null.
   */
  @Override
  protected <T extends Annotation> T getAnnotation(Class<T> type) {
    if (propertyInfo != null) {
      Map<String, Method> methods = propertyInfo.getMethods();
      for (Method method : methods.values()) {
        if (method.isAnnotationPresent(type)) {
          return method.getAnnotation(type);
        }
      }

      // Get the field for the property
      String name = propertyInfo.getName();
      try {
        Field field = declaringClass.getField(name);
        if (field.isAnnotationPresent(type)) {
          return field.getAnnotation(type);
        }
      } catch (NoSuchFieldException nsfe) {
        // Smother
      }
    }

    if (field != null && field.isAnnotationPresent(type)) {
      return field.getAnnotation(type);
    }

    return null;
  }

  /**
   * @return Returns this.
   */
  public MemberAccessor getMemberAccessor() {
    return this;
  }

  public String toString() {
    return (propertyInfo != null) ? propertyInfo.toString() : "Field [" + field.toString() + "] in class [" +
      field.getDeclaringClass() + "]";
  }

  /**
   * Loads or fetches from the cache a Map of {@link PropertyInfo} objects keyed into the Map by the property name they
   * correspond to.
   *
   * @param beanClass The bean class to grab the property map from.
   * @return The Map or null if there were no properties.
   */
  private static Map<String, PropertyInfo> getPropMap(Class<?> beanClass) {
    Map<String, PropertyInfo> propMap;
    synchronized (cache) {
      // Otherwise look for the property Map or create and store
      propMap = cache.get(beanClass);
      if (propMap == null) {
        propMap = new HashMap<String, PropertyInfo>();
      } else {
        return propMap;
      }

      boolean errorMethods = false;
      Method[] methods = beanClass.getMethods();
      for (Method method : methods) {
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
          info.getMethods().put(prefix, ERROR);
          errorMethods = true;
          continue;
        }

        MethodVerifier verifier = verifiers.get(prefix);
        if (verifier != null) {
          String error = verifier.isValid(method, info);
          if (error != null) {
            continue;
          }
        } else {
          continue;
        }

        info.getMethods().put(prefix, method);
        info.setType(verifier.determineType(method));
        info.setGenericType(verifier.determineGenericType(method));
        info.setIndexed(verifier.isIndexed(method));

        if (constructed) {
          propMap.put(name.getName(), info);
        }
      }

      if (errorMethods) {
        Set keys = propMap.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
          String s = (String) i.next();
          PropertyInfo info = propMap.get(s);
          Set entries = info.getMethods().entrySet();
          for (Iterator i2 = entries.iterator(); i2.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i2.next();
            if (entry.getValue() == ERROR) {
              i2.remove();
            }
          }

          if (info.getMethods().size() == 0) {
            i.remove();
          }
        }
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

  /**
   * This handles invoking the getter method.
   *
   * @param method The method to invoke.
   * @param object The object to invoke the method on.
   * @return The return value of the method.
   * @throws ExpressionException If any mishap occurred whilst Reflecting sire. All the exceptions that could be thrown
   *                             whilst invoking will be wrapped inside the ReflectionException.
   * @throws RuntimeException    If the target of the InvocationTargetException is a RuntimeException, in which case, it
   *                             is re-thrown.
   * @throws Error               If the target of the InvocationTargetException is an Error, in which case, it is
   *                             re-thrown.
   */
  public static Object invokeGetter(Method method, Object object)
    throws ExpressionException, RuntimeException, Error {
    try {
      // I think we have a winner
      return method.invoke(object);
    } catch (IllegalAccessException iae) {
      throw new ExpressionException("Illegal access for method [" + method + "]", iae);
    } catch (IllegalArgumentException iare) {
      throw new ExpressionException("Illegal argument for method [" + method + "]", iare);
    } catch (InvocationTargetException ite) {

      // Check if the target is a runtime or error and re-throw it
      Throwable target = ite.getTargetException();

      if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }

      if (target instanceof Error) {
        throw (Error) target;
      }

      throw new ExpressionException("Method [" + method + "] threw an exception [" + target.toString() + "]", target);
    }
  }

  /**
   * This handles invoking the setter method and also will handle a single special case where the setter method takes a
   * single object and the value is a collection with a single value.
   *
   * @param method The method to invoke.
   * @param object The object to invoke the method on.
   * @param value  The value to set into the method.
   * @throws ExpressionException If any mishap occurred whilst Reflecting sire. All the exceptions that could be thrown
   *                             whilst invoking will be wrapped inside the ReflectionException.
   * @throws RuntimeException    If the target of the InvocationTargetException is a RuntimeException, in which case, it
   *                             is re-thrown.
   * @throws Error               If the target of the InvocationTargetException is an Error, in which case, it is
   *                             re-thrown.
   */
  public static void invokeSetter(Method method, Object object, Object value)
    throws ExpressionException, RuntimeException, Error {
    Class[] types = method.getParameterTypes();
    if (types.length != 1) {
      throw new ExpressionException("Invalid method [" + method + "] it should take a single parameter");
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

    try {
      // I think we have a winner
      method.invoke(object, value);
    } catch (IllegalAccessException iae) {
      throw new ExpressionException("Illegal access for method [" + method + "]", iae);
    } catch (IllegalArgumentException iare) {
      throw new ExpressionException("Illegal argument for method [" + method + "]", iare);
    } catch (InvocationTargetException ite) {

      // Check if the target is a runtime or error and re-throw it
      Throwable target = ite.getTargetException();

      if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }

      if (target instanceof Error) {
        throw (Error) target;
      }

      throw new ExpressionException("Method [" + method + "] threw an exception [" + target.toString() + "]", target);
    }
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
      throw new ExpressionException("Illegal access for field [" + field + "]", iae);
    } catch (IllegalArgumentException iare) {
      throw new ExpressionException("Illegal agrument for field [" + field + "]", iare);
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
        throw new ExpressionException("Cannot set a Collection that contains multiple values into the field [" +
          field + "] which is not a collection.");
      }
    }

    try {
      // I think we have a winner
      field.set(object, value);
    } catch (IllegalAccessException iae) {
      throw new ExpressionException("Illegal access for field [" + field + "]", iae);
    } catch (IllegalArgumentException iare) {
      throw new ExpressionException("Illegal agrument for field [" + field + "]", iare);
    }
  }
}
