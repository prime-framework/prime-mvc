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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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

import static net.java.lang.reflect.ReflectionTools.*;

/**
 * <p>
 * This
 * </p>
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

    protected abstract Object get(Context context);

    protected abstract void set(String[] values, Context context);

    protected abstract void set(Object value, Context context);

    protected abstract <T extends Annotation> T getAnnotation(Class<T> type);

    public final Object get(Object object, Context context) {
        this.object = object;
        return get(context);
    }

    public final void set(Object object, String[] values, Context context) {
        this.object = object;
        set(values, context);
    }

    public final void set(Object object, Object value, Context context) {
        this.object = object;
        set(value, context);
    }

    /**
     * <p>
     * After the object is originally get or set, this method can be called to update the value.
     * This method should only work if the {@link #set(Object, String[], Context)} or
     * {@link #get(Object, Context)} method was called first.
     * </p>
     *
     * <p>
     * <strong>NOTE:</strong> Accessors are not thread safe and need not be because a new one is
     * created for each atom.
     * </p>
     *
     * @param   value The value to update the accessor with.
     * @param   context The current context.
     */
    public void update(Object value, Context context) {
        if (object == null) {
            throw new IllegalStateException("The object is null, unable to update.");
        }

        set(object, value, context);
    }

    /**
     * @return  Returns the member accessor that is closest to the current atom in the expression.
     *          If the current atom is a member, this should just return <strong>this</strong>.
     *          If the current atom is a collection for example, this would return the member that
     *          the collection was retrieved from.
     */
    public abstract MemberAccessor getMemberAccessor();

    /**
     * Creates a new instance of the current type.
     *
     * @param   key This is only used when creating arrays. It is the next atom, which is always
     *          the size of the array.
     * @return  The new value.
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
                throw new ExpressionException("Attempting to create an array, but there isn't an index " +
                    "available to determine the size of the array");
            }

            value = Array.newInstance(typeClass.getComponentType(), Integer.parseInt(key.toString()) + 1);
        } else {
            value = instantiate(typeClass);
        }

        return value;
    }

    /**
     * Converts the given value parameter (parameter) to a type that is accepted by the set method of
     * this property. This method attempts to convert the value regardless of the value being null.
     * However, this method short circuits and returns the value unchanged if value is runtime
     * assignable to the type of this BaseBeanProperty.
     *
     * @param   context The current context.
     * @param   field The field that the conversion is occurring for. This is used to look for
     *          conversion annotations.
     * @param   values The String values to convert.
     * @return  The value parameter converted to the correct type.
     * @throws  ConversionException If there was a problem converting the parameter.
     */
    protected Object convert(Context context, Field field, final String... values) throws ConversionException {
        Object newValue = values;

        // First look for annotations
        if (field != null) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                ConverterAnnotation converterAnnotation = annotation.annotationType().getAnnotation(ConverterAnnotation.class);
                if (converterAnnotation != null) {
                    AnnotationConverter converter = converterProvider.lookup(annotation);
                    return converter.convertFromStrings(annotation, values, type, context.getAttributes(), context.getExpression());
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

            newValue = converter.convertFromStrings(type, context.getAttributes(), context.getExpression(), values);
        }

        return newValue;
    }

    public String toString() {
        return "declaring class [" + declaringClass + "]";
    }
}
