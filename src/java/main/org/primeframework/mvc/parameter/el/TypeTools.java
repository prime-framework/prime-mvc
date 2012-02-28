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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * This is a toolkit that assists with generics.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class TypeTools {
    /**
     * Determines the component type. Lists is the first type, Map is the second type, etc.
     *
     * @param   type The parameterized type.
     * @param   path The path to the type, used in exception message.
     * @return  The component type.
     */
    public static Type componentType(Type type, String path) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Map.class.isAssignableFrom(rawType)) {
                return parameterizedType.getActualTypeArguments()[1];
            } else if (Collection.class.isAssignableFrom(rawType)) {
                return parameterizedType.getActualTypeArguments()[0];
            } else {
                throw new ExpressionException("Unknown collection type [" + type + "]");
            }
        } else if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        }

        Class<?> rawType = (Class<?>) type;
        if (Map.class == type || Collection.class == type) {
            throw new ExpressionException("The method or member [" + path + "] returns a simple " +
                "Map or Collection. Unable to determine the type of the Map or Collection. " +
                "Please make this method generic so that the correct type can be determined.");
        } else if (rawType.isArray()) {
            return rawType.getComponentType();
        }

        return rawType;
    }

    /**
     * Determines the key type for a Map.
     *
     * @param   type The parameterized type.
     * @param   path The path to the type, used in exception message.
     * @return  The key type.
     */
    public static Type keyType(Type type, String path) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Map.class.isAssignableFrom(rawType)) {
                return parameterizedType.getActualTypeArguments()[0];
            }
        }

        throw new ExpressionException("The method or member [" + path + "] returns a simple Map. Unable to determine the " +
            "types of the Map. Please make this method or member generic so that the correct type can be determined.");
    }

    /**
     * Determines the final component type. This continues to loop over Collections until it hits
     * a non-parameterized type.
     *
     * @param   type The parameterized type.
     * @param   path The path to the type, used in exception message.
     * @return  The final component type.
     */
    public static Class<?> componentFinalType(Type type, String path) {
        while (!(type instanceof Class)) {
            type = componentType(type, path);
        }

        return (Class<?>) type;
    }

    /**
     * Determines the raw type of the type given.
     *
     * @param   type The type.
     * @return  The raw type.
     */
    public static Class<?> rawType(Type type) {
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            Class<?> componentType = (Class<?>) ((GenericArrayType) type).getGenericComponentType();
            type = Array.newInstance(componentType, 0).getClass();
        }

        return (Class<?>) type;
    }

}
