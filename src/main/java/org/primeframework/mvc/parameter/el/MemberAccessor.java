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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * This class provides member access.
 *
 * @author Brian Pontarelli
 */
public class MemberAccessor extends Accessor {
  final Field field;

  final ReflectionUtils.PropertyInfo propertyInfo;

  private final List<Class<? extends Annotation>> unWrappedAnnotations;

  public MemberAccessor(ConverterProvider converterProvider, MemberAccessor accessor, MVCConfiguration configuration) {
    super(converterProvider, accessor);
    this.field = accessor.field;
    this.propertyInfo = accessor.propertyInfo;
    if (configuration != null) {
      this.unWrappedAnnotations = configuration.unwrapAnnotations();
    } else {
      this.unWrappedAnnotations = Collections.emptyList();
    }
  }

  public MemberAccessor(ConverterProvider converterProvider, Class<?> declaringClass, String name, String expression, MVCConfiguration configuration) {
    super(converterProvider);
    if (configuration != null) {
      this.unWrappedAnnotations = configuration.unwrapAnnotations();
    } else {
      this.unWrappedAnnotations = Collections.emptyList();
    }
    this.declaringClass = declaringClass;

    Map<String, ReflectionUtils.PropertyInfo> properties = ReflectionUtils.findPropertyInfo(this.declaringClass);
    ReflectionUtils.PropertyInfo bpi = properties.get(name);

    if (bpi == null) {
      Map<String, Field> fields = findFields();
      this.propertyInfo = null;
      this.field = fields.get(name);
    } else {
      this.propertyInfo = bpi;
      this.field = null;
    }

    if (this.field == null && this.propertyInfo == null) {
      throw new MissingPropertyExpressionException("While evaluating the expression [" + expression + "]. The property/field [" +
          name + "] does not exist in the class [" + declaringClass + "]", name, declaringClass, expression);
    }

    super.type = (bpi != null) ? bpi.getGenericType() : this.field.getGenericType();
  }

  public Object get(Expression expression) {
    if (propertyInfo != null) {
      Method getter = propertyInfo.getMethods().get("get");
      if (getter == null) {
        throw new ReadExpressionException("Missing getter for property [" + propertyInfo.getName() +
            "] in class [" + declaringClass + "]");
      }
      return ReflectionUtils.invokeGetter(getter, this.object);
    }

    return getField();
  }

  /**
   * @return Returns this.
   */
  public MemberAccessor getMemberAccessor() {
    return this;
  }

  public boolean isIndexed() {
    return propertyInfo != null && propertyInfo.isIndexed();
  }

  public void set(Object value, Expression expression) {
    if (propertyInfo != null) {
      Method setter = propertyInfo.getMethods().get("set");
      if (setter == null) {
        throw new UpdateExpressionException("Missing setter for property [" + propertyInfo.getName() +
            "] in class [" + declaringClass + "]");
      }
      ReflectionUtils.invokeSetter(setter, object, value);
    } else {
      setField(value, expression);
    }
  }

  public void set(String[] values, Expression expression) {
    set(convert(expression, field, values), expression);
  }

  @Override
  public String toString() {
    return (propertyInfo != null) ? propertyInfo.toString() : "Field [" + field.toString() + "] in class [" +
        field.getDeclaringClass() + "]";
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
      Field field = ReflectionUtils.findFields(declaringClass).get(name);
      if (field != null && field.isAnnotationPresent(type)) {
        return field.getAnnotation(type);
      }
    }

    if (field != null && field.isAnnotationPresent(type)) {
      return field.getAnnotation(type);
    }

    return null;
  }

  @Override
  protected Object newInstance(Object key, Class<?> clazz) throws IllegalAccessException, InstantiationException {
    Object object = clazz.newInstance();
    Set<String> fieldNames = ReflectionUtils.findFields(clazz).keySet();

    // If the object contains the field name matching the provided key we're done. This is the normal case.
    if (fieldNames.contains(key.toString())) {
      return object;
    } else {
      // Otherwise if there is a nested field to unwrap, let's do that until we find it.
      for (Field annotatedField : ReflectionUtils.findAllFieldsWithAnnotations(object.getClass(), unWrappedAnnotations)) {
        Object thisField = annotatedField.get(object);
        if (thisField == null) {
          annotatedField.set(object, newInstance(key, annotatedField.getType()));
        }
      }
    }

    return object;
  }

  /**
   * Find the fields in the declaring class being aware that if any of those fields are annotated with an annotation indicating it should be
   * unwrapped -  we should ignore that field, and instead add the fields that belong to that object.
   *
   * @return the fields found keyed by the field name.
   */
  private Map<String, Field> findFields() {
    Map<String, Field> fields = new HashMap<>();
    try {
      for (Map.Entry<String, Field> entry : ReflectionUtils.findFields(this.declaringClass).entrySet()) {
        if (ReflectionUtils.areAnyAnnotationsPresent(entry.getValue(), unWrappedAnnotations)) {
          Field unwrappedField = declaringClass.getField(entry.getKey());
          fields.putAll(ReflectionUtils.findFields(unwrappedField.getType()));
        } else {
          fields.put(entry.getKey(), entry.getValue());
        }
      }
    } catch (NoSuchFieldException ignore) {
    }

    return fields;
  }

  /**
   * Return the field for the object being aware that the field may be nested inside of another object annotated with an annotation
   * indicating it should be unwrapped.
   *
   * @return the field object.
   */
  private Object getField() {
    if (field.getDeclaringClass().isAssignableFrom(this.object.getClass())) {
      return ReflectionUtils.getField(field, this.object);
    }

    try {
      for (Field f : ReflectionUtils.findAllFieldsWithAnnotations(this.object.getClass(), unWrappedAnnotations)) {
        if (f.getType().equals(field.getDeclaringClass())) {
          return ReflectionUtils.getField(field, f.get(this.object));
        }
      }
    } catch (IllegalAccessException ignore) {
    }

    return null;
  }

  /**
   * Set the field in the object being aware that the field may be nested inside of another object annotated with an annotation indicating
   * it should be unwrapped.
   *
   * @param value      The value to set into the field.
   * @param expression the current expression that was used to idenfity the field, used only for exception cases.
   */
  private void setField(Object value, Expression expression) {
    // Normal case, the field is found in the object.
    if (field.getDeclaringClass().isAssignableFrom(this.object.getClass())) {
      ReflectionUtils.setField(field, object, value);
      return;
    }

    // Declaring class doesn't match up with the object, look for unwrapped fields, the field may be nested.
    for (Field f : ReflectionUtils.findAllFieldsWithAnnotations(this.object.getClass(), unWrappedAnnotations)) {
      if (f.getType().equals(field.getDeclaringClass())) {
        try {
          ReflectionUtils.setField(field, f.get(object), value);
        } catch (IllegalAccessException e) {
          throw new UpdateExpressionException("Unexpected failure setting expression [" + expression.getExpression() + "]", e);
        }
      }
    }
  }
}
