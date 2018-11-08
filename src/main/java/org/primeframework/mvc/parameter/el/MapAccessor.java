/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.util.TypeTools;

/**
 * This class models a Map accessor during expression evaluation.
 *
 * @author Brian Pontarelli
 */
public class MapAccessor extends Accessor {
  final Object key;

  final MemberAccessor memberAccessor;

  public MapAccessor(ConverterProvider converterProvider, Accessor accessor, String index, MemberAccessor memberAccessor) {
    super(converterProvider, accessor);

    String path = memberAccessor.toString();
    Type objectType = this.type;
    this.memberAccessor = memberAccessor;

    Type[] types = TypeTools.mapTypes(objectType, path);

    Class<?> classDeclaringMap = memberAccessor.field != null ? memberAccessor.field.getDeclaringClass() : memberAccessor.propertyInfo.getDeclaringClass();
    Type valueType = types[1];
    if (valueType instanceof TypeVariable<?>) {
      this.type = TypeTools.resolveGenericType(classDeclaringMap, currentClass, (TypeVariable<?>) valueType);
    } else {
      this.type = types[1]; // Value type
    }

    Type keyType = types[0];
    if (keyType instanceof TypeVariable<?>) {
      keyType= TypeTools.resolveGenericType(classDeclaringMap, currentClass, (TypeVariable<?>) keyType);
    } else {
      keyType = TypeTools.rawType(keyType); // Key type
    }

    if (!(keyType instanceof Class<?>)) {
      throw new IllegalStateException("Unable to determine concrete type of Map key for [" + toString() + "]");
    }

    Class<?> keyClass = (Class<?>) keyType;
    GlobalConverter converter = converterProvider.lookup(keyClass);
    if (converter == null) {
      throw new ConversionException("No type converter is registered for the type [" + keyClass + "], which is the " +
          "type for the key of the map at [" + path + "]");
    }

    // Use null and empty string from the parsed expression as is.
    if (index == null || index.length() == 0) {
      this.key = index;
    } else {
      this.key = converter.convertFromStrings(keyClass, null, path, index);
    }
  }

  /**
   * @return The memberAccessor member variable.
   */
  public MemberAccessor getMemberAccessor() {
    return memberAccessor;
  }

  /**
   * @return Always false. The reason is that since this retrieves from a Collection, we want it to look like a
   * non-indexed property so that the context will invoke the method.
   */
  public boolean isIndexed() {
    return false;
  }

  public Object get(Expression expression) {
    return ((Map) this.object).get(key);
  }

  public void set(String[] values, Expression expression) {
    set(convert(expression, memberAccessor.field, values), expression);
  }

  @SuppressWarnings("unchecked")
  public void set(Object value, Expression expression) {
    ((Map) this.object).put(key, value);
  }

  /**
   * Returns the annotation of the member this collection belongs to.
   *
   * @param type The annotation type.
   * @return The annotation or null.
   */
  @Override
  protected <T extends Annotation> T getAnnotation(Class<T> type) {
    return memberAccessor.getAnnotation(type);
  }
}
