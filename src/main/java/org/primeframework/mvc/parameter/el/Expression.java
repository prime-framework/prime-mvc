/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;

/**
 * This class is the evaluation context.
 *
 * @author Brian Pontarelli
 */
public class Expression {
  private final List<String> atoms;

  private final Map<String, String> attributes;

  private final MVCConfiguration configuration;

  private final ConverterProvider converterProvider;

  private final String expression;

  private Accessor accessor;

  private Object current;

  private int index;

  private Class<?> type;

  public Expression(ConverterProvider converterProvider, String expression, Object current,
                    Map<String, String> attributes, MVCConfiguration configuration) {
    this.expression = expression;
    this.attributes = attributes;
    this.converterProvider = converterProvider;
    this.atoms = parse(expression);
    this.configuration = configuration;
    setCurrentObject(current);
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public Accessor getCurrentAccessor() {
    return accessor;
  }

  public Object getCurrentObject() {
    return current;
  }

  private void setCurrentObject(Object object) {
    this.current = object;
    this.type = object.getClass();
  }

  public String getCurrentValueAsString() {
    Class<?> type = current.getClass();
    GlobalConverter converter = converterProvider.lookup(type);
    if (converter == null) {
      throw new ConverterStateException("No type converter found for the type [" + type + "]");
    }

    return converter.convertToString(type, attributes, expression, current);
  }

  public String getExpression() {
    return expression;
  }

  public Object traverseToEndForGet() {
    while (hasNext()) {
      next();

      Object value = getCurrentValue();
      if (value == null) {
        return null;
      }

      setCurrentObject(value);
    }

    return getCurrentObject();
  }

  public void traverseToEndForSet() {
    while (hasNext()) {
      next();

      if (hasNext()) {
        Object value = getCurrentValue();
        if (value == null) {
          value = createValue();
        }

        if (value != null) {
          setCurrentObject(value);
        }
      }
    }
  }

  private Object createValue() {
    // Peek at the next atom, in case this is an array
    Object key = hasNext() ? peek() : null;
    Object value = accessor.createValue(key);
    setCurrentValue(value);
    return value;
  }

  private Object getCurrentValue() {
    return accessor.get(current, this);
  }

  public void setCurrentValue(String[] values) {
    try {
      accessor.set(current, values, this);
    } catch (UnsupportedOperationException e) {
      // Re-using allowUnknownParameters. At runtime, if this exception is thrown
      // it means someone is trying to set something we did not intend. We can safely
      // treat this as an unknown parameter and ignore this exception.
      if (configuration.allowUnknownParameters()) {
        return;
      }

      throw e;
    }
  }

  public void setCurrentValue(Object value) {
    try {
      accessor.set(current, value, this);
    } catch (UnsupportedOperationException e) {
      // Re-using allowUnknownParameters. At runtime, if this exception is thrown
      // it means someone is trying to set something we did not intend. We can safely
      // treat this as an unknown parameter and ignore this exception.
      if (configuration.allowUnknownParameters()) {
        return;
      }

      throw e;
    }
  }

  private boolean hasNext() {
    return index < atoms.size();
  }

  private void next() {
    String atom = atoms.get(index++);

    // This is the indexed case, so the next atom is the index
    if (accessor != null && accessor.isIndexed()) {
      accessor = new IndexedAccessor(converterProvider, (MemberAccessor) accessor, atom);
    } else {
      if (Collection.class.isAssignableFrom(type) || current.getClass().isArray()) {
        GlobalConverter converter = converterProvider.lookup(Integer.class);
        Integer index = (Integer) converter.convertFromStrings(Integer.class, null, null, atom);

        accessor = new IndexedCollectionAccessor(converterProvider, accessor, index, accessor.getMemberAccessor());
      } else if (Map.class.isAssignableFrom(type)) {
        accessor = new MapAccessor(converterProvider, accessor, atom, accessor.getMemberAccessor());
      } else {
        accessor = new MemberAccessor(converterProvider, type, atom, expression, configuration);
      }
    }

    // Check if the new accessor is indexed and if there are no more atoms left. In this case, we error out.
    while (skip()) {
      if (!hasNext()) {
        throw new IndexExpressionException("Encountered an indexed property without an index in the expression [" + expression + "]");
      }

      // Recurse until we hit a non-indexed atom
      next();
    }
  }

  /**
   * This breaks the expression name down into manageable pieces. These are the individual instances of the Atom inner class which store the name and
   * the indices (which could be null or any object). This is broken on the '.' character.
   *
   * @param expression The expression string to break down.
   * @return A new ArrayList of PropertyInfo objects.
   * @throws ExpressionException If the property string is invalid.
   */
  private List<String> parse(String expression) throws ExpressionException {
    char[] ca = expression.toCharArray();
    List<String> list = new ArrayList<>();
    int index = 0;
    int position = 0;
    int openBracket = 0;
    char[] buf = new char[128];
    boolean insideBracket = false;
    boolean insideQuote = false;

    for (; index < ca.length; index++) {
      if (ca[index] == '.' && !insideQuote) {
        if (insideBracket || insideQuote) {
          throw new InvalidExpressionException("The expression string [" + expression + "] contains an invalid indices");
        }

        if (position == 0) {
          throw new InvalidExpressionException("The expression string [" + expression + "] is invalid.");
        }

        list.add(new String(buf, 0, position));
        position = 0;
      } else if (ca[index] == '[' && !insideQuote) {
        if (insideBracket) {
          throw new InvalidExpressionException("The expression string [" + expression + "] contains an invalid indices");
        }

        list.add(new String(buf, 0, position));
        insideBracket = true;
        position = 0;
        openBracket = index;
      } else if (ca[index] == ']' && !insideQuote) {
        if (!insideBracket) {
          throw new InvalidExpressionException("The expression string [" + expression + "] contains an invalid indices");
        }

        if (position == 0 && (index - openBracket == 1)) {
          list.add(null);
        } else {
          list.add(new String(buf, 0, position));
        }

        // Gobble up the period if there is one
        if (index + 1 < ca.length && ca[index + 1] == '.') {
          index++;
        }

        insideBracket = false;
        position = 0;
      } else if (ca[index] == '\'' || ca[index] == '\"') {
        if (!insideBracket) {
          throw new InvalidExpressionException("The expression string [" + expression + "] is invalid.");
        }

        insideQuote = !insideQuote;
      } else {
        if (position == buf.length) {
          if (buf.length >= 32_768) {
            throw new InvalidExpressionException("The expression token [" + new String(buf, 0, position) + "] is too long");
          }

          char[] newBuf = new char[buf.length + 128];
          System.arraycopy(buf, 0, newBuf, 0, position);
          buf = newBuf;
        }

        buf[position++] = ca[index];
      }
    }

    if (position > 0) {
      list.add(new String(buf, 0, position));
    }

    // atom may be null
    // - The 'class' name is reserved. This will fail anyway, but failing earlier with a better message. This also
    //   allows us to ignore this expression when allowUnknownParameters is true which will avoid un-necessary
    //   logging.
    for (String atom : list) {
      if ("class".equals(atom)) {
        throw new InvalidExpressionException("The expression string [" + expression + "] is invalid.");
      }
    }

    return list;
  }

  private String peek() {
    return atoms.get(index);
  }

  private boolean skip() {
    return accessor != null && accessor.isIndexed();
  }
}
