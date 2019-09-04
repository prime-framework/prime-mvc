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
package org.primeframework.mvc.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.primeframework.mvc.parameter.el.CollectionExpressionException;

/**
 * This is a toolkit that assists with generics.
 *
 * @author Brian Pontarelli
 */
public class TypeTools {
  /**
   * Determines the final component type. This continues to loop over Collections until it hits a non-parameterized
   * type.
   *
   * @param type The parametrized type.
   * @param path The path to the type, used in exception message.
   * @return The final component type.
   */
  public static Class<?> componentFinalType(Type type, String path) {
    while (!(type instanceof Class)) {
      type = componentType(type, path);
    }

    return (Class<?>) type;
  }

  /**
   * Determines the component type. Lists is the first type, Map is the second type, etc.
   *
   * @param type The parametrized type.
   * @param path The path to the type, used in exception message.
   * @return The component type.
   */
  public static Type componentType(Type type, String path) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Class<?> rawType = (Class<?>) parameterizedType.getRawType();
      if (Map.class.isAssignableFrom(rawType)) {
        return parameterizedType.getActualTypeArguments()[1];
      } else if (Collection.class.isAssignableFrom(rawType)) {
        return parameterizedType.getActualTypeArguments()[0];
      } else {
        throw new CollectionExpressionException("Unknown collection type [" + type + "]");
      }
    } else if (type instanceof GenericArrayType) {
      return ((GenericArrayType) type).getGenericComponentType();
    }

    Class<?> rawType = (Class<?>) type;
    if (Map.class == type || Collection.class == type) {
      throw new CollectionExpressionException("The method or member [" + path + "] returns a simple " +
          "Map or Collection. Unable to determine the type of the Map or Collection. " +
          "Please make this method generic so that the correct type can be determined.");
    } else if (rawType.isArray()) {
      return rawType.getComponentType();
    }

    return rawType;
  }

  /**
   * Returns true if the given type is a Parameterized type with a raw type of Map.
   *
   * @param t The type.
   * @return True or false.
   */
  public static boolean isGenericMap(Type t) {
    return t instanceof ParameterizedType && Map.class.isAssignableFrom((Class) ((ParameterizedType) t).getRawType());
  }

  /**
   * Determines the key type for a Map.
   *
   * @param type The parametrized type.
   * @param path The path to the type, used in exception message.
   * @return The key type.
   */
  public static Type[] mapTypes(Type type, String path) {
    if (type instanceof Class) {
      Class<?> c = (Class<?>) type;

      while (c != null && !isGenericMap(type)) {
        Type[] types = c.getGenericInterfaces();
        if (types != null && types.length > 0) {
          for (Type t : types) {
            if (isGenericMap(t)) {
              type = t;
              break;
            }
          }
        }

        // Go up to the next parent and check
        if (!isGenericMap(type)) {
          type = c.getGenericSuperclass();
          if (type instanceof Class) {
            c = (Class<?>) type;
          } else if (type instanceof ParameterizedType) {
            c = (Class) ((ParameterizedType) type).getRawType();
          } else {
            c = null;
          }
        }
      }
    }

    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Class<?> rawType = (Class<?>) parameterizedType.getRawType();
      if (Map.class.isAssignableFrom(rawType)) {
        return parameterizedType.getActualTypeArguments();
      }
    }

    throw new CollectionExpressionException("The method or member [" + path + "] returns a simple Map. Unable to determine the " +
        "types of the Map. Please make this method or member generic so that the correct type can be determined.");
  }

  /**
   * Determines the raw type of the type given.
   *
   * @param type The type.
   * @return The raw type.
   */
  public static Class<?> rawType(Type type) {
    if (type instanceof ParameterizedType) {
      type = ((ParameterizedType) type).getRawType();
    } else if (type instanceof GenericArrayType) {
      Class<?> componentType = (Class<?>) ((GenericArrayType) type).getGenericComponentType();
      type = Array.newInstance(componentType, 0).getClass();
    }

    return (Class<?>) type;
  }

  /**
   * Resolves the generic types of a field or method by matching up the generics in the class definition to the ones
   * used in the method/field. This also works for methods/fields that use other types that are generic. For example,
   * Map&lt;T, U> can be resolved.
   *
   * @param declaringClassGeneric The class where the generic method/field was defined that uses the generic.
   * @param currentClass          The current class that has completely defined all the generic information to satisfy
   *                              the method/field.
   * @param typeVariable          The generic type variable from the method/field.
   * @return The type of the generic method/field.
   */
  public static Type resolveGenericType(Class<?> declaringClassGeneric, Class<?> currentClass,
                                        final TypeVariable<?> typeVariable) {
    List<Class<?>> classes = new ArrayList<>();
    while (currentClass != declaringClassGeneric) {
      classes.add(currentClass);
      currentClass = currentClass.getSuperclass();
    }
    classes.add(declaringClassGeneric);

    // Reverse it to work from base class to child class
    Collections.reverse(classes);

    int position = -1;
    TypeVariable<?> currentTypeVariable = typeVariable;
    for (Class<?> klass : classes) {
      TypeVariable<?>[] genericTypes = klass.getTypeParameters();
      if (genericTypes == null) {
        break;
      }

      if (position != -1) {
        Type genericSuperclass = klass.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
          throw new IllegalStateException("Something bad happened while trying to resolve generic types. The class [" + genericSuperclass +
              "] was encountered but didn't have generic types. It probably should have.");
        }

        Type[] inheritedGenericTypes = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        Type nextParameterType = inheritedGenericTypes[position];
        if (nextParameterType instanceof TypeVariable<?>) {
          currentTypeVariable = (TypeVariable<?>) nextParameterType;
        } else if (nextParameterType instanceof Class<?>) {
          return nextParameterType;
        }
      }

      String name = currentTypeVariable.getName();
      for (int i = 0; i < genericTypes.length; i++) {
        if (genericTypes[i].getName().equals(name)) {
          position = i;
          currentTypeVariable = genericTypes[i];
          break;
        }
      }
    }

    return typeVariable;
  }
}
