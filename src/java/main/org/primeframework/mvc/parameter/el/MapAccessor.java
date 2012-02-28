/*
 * Copyright (c) 2001-2010, JCatapult.org, All Rights Reserved
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
import java.lang.reflect.Type;

import static net.java.lang.ObjectTools.*;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;

/**
 * <p>
 * This class models a collection accessor during expression evaluation.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class MapAccessor extends Accessor {
    Object key;
    MemberAccessor memberAccessor;

    public MapAccessor(ConverterProvider converterProvider, Accessor accessor, String index, MemberAccessor memberAccessor) {
        super(converterProvider, accessor);

        String path = memberAccessor.toString();
        Type objectType = super.type;
        super.type = TypeTools.componentType(objectType, path);
        this.memberAccessor = memberAccessor;

        Class<?> keyType = TypeTools.rawType(TypeTools.keyType(objectType, path));
        GlobalConverter converter = converterProvider.lookup(keyType);
        if (converter == null) {
            throw new ConversionException("No type converter is registered for the type [" + keyType + "], which is the " +
                "type for the key of the map at [" + path + "]");
        }

        this.key = converter.convertFromStrings(keyType, null, path, index);
    }

    /**
     * @return  The memberAccessor member variable.
     */
    public MemberAccessor getMemberAccessor() {
        return memberAccessor;
    }

    /**
     * @return  Always false. The reason is that since this retrieves from a Collection, we want
     *          it to look like a non-indexed property so that the context will invoke the method.
     */
    public boolean isIndexed() {
        return false;
    }

    public Object get(Context context) {
        try {
            return getValueFromCollection(this.object, key);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void set(String[] values, Context context) {
        set(convert(context, memberAccessor.field, values), context);
    }

    public void set(Object value, Context context) {
        setValueIntoCollection(object, key, value);
    }

    /**
     * Returns the annotation of the member this collection belongs to.
     *
     * @param   type The annotation type.
     * @return  The annotation or null.
     */
    @Override
    protected <T extends Annotation> T getAnnotation(Class<T> type) {
        return memberAccessor.getAnnotation(type);
    }
}
