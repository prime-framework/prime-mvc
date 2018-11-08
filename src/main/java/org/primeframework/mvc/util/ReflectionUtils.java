/*
 * Copyright (c) 2012-2017, Inversoft Inc., All Rights Reserved
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.primeframework.mvc.parameter.el.BeanExpressionException;
import org.primeframework.mvc.parameter.el.CollectionExpressionException;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.el.ReadExpressionException;
import org.primeframework.mvc.parameter.el.UpdateExpressionException;

/**
 * Provides support for reflection, bean properties and field access.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ReflectionUtils {
  private static final Map<Class<?>, Map<String, Field>> fieldCache = new WeakHashMap<>();

  private static final Map<String, Package> packageCache = new WeakHashMap<>();

  private static final Map<Class<?>, Method[]> methods = new WeakHashMap<>();

  private static final Map<Class<?>, Map<String, PropertyInfo>> propertyCache = new WeakHashMap<>();

  private static final Map<String, MethodInformationExtractor> verifiers = new HashMap<>();

  static {
    verifiers.put("is", new GetMethodInformationExtractor());
    verifiers.put("get", new GetMethodInformationExtractor());
    verifiers.put("set", new SetMethodInformationExtractor());
  }

  /**
   * Return true if any of the provided annotations are provided on the field.
   *
   * @param field       The field
   * @param annotations a list of annotations to look for
   * @return true if any of the provided annotations are present.
   */
  public static boolean areAnyAnnotationsPresent(Field field, List<Class<? extends Annotation>> annotations) {
    for (Class<? extends Annotation> annotation : annotations) {
      if (field.isAnnotationPresent(annotation)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Finds all of the fields that have the given annotation on the given Object.
   *
   * @param type       The class to find fields from.
   * @param annotation The annotation.
   */
  public static List<Field> findAllFieldsWithAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
    Map<String, Field> fields = findFields(type);
    List<Field> fieldList = new ArrayList<>();
    for (Field field : fields.values()) {
      if (field.isAnnotationPresent(annotation)) {
        fieldList.add(field);
      }
    }
    return fieldList;
  }

  /**
   * Finds all of the fields that have the given annotation on the given Object.
   *
   * @param type        The class to find fields from.
   * @param annotations The annotations.
   */
  public static List<Field> findAllFieldsWithAnnotations(Class<?> type, List<Class<? extends Annotation>> annotations) {
    Map<String, Field> fields = findFields(type);
    List<Field> fieldList = new ArrayList<>();
    for (Field field : fields.values()) {
      for (Class<? extends Annotation> annotation : annotations) {
        if (field.isAnnotationPresent(annotation)) {
          fieldList.add(field);
          break;
        }
      }
    }
    return fieldList;
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
    Set<String> names = new HashSet<>(fields.keySet());
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
    Map<String, T> annotations = new HashMap<>();
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
    List<Method> methodList = new ArrayList<>();
    for (Method method : methods) {
      if (method.isAnnotationPresent(annotation)) {
        methodList.add(method);
      }
    }
    return methodList;
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

      fieldMap = new HashMap<>();
      Field[] fields = type.getFields();
      for (Field field : fields) {
        fieldMap.put(field.getName(), field);
      }

      fieldCache.put(type, Collections.unmodifiableMap(fieldMap));
    }

    return fieldMap;
  }

  /**
   * Find a package by name and return it if it has the requested annotation.
   *
   * @param packageName the string name of the package.
   * @param annotation  the annotation to find in that package.
   * @return the package if it exists and has the requested annotation or null.
   */
  public static Package findPackageWithAnnotation(String packageName, Class<? extends Annotation> annotation) {
    Package pkg;
    synchronized (packageCache) {
      pkg = packageCache.get(packageName);
      if (pkg == null) {
        pkg = Package.getPackage(packageName);
        if (pkg != null) {
          packageCache.put(packageName, pkg);
        }
      }
    }

    return pkg != null && pkg.isAnnotationPresent(annotation) ? pkg : null;
  }

  /**
   * Loads and caches the methods of the given Class in an order array. The order of this array is that methods defined
   * in superclasses are in the array first, followed by methods in the given type. The deeper the superclass, the
   * earlier the methods are in the array.
   *
   * @param type The class.
   * @return The methods.
   */
  public static Method[] findMethods(final Class<?> type) {
    synchronized (methods) {
      Method[] array = methods.get(type);
      if (array == null) {
        array = type.getMethods();
        methods.put(type, array);

        Arrays.sort(array, new Comparator<Method>() {
          @Override
          public int compare(Method method1, Method method2) {
            int depth1 = depth(method1, type);
            int depth2 = depth(method2, type);
            if (depth1 == depth2) {
              return method1.getName().compareTo(method2.getName());
            }

            return depth2 - depth1;
          }

          public int depth(Method method, Class<?> type) {
            int depth = 0;
            Class<?> declaringType = method.getDeclaringClass();
            while (declaringType != type && type != null) {
              type = type.getSuperclass();
              depth++;
            }
            return depth;
          }
        });
      }
      return array;
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

      propMap = new HashMap<>();
      Set<String> errors = new HashSet<>();
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
          info.setDeclaringClass(type);
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

        MethodInformationExtractor verifier = verifiers.get(prefix);
        if (verifier == null) {
          continue;
        }

        info.getMethods().put(prefix, method);
        info.setGenericType(verifier.determineGenericType(method));
        info.setType(verifier.determineType(method));
        info.setIndexed(verifier.isIndexed(method));

        if (constructed) {
          propMap.put(name.getName(), info);
        }
      }

      // Check for property errors
      for (PropertyInfo info : propMap.values()) {
        Method read = info.getMethods().get("get");
        Method write = info.getMethods().get("set");
        if (read != null && isValidGetter(read)) {
          if (info.isIndexed()) {
            errors.add("Invalid property named [" + info.getName() + "]. It mixes indexed and normal JavaBean methods.");
          }
        } else if (read != null && isValidIndexedGetter(read)) {
          if (!info.isIndexed() && write != null) {
            errors.add("Invalid property named [" + info.getName() + "]. It mixes indexed and normal JavaBean methods.");
          }
        } else if (read != null) {
          errors.add("Invalid getter method for property named [" + info.getName() + "]");
        }

        if (write != null && isValidSetter(write)) {
          if (info.isIndexed()) {
            errors.add("Invalid property named [" + info.getName() + "]. It mixes indexed and normal JavaBean methods.");
          }
        } else if (write != null && isValidIndexedSetter(write)) {
          if (!info.isIndexed() && read != null) {
            errors.add("Invalid property named [" + info.getName() + "]. It mixes indexed and normal JavaBean methods.");
          }
        } else if (write != null) {
          errors.add("Invalid setter method for property named [" + info.getName() + "]");
        }

        if (read != null && write != null &&
            ((info.isIndexed() && read.getReturnType() != write.getParameterTypes()[1]) ||
                (!info.isIndexed() && read.getReturnType() != write.getParameterTypes()[0]))) {
          errors.add("Invalid getter/setter pair for JavaBean property named [" + info.getName() + "] in class [" +
              write.getDeclaringClass() + "]. The return type and parameter types must be identical");
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
   * This handles fetching a field value.
   *
   * @param field  The field to get.
   * @param object The object to get he field from.
   * @return The value of the field.
   * @throws ExpressionException If any mishap occurred whilst Reflecting sire. All the exceptions that could be thrown
   * whilst invoking will be wrapped inside the ReflectionException.
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
   * Determines the type of the given member (field or proprty).
   *
   * @param type   The class.
   * @param member The member name.
   * @return The type.
   */
  public static Class<?> getMemberType(Class<?> type, String member) {
    Field field = findFields(type).get(member);
    if (field != null) {
      return field.getType();
    }

    PropertyInfo propertyInfo = findPropertyInfo(type).get(member);
    if (propertyInfo != null) {
      return propertyInfo.getType();
    }
    return null;
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
        throw new ExpressionException("Unable to call method [" + method + "] because it isn't accessible", e);
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
   * This handles invoking the getter method.
   *
   * @param method The method to invoke.
   * @param object The object to invoke the method on.
   * @return The return value of the method.
   * @throws RuntimeException If the target of the InvocationTargetException is a RuntimeException, in which case, it is
   * re-thrown.
   * @throws Error If the target of the InvocationTargetException is an Error, in which case, it is
   * re-thrown.
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
   * re-thrown.
   * @throws Error If the target of the InvocationTargetException is an Error, in which case, it is
   * re-thrown.
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

  /**
   * Check if the method is a proper java bean indexed setter method. This means that it starts with set, has the form
   * setFoo or setFOO, takes a two parameters, an indices and a value.
   *
   * @param method The method to check.
   * @return True if valid, false otherwise.
   */
  public static boolean isValidIndexedSetter(Method method) {
    return (method.getParameterTypes().length == 2);
  }

  /**
   * Check if the method is a proper java bean setter-property method. This means that it starts with set, has the form
   * setFoo or setFOO, takes a single parameter.
   *
   * @param method The method to check.
   * @return True if valid, false otherwise.
   */
  public static boolean isValidSetter(Method method) {
    return (method.getParameterTypes().length == 1);
  }

  /**
   * This handles setting a value on a field and also will handle a single special case where the setter method takes a
   * single object and the value is a collection with a single value.
   *
   * @param field  The field to set.
   * @param object The object to set the field on.
   * @param value  The value to set into the field.
   * @throws ExpressionException If any mishap occurred whilst Reflecting sire. All the exceptions that could be thrown
   * whilst invoking will be wrapped inside the ReflectionException.
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
   * This interface defines a mechanism to extract information from JavaBean properties.
   *
   * @author Brian Pontarelli
   */
  public interface MethodInformationExtractor {
    /**
     * Determines the generic type of the property.
     *
     * @param method The method to pull the generic type from.
     * @return The generic type.
     */
    Type determineGenericType(Method method);

    /**
     * Determines the type of the method. For getters this is the return type. For setters this is the parameter.
     *
     * @param method The method.
     */
    Class<?> determineType(Method method);

    /**
     * Whether or not this property is an indexed property.
     *
     * @param method The method to determine if it is indexed.
     * @return True if indexed or false otherwise.
     */
    boolean isIndexed(Method method);
  }

  /**
   * This class extracts information about JavaBean standard getter methods. The forms of the methods are as follows:
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
  public static class GetMethodInformationExtractor implements MethodInformationExtractor {
    /**
     * @param method The method to get the generic type from.
     * @return Returns the return type of the method.
     */
    @Override
    public Type determineGenericType(Method method) {
      return method.getGenericReturnType();
    }

    @Override
    public Class<?> determineType(Method method) {
      return method.getReturnType();
    }

    @Override
    public boolean isIndexed(Method method) {
      return isValidIndexedGetter(method);
    }
  }

  /**
   * This class is a small helper class that is used to store the read and write methods of a bean property as well as a
   * flag that determines if it is indexed.
   *
   * @author Brian Pontarelli
   */
  public static class PropertyInfo {
    private final Map<String, Method> methods = new HashMap<>();

    private Class<?> declaringClass;

    private Type genericType;

    private boolean indexed;

    private String name;

    private Class<?> type;

    public Type getGenericType() {
      return genericType;
    }

    public void setGenericType(Type genericType) {
      this.genericType = genericType;
    }

    public Map<String, Method> getMethods() {
      return methods;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Class<?> getType() {
      return type;
    }

    public void setType(Class<?> type) {
      this.type = type;
    }

    public boolean isIndexed() {
      return indexed;
    }

    public void setIndexed(boolean indexed) {
      this.indexed = indexed;
    }

    public Class<?> getDeclaringClass() {
      return declaringClass;
    }

    public void setDeclaringClass(Class<?> declaringClass) {
      this.declaringClass = declaringClass;
    }

    public String toString() {
      return "Property named [" + name + "] in class [" + declaringClass + "]";
    }
  }

  /**
   * This class stores the information about JavaBean methods including the prefix and propertyName.
   *
   * @author Brian Pontarelli
   */
  public static class PropertyName {
    private final String name;

    private final String prefix;

    public PropertyName(String prefix, String name) {
      this.prefix = prefix;
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public String getPrefix() {
      return prefix;
    }
  }

  /**
   * This class extracts information from JavaBean standard setter methods. The forms of the methods are as follows:
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
  public static class SetMethodInformationExtractor implements MethodInformationExtractor {
    @Override
    public Type determineGenericType(Method method) {
      Type[] types = method.getGenericParameterTypes();
      if (types.length == 1) {
        return types[0];
      }

      return types[1];
    }

    @Override
    public Class<?> determineType(Method method) {
      return isIndexed(method) ? method.getParameterTypes()[1] : method.getParameterTypes()[0];
    }

    @Override
    public boolean isIndexed(Method method) {
      return isValidIndexedSetter(method);
    }
  }
}
