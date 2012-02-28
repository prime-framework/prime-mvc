/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;

/**
 * <p>
 * This
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Context {
    private final String expression;
    private final List<String> atoms;
    private final HttpServletRequest request;
    private final Map<String, String> attributes;
    private final Locale locale;
    private final ConverterProvider converterProvider;

    private Class<?> type;
    private Object object;
    private Accessor accessor;
    private int index;

    public Context(ConverterProvider converterProvider, String expression, List<String> atoms,
            HttpServletRequest request, Locale locale, Map<String, String> attributes) {
        this.expression = expression;
        this.atoms = atoms;
        this.request = request;
        this.locale = locale;
        this.attributes = attributes;
        this.converterProvider = converterProvider;
    }

    public Context(ConverterProvider converterProvider, String expression, List<String> atoms) {
        this(converterProvider, expression, atoms, null, null, null);
    }

    /**
     * @return  The full expression that this context is managing.
     */
    public String getExpression() {
        return expression;
    }

    public void init(Object object) {
        this.object = object;
        this.type = object.getClass();
    }

    public boolean skip() {
        return accessor != null && accessor.isIndexed();
    }

    public void initAccessor(String name) {
        // This is the indexed case, so the name is the index to the method
        if (accessor != null && accessor.isIndexed()) {
            accessor = new IndexedAccessor(converterProvider, (MemberAccessor) accessor, name);
        } else if (Collection.class.isAssignableFrom(type) || object.getClass().isArray()) {
            GlobalConverter converter = converterProvider.lookup(Integer.class);
            Integer index = (Integer) converter.convertFromStrings(Integer.class, null, null, name);

            accessor = new IndexedCollectionAccessor(converterProvider, accessor, index, accessor.getMemberAccessor());
        } else if (Map.class.isAssignableFrom(type)) {
            accessor = new MapAccessor(converterProvider, accessor, name, accessor.getMemberAccessor());
        } else {
            accessor = new MemberAccessor(converterProvider, type, name);
        }
    }

    public Object getCurrentValue() {
        return accessor.get(object, this);
    }

    public void setCurrentValue(String[] values) {
        accessor.set(object, values, this);
    }

    public void setCurrentValue(Object value) {
        accessor.set(object, value, this);
    }

    public Object createValue() {
        // Peek at the next atom, in case this is an array
        Object key = hasNext() ? peek() : null;
        Object value = accessor.createValue(key);
        accessor.set(object, value, this);
        return value;
    }

    public String peek() {
        return atoms.get(index);
    }

    public String next() {
        return atoms.get(index++);
    }

    public boolean hasNext() {
        return index < atoms.size();
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }

    public Accessor getAccessor() {
        return accessor;
    }

    public int getIndex() {
        return index;
    }

    public Locale getLocale() {
        return locale;
    }
}
