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
package org.primeframework.mvc.parameter.convert;

import java.lang.annotation.Annotation;
import static java.util.Arrays.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.primeframework.mvc.ObjectFactory;
import org.primeframework.mvc.parameter.convert.annotation.ConverterAnnotation;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the manager for all the Converters. It loads the
 * global type converters from Guice. Therefore, if you want to
 * supply a global custom type converter, just add it to a Guice
 * module and place it in your classpath. JCatapult will discover
 * the module and load it up.
 * </p>
 *
 * <p>
 * A converter for a given type will be retrieved when the manager
 * is queried for that type and all sub class of that type, unless
 * another converter is registered for a sub class of the type. For
 * example, registering a convert for the type Number would ensure
 * that Integer, Double, Float, etc. used that converter for
 * conversions. If a converter was registered for Number and another
 * converter for Double, the converter for Number would handle all
 * sub-class of Number (Integer, etc.) except Double.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultConverterProvider implements ConverterProvider {
    private static final Logger logger = Logger.getLogger(ConverterProvider.class.getName());
    private final static Map<Class<?>, Class<?>> converters = new HashMap<Class<?>, Class<?>>();
    private final ObjectFactory objectFactory;

    @Inject
    public static void initialize(ObjectFactory objectFactory) {
        List<Class<? extends GlobalConverter>> types = objectFactory.getAllForType(GlobalConverter.class);
        for (Class<? extends GlobalConverter> type : types) {
            org.primeframework.mvc.parameter.convert.annotation.GlobalConverter converter =
                type.getAnnotation(org.primeframework.mvc.parameter.convert.annotation.GlobalConverter.class);
            Class<?>[] convertTypes = converter.forTypes();
            for (Class<?> convertType : convertTypes) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Registering converter class [" + converter.getClass() + "] for type [" + convertType + "]");
                }

                converters.put(convertType, type);
            }
        }
    }

    @Inject
    public DefaultConverterProvider(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * <p>
     * Returns the global type converter for the given type. This converter is either the converter
     * associated with the given type of associated with a super class of the given type. This
     * principle also works with arrays. If the type is an array, then what happens is that the
     * array type is asked for its component type using the method getComponentType and this
     * type is used to query the manager. So, the converter registered for Number is returned
     * Double[] is queried (because Double is queried and since no converter was register for it,
     * then Number is checked).
     * </p>
     *
     * <p>
     * Normal types work the exact same way. First the type is checked and then its parents are
     * checked until Object is reached, in which case null is returned.
     * </p>
     *
     * <p>
     * Primitive values are treated as their wrapper classes. So, if int.class is passed into this
     * method (queried) then either a converter registered for Integer, or Number or null is returned
     * depending on what converters have been registered so far.
     * </p>
     *
     * @param   type The type to start with when looking for converters
     * @return  The converter or null if one was not found
     */
    public GlobalConverter lookup(Class<?> type) {
        Class<?> localType = type;

        // If it is an array, just use the component type because TypeConverters
        // can convert to arrays
        while (localType.isArray()) {
            localType = localType.getComponentType();
        }

        // The local type becomes null when it is an interface or a primitive and the
        // super class is asked for
        while (localType != null && localType != Object.class) {
            Class<?> converterType = converters.get(localType);
            if (converterType == null) {
                localType = localType.getSuperclass();
            } else {
                return (GlobalConverter) objectFactory.create(converterType);
            }
        }

        localType = type;
        Queue<Class<?>> interfaces = new LinkedList<Class<?>>(asList(type.getInterfaces()));
        Class<?> inter;
        while ((inter = interfaces.poll()) != null) {
            // First, check the interface
            Class<?> converterType = converters.get(inter);
            if (converterType != null) {
                return (GlobalConverter) objectFactory.create(converterType);
            }

            // Next, append the interfaces for this interface
            interfaces.addAll(asList(inter.getInterfaces()));

            // If there are no more, go up to the super class
            if (interfaces.size() == 0 && !localType.isInterface()) {
                localType = localType.getSuperclass();
                if (localType != Object.class) {
                    interfaces.addAll(asList(localType.getInterfaces()));
                }
            }
        }


        return null;
    }

    /**
     * <p>
     * Returns the Converter for the given annotation.
     * </p>
     *
     * @param   annotation The annotation.
     * @return  The Converter.
     */
    public AnnotationConverter lookup(Annotation annotation) {
        ConverterAnnotation ra = annotation.annotationType().getAnnotation(ConverterAnnotation.class);
        return objectFactory.create(ra.value());
    }
}