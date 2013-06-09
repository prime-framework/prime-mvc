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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

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

  public MemberAccessor(ConverterProvider converterProvider, MemberAccessor accessor) {
    super(converterProvider, accessor);
    this.field = accessor.field;
    this.propertyInfo = accessor.propertyInfo;
  }

  public MemberAccessor(ConverterProvider converterProvider, Class<?> declaringClass, String name, String expression) {
    super(converterProvider);

    this.declaringClass = declaringClass;

    Map<String, ReflectionUtils.PropertyInfo> properties = ReflectionUtils.findPropertyInfo(this.declaringClass);
    ReflectionUtils.PropertyInfo bpi = properties.get(name);
    if (bpi == null) {
      Map<String, Field> fields = ReflectionUtils.findFields(this.declaringClass);
      this.propertyInfo = null;
      this.field = fields.get(name);
    } else {
      this.propertyInfo = bpi;
      this.field = null;
    }

    if (this.field == null && this.propertyInfo == null) {
      throw new MissingPropertyExpressionException("While evaluating the expression [" + expression + "] the property/field [" +
        name + "] in the class [" + declaringClass + "]", name, declaringClass, expression);
    }

    super.type = (bpi != null) ? bpi.getGenericType() : this.field.getGenericType();
  }

  public boolean isIndexed() {
    return propertyInfo != null && propertyInfo.isIndexed();
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

    return ReflectionUtils.getField(field, this.object);
  }

  public void set(String[] values, Expression expression) {
    set(convert(expression, field, values), expression);
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
      ReflectionUtils.setField(field, object, value);
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
}
