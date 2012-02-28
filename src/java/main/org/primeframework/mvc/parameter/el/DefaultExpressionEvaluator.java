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

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.inject.Inject;
import net.java.variable.ExpanderException;
import net.java.variable.ExpanderStrategy;
import net.java.variable.VariableExpander;
import org.primeframework.locale.annotation.CurrentLocale;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;

/**
 * <p>
 * This class is the default implementation of the ExpressionEvaluator
 * service. This provides a robust expression processing facility that
 * leverages JavaBean properties, fields and generics to get and set
 * values into Objects.
 * </p>
 *
 * <p>
 * TODO fully document here
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class DefaultExpressionEvaluator implements ExpressionEvaluator {
    private static final Map<String, char[]> expressionCache = new WeakHashMap<String, char[]>(15);
    private final Locale locale;
    private final HttpServletRequest request;
    private final ConverterProvider converterProvider;

    @Inject
    public DefaultExpressionEvaluator(@CurrentLocale Locale locale, HttpServletRequest request,
            ConverterProvider converterProvider) {
        this.locale = locale;
        this.request = request;
        this.converterProvider = converterProvider;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getValue(String expression, Object object) throws ExpressionException {
        List<String> atoms = parse(expression);
        Context context = new Context(converterProvider, expression, atoms);
        context.init(object);
        while (context.hasNext()) {
            String atom = context.next();
            context.initAccessor(atom);
            if (context.skip()) {
                continue;
            }

            Object value = context.getCurrentValue();
            if (value == null) {
                return null;
            }

            context.init(value);
        }

        return (T) context.getObject();
    }

    /**
     * {@inheritDoc}
     */
    public String getValue(String expression, Object object, Map<String, String> attributes)
    throws ExpressionException {
        Object value = getValue(expression, object);
        if (value == null) {
            return null;
        }

        Class<?> type = value.getClass();
        GlobalConverter converter = converterProvider.lookup(type);
        if (converter == null) {
            throw new ConverterStateException("No type converter found for the type [" + type + "]");
        }

        return converter.convertToString(type, attributes, expression, value);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String expression, Object object, Object value) throws ExpressionException {
        List<String> atoms = parse(expression);
        Context context = new Context(converterProvider, expression, atoms);
        context.init(object);
        while (context.hasNext()) {
            String atom = context.next();
            context.initAccessor(atom);
            if (context.skip()) {
                if (!context.hasNext()) {
                    throw new ExpressionException("Encountered an indexed property without an index in the " +
                        "expression [" + expression + "]");
                }

                continue;
            }

            if (!context.hasNext()) {
                context.setCurrentValue(value);
            } else {
                Object nextValue = context.getCurrentValue();
                if (nextValue == null) {
                    nextValue = context.createValue();
                }

                context.init(nextValue);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String expression, Object object, String[] values, Map<String, String> attributes)
    throws ConversionException, ConverterStateException, ExpressionException {
        List<String> atoms = parse(expression);
        Context context = new Context(converterProvider, expression, atoms, request, locale, attributes);
        context.init(object);
        while (context.hasNext()) {
            String atom = context.next();
            context.initAccessor(atom);
            if (context.skip()) {
                if (!context.hasNext()) {
                    throw new ExpressionException("Encountered an indexed property without an index in the " +
                        "expression [" + expression + "]");
                }

                continue;
            }

            if (!context.hasNext()) {
                context.setCurrentValue(values);
            } else {
                Object nextValue = context.getCurrentValue();
                if (nextValue == null) {
                    nextValue = context.createValue();
                }

                context.init(nextValue);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String expand(String str, final Object object)
    throws ExpressionException {
        return VariableExpander.expand(str, new ExpanderStrategy() {
            public String expand(String variableName) throws ExpanderException {
                return getValue(variableName, object, new HashMap<String, String>());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getAllMembers(Class<?> type) {
        return MemberAccessor.getAllMembers(type);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Object> getAllMemberValues(Object obj) {
        Set<String> names = getAllMembers(obj.getClass());
        Collection<Object> values = new ArrayList<Object>();
        for (String name : names) {
            values.add(getValue(name, obj));
        }

        return values;
    }

    /**
     * {@inheritDoc}
     */
    public <T extends Annotation> T getAnnotation(Class<T> type, String expression, Object object) {
        List<String> atoms = parse(expression);
        Context context = new Context(converterProvider, expression, atoms);
        context.init(object);
        while (context.hasNext()) {
            String atom = context.next();
            context.initAccessor(atom);
            if (context.skip()) {
                if (!context.hasNext()) {
                    throw new ExpressionException("Encountered an indexed property without an index in the " +
                        "expression [" + expression + "]");
                }

                continue;
            }

            if (context.hasNext()) {
                Object nextValue = context.getCurrentValue();
                if (nextValue == null) {
                    nextValue = context.createValue();
                }

                context.init(nextValue);
            }
        }

        return context.getAccessor().getAnnotation(type);
    }

    /**
     * This breaks the expression name down into manageable pieces. These are the individual instances
     * of the Atom inner class which store the name and the indices (which could be null or any object).
     * This is broken on the '.' character.
     *
     * @param   expression The expression strng to break down.
     * @return  A new ArrayList of PropertyInfo objects.
     * @throws  ExpressionException If the property string is invalid.
     */
    private List<String> parse(String expression) throws ExpressionException {
        Parser parser = new Parser(toCharArray(expression));
        List<String> info = new ArrayList<String>();
        for (String s : parser) {
            info.add(s);
        }

        return info;
    }

    protected char[] toCharArray(String expression) {
        char[] expr;
        synchronized (expressionCache) {
            if ((expr = expressionCache.get(expression)) == null) {
                expr = expression.toCharArray();
                expressionCache.put(expression, expr);
            }
        }

        return expr;
    }

    private class Parser implements Iterable<String> {
        private final char[] expression;

        public Parser(char[] expression) {
            this.expression = expression;
        }

        public Iterator<String> iterator() {
            return new Iterator<String>() {
                int index;
                int position;
                char[] ca = new char[128];
                boolean insideBracket = false;
                boolean insideQuote = false;

                public boolean hasNext() {
                    return index < expression.length;
                }

                public String next() {
                    for (; index < expression.length; index++) {
                        if (expression[index]  == '.' && !insideQuote) {
                            if (insideBracket || insideQuote) {
                                throw new ExpressionException("The expression string [" +
                                    new String(expression) + "] contains an invalid indices");
                            }

                            if (position == 0) {
                                throw new ExpressionException("The expression string [" +
                                    new String(expression) + "] is invalid.");
                            }

                            String result = new String(ca, 0, position);
                            index++;
                            position = 0;
                            return result;
                        } else if (expression[index]  == '[' && !insideQuote) {
                            if (insideBracket) {
                                throw new ExpressionException("The expression string [" +
                                    new String(expression) + "] contains an invalid indices");
                            }

                            String result = new String(ca, 0, position);
                            insideBracket = true;
                            index++;
                            position = 0;
                            return result;
                        } else if (expression[index]  == ']' && !insideQuote) {
                            if (!insideBracket) {
                                throw new ExpressionException("The expression string [" +
                                    new String(expression) + "] contains an invalid indices");
                            }

                            // Gobble up the period if there is one
                            index++;
                            if (index < expression.length && expression[index] == '.') {
                                index++;
                            }

                            insideBracket = false;
                            String result = new String(ca, 0, position);
                            position = 0;
                            return result;
                        } else if (expression[index] == '\'' || expression[index] == '\"') {
                            if (!insideBracket) {
                                throw new ExpressionException("The expression string [" +
                                    new String(expression) + "] is invalid.");
                            }

                            insideQuote = !insideQuote;
                        } else {
                            ca[position++] = expression[index];
                        }
                    }

                    if (position > 0) {
                        String result = new String(ca, 0, position);
                        index++;
                        position = 0;
                        return result;
                    }

                    return null;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
