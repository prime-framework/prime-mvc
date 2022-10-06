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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.util.TypeTools;

/**
 * This class models a collection accessor during expression evaluation.
 *
 * @author Brian Pontarelli
 */
public class IndexedCollectionAccessor extends Accessor {
  Integer index;

  MemberAccessor memberAccessor;

  public IndexedCollectionAccessor(ConverterProvider converterProvider, Accessor accessor, Integer index,
                                   MemberAccessor memberAccessor) {
    super(converterProvider, accessor);
    this.index = index;
    this.type = TypeTools.componentType(this.type, memberAccessor.toString());
    if (this.type instanceof TypeVariable<?>) {
      this.type = TypeTools.resolveGenericType(memberAccessor.declaringClass, this.currentClass, (TypeVariable<?>) this.type);
    }

    this.memberAccessor = memberAccessor;
  }

  public Object get(Expression expression) {
    return getValueFromCollection(index);
  }

  /**
   * @return The memberAccessor member variable.
   */
  public MemberAccessor getMemberAccessor() {
    return memberAccessor;
  }

  /**
   * @return Always false. The reason is that since this retrieves from a Collection, we want it to look like a
   *     non-indexed property so that the context will invoke the method.
   */
  public boolean isIndexed() {
    return false;
  }

  public void set(String[] values, Expression expression) {
    set(convert(expression, memberAccessor.field, values), expression);
  }

  public void set(Object value, Expression expression) {
    if (value instanceof String[] && ((String[]) value).length == 1) {
      expression.setCurrentValue(((String[]) value)[0]);
    } else {
      object = pad(object, expression);
      setValueIntoCollection(index, value);
    }
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

  /**
   * Adds padding to the array or list so that it can hold the item being inserted.
   *
   * @param object     The object to pad. If this isn't a List or an array, this method does nothing and just returns
   *                   the Object.
   * @param expression The current expression.
   * @return The padded list or array.
   */
  @SuppressWarnings("unchecked")
  private Object pad(Object object, Expression expression) {
    if (object instanceof List list) {
      int length = list.size();
      if (length <= index) {
        for (int i = length; i <= index; i++) {
          list.add(null);
        }
      }
    } else if (object.getClass().isArray()) {
      int length = Array.getLength(object);
      if (length <= index) {
        Object newArray = Array.newInstance(object.getClass().getComponentType(), index + 1);
        System.arraycopy(object, 0, newArray, 0, length);
        object = newArray;

        // Set the new array into the member
        memberAccessor.update(newArray, expression);
      }
    }

    return object;
  }
}
