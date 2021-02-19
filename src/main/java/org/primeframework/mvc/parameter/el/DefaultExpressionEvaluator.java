/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.ConverterStateException;

/**
 * This class is the default implementation of the ExpressionEvaluator service. This provides a robust expression
 * processing facility that leverages JavaBean properties, fields and generics to get and set values into Objects.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class DefaultExpressionEvaluator implements ExpressionEvaluator {
  private final MVCConfiguration configuration;

  private final ConverterProvider converterProvider;

  @Inject
  public DefaultExpressionEvaluator(ConverterProvider converterProvider, MVCConfiguration configuration) {
    this.converterProvider = converterProvider;
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String expand(final String str, final Object object, final boolean encode)
      throws ExpressionException {
    return new StrSubstitutor(new StrLookup<String>() {
      public String lookup(String name) {
        String value = getValue(name, object, Collections.emptyMap());
        if (value == null) {
          return "";
        }
        if (encode) {
          try {
            value = URLEncoder.encode(value, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            // Impossible
          }
        }

        return value;
      }
    }).replace(str);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<Object> getAllMemberValues(Object obj, Set<String> memberNames) {
    Collection<Object> values = new ArrayList<>();
    for (String name : memberNames) {
      values.add(getValue(name, obj));
    }

    return values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getValue(String expression, Object object) throws ExpressionException {
    Expression expr = new Expression(converterProvider, expression, object, null, configuration);
    return (T) expr.traverseToEndForGet();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getValue(String expression, Object object, Map<String, String> attributes) throws ExpressionException {
    Expression expr = new Expression(converterProvider, expression, object, attributes, configuration);
    Object value = expr.traverseToEndForGet();
    if (value == null) {
      return null;
    }

    return expr.getCurrentValueAsString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setValue(String expression, Object object, Object value) throws ExpressionException {
    Expression expr = new Expression(converterProvider, expression, object, null, configuration);
    expr.traverseToEndForSet();
    expr.setCurrentValue(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setValue(String expression, Object object, String[] values, Map<String, String> attributes)
      throws ConversionException, ConverterStateException, ExpressionException {
    Expression expr = new Expression(converterProvider, expression, object, attributes, configuration);
    expr.traverseToEndForSet();
    expr.setCurrentValue(values);
  }
}
