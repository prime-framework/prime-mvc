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

import java.lang.reflect.Method;

import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * This models a indexed property that takes a single parameter for the getter that is the index or key and two
 * parameters for the setter, the index/key and value.
 *
 * @author Brian Pontarelli
 */
public class IndexedAccessor extends MemberAccessor {
  String index;

  public IndexedAccessor(ConverterProvider converterProvider, MemberAccessor accessor, String index) {
    super(converterProvider, accessor);
    
    this.index = index;
  }

  /**
   * @return Always false. The reason is that since this invokes the indexed property, we want it to look like a
   *         non-indexed property so that the context will invoke the method.
   */
  public boolean isIndexed() {
    return false;
  }

  public Object get(Expression expression) {
    Method getter = propertyInfo.getMethods().get("get");
    if (getter.getParameterTypes().length == 0) {
      return ReflectionUtils.invoke(getter, this.object);
    }

    Class<?> indexType = getter.getParameterTypes()[0];
    GlobalConverter converter = converterProvider.lookup(indexType);
    if (converter == null) {
      throw new ConversionException("Error while getting an indexed property in the expression [" + expression.getExpression() +
        "]. The indexed property uses a key of type [" + indexType + "] but there isn't a converter registered for that type");
    }

    Object indexObject = converter.convertFromStrings(indexType, null, null, index);
    return ReflectionUtils.invoke(getter, this.object, indexObject);
  }

  public void set(String[] values, Expression expression) {
    set(convert(expression, field, values), expression);
  }

  public void set(Object value, Expression expression) {
    Method setter = propertyInfo.getMethods().get("set");
    if (setter.getParameterTypes().length == 1) {
      ReflectionUtils.invoke(setter, this.object, value);
    } else {
      Class<?> indexType = setter.getParameterTypes()[0];
      GlobalConverter converter = converterProvider.lookup(indexType);
      if (converter == null) {
        throw new ConversionException("Error while setting an indexed property in the expression [" + expression.getExpression() +
          "]. The indexed property uses a key of type [" + indexType + "] but there isn't a converter registered for that type");
      }

      Object indexObject = converter.convertFromStrings(indexType, null, null, index);
      ReflectionUtils.invoke(setter, this.object, indexObject, value);
    }
  }
}
