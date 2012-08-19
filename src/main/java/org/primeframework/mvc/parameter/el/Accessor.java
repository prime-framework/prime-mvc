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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.primeframework.mvc.parameter.convert.AnnotationConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.convert.annotation.ConverterAnnotation;

/**
 * This class provides the base accessor support.
 *
 * @author Brian Pontarelli
 */
public abstract class Accessor {
  protected final ConverterProvider converterProvider;
  protected Type type;
  protected Class<?> declaringClass;
  protected Object object;

  protected Accessor(ConverterProvider converterProvider) {
    this.converterProvider = converterProvider;
  }

  public Accessor(ConverterProvider converterProvider, Accessor accessor) {
    this.converterProvider = converterProvider;
    this.type = accessor.type;
    this.declaringClass = accessor.declaringClass;
  }

  public abstract boolean isIndexed();

  protected abstract Object get(Expression expression);

  protected abstract void set(String[] values, Expression expression);

  protected abstract void set(Object value, Expression expression);

  protected abstract <T extends Annotation> T getAnnotation(Class<T> type);

  public final Object get(Object object, Expression expression) {
    this.object = object;
    return get(expression);
  }

  public final void set(Object object, String[] values, Expression expression) {
    this.object = object;
    set(values, expression);
  }

  public final void set(Object object, Object value, Expression expression) {
    this.object = object;
    set(value, expression);
  }

  /**
   * After the object is originally get or set, this method can be called to update the value. This method should only
   * work if the {@link #set(Object, String[], Expression)} or {@link #get(Object, Expression)} method was called
   * first.
   * <p/>
   * <strong>NOTE:</strong> Accessors are not thread safe and need not be because a new one is created for each atom.
   *
   * @param value      The value to update the accessor with.
   * @param expression The current expression.
   */
  public void update(Object value, Expression expression) {
    if (object == null) {
      throw new UpdateExpressionException("The object is null, unable to update.");
    }

    set(object, value, expression);
  }

  /**
   * @return Returns the member accessor that is closest to the current atom in the expression. If the current atom is a
   *         member, this should just return <strong>this</strong>. If the current atom is a collection for example,
   *         this would return the member that the collection was retrieved from.
   */
  public abstract MemberAccessor getMemberAccessor();

  /**
   * Creates a new instance of the current type.
   *
   * @param key This is only used when creating arrays. It is the next atom, which is always the size of the array.
   * @return The new value.
   */
  protected Object createValue(Object key) {
    Class<?> typeClass = TypeTools.rawType(type);
    Object value;
    if (Map.class == typeClass) {
      value = new HashMap();
    } else if (List.class == typeClass) {
      value = new ArrayList();
    } else if (Set.class == typeClass) {
      value = new HashSet();
    } else if (Queue.class == typeClass) {
      value = new LinkedList();
    } else if (Deque.class == typeClass) {
      value = new ArrayDeque();
    } else if (SortedSet.class == typeClass) {
      value = new TreeSet();
    } else if (typeClass.isArray()) {
      if (key == null) {
        throw new UpdateExpressionException("Attempting to create an array, but there isn't an index " +
          "available to determine the size of the array");
      }

      value = Array.newInstance(typeClass.getComponentType(), Integer.parseInt(key.toString()) + 1);
    } else {
      try {
        value = typeClass.newInstance();
      } catch (Exception e) {
        throw new UpdateExpressionException("Unable to instantiate object [" + typeClass.getName() + "]");
      }
    }

    return value;
  }

  /**
   * Converts the given value parameter (parameter) to a type that is accepted by the set method of this property. This
   * method attempts to convert the value regardless of the value being null. However, this method short circuits and
   * returns the value unchanged if value is runtime assignable to the type of this BaseBeanProperty.
   *
   * @param expression       The current expression.
   * @param accessibleObject The field or method that the conversion is occurring for. This is used to look for
   *                         conversion annotations.
   * @param values           The String values to convert.
   * @return The value parameter converted to the correct type.
   * @throws ConversionException If there was a problem converting the parameter.
   */
  @SuppressWarnings("unchecked")
  protected Object convert(Expression expression, AccessibleObject accessibleObject, final String... values) throws ConversionException {
    Object newValue = values;

    // First look for annotations
    if (accessibleObject != null) {
      Annotation[] annotations = accessibleObject.getAnnotations();
      for (Annotation annotation : annotations) {
        ConverterAnnotation converterAnnotation = annotation.annotationType().getAnnotation(ConverterAnnotation.class);
        if (converterAnnotation != null) {
          AnnotationConverter converter = converterProvider.lookup(annotation);
          return converter.convertFromStrings(annotation, values, type, expression.getAttributes(), expression.getExpression());
        }
      }
    }

    // The converter does this, but pre-emptively checking these conditions will speed up conversion times
    Class<?> typeClass = TypeTools.rawType(type);
    if (!typeClass.isInstance(values)) {
      GlobalConverter converter = converterProvider.lookup(typeClass);
      if (converter == null) {
        throw new ConverterStateException("No type converter found for the type [" + typeClass.getName() + "]");
      }

      newValue = converter.convertFromStrings(type, expression.getAttributes(), expression.getExpression(), values);
    }

    return newValue;
  }

  /**
   * Gets a value from a collection using the index. This supports Arrays, Lists and Collections.
   *
   * @param index The index.
   * @return The value or null if the index is out of bounds.
   */
  protected Object getValueFromCollection(int index) {
    if (this.object.getClass().isArray()) {
      if (Array.getLength(this.object) <= index) {
        return null;
      }

      return Array.get(this.object, index);
    } else if (this.object instanceof List) {
      List l = (List) this.object;
      if (l.size() <= index) {
        return null;
      }

      return l.get(index);
    } else {
      Iterator iter = ((Collection) this.object).iterator();
      Object value = null;
      for (int i = 0; i < index; i++) {
        if (iter.hasNext()) {
          value = iter.next();
        } else {
          return null;
        }
      }

      return value;
    }
  }

  /**
   * Sets the given value into the collection at the given index.
   *
   * @param index The index.
   * @param value The value.
   */
  @SuppressWarnings("unchecked")
  protected void setValueIntoCollection(int index, Object value) {
    if (this.object.getClass().isArray()) {
      Array.set(this.object, index, value);
    } else if (this.object instanceof List) {
      List l = (List) this.object;
      l.set(index, value);
    } else {
      throw new UpdateExpressionException("You can only set values into arrays and Lists. You are setting a parameter into [" +
        getMemberAccessor() + "] which is of type [" + this.object.getClass() + "]");
    }
  }

  public String toString() {
    return "declaring class [" + declaringClass + "]";
  }
}
