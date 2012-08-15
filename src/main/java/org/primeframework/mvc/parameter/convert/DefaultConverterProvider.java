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
package org.primeframework.mvc.parameter.convert;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.primeframework.mvc.parameter.convert.annotation.ConverterAnnotation;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import static java.util.Arrays.*;

/**
 * This class is the manager for all the Converters. It loads the global type converters from Guice. Therefore, if you
 * want to supply a global custom type converter, just add it to a Guice module and place it in your classpath.
 * Prime will discover the module and load it up.
 * <p/>
 * A converter for a given type will be retrieved when the manager is queried for that type and all sub class of that
 * type, unless another converter is registered for a sub class of the type. For example, registering a convert for the
 * type Number would ensure that Integer, Double, Float, etc. used that converter for conversions. If a converter was
 * registered for Number and another converter for Double, the converter for Number would handle all sub-class of
 * Number (Integer, etc.) except Double.
 *
 * @author Brian Pontarelli
 */
@Singleton
public class DefaultConverterProvider implements ConverterProvider {
  private final Injector injector;
  private final Map<Class<?>, GlobalConverter> converters;

  @Inject
  public DefaultConverterProvider(Injector injector, Map<Class<?>, GlobalConverter> converters) {
    this.injector = injector;
    this.converters = converters;
  }

  /**
   * Returns the global type converter for the given type. This converter is either the converter associated with the
   * given type of associated with a super class of the given type. This principle also works with arrays. If the type
   * is an array, then what happens is that the array type is asked for its component type using the method
   * getComponentType and this type is used to query the manager. So, the converter registered for Number is returned
   * Double[] is queried (because Double is queried and since no converter was register for it, then Number is
   * checked).
   * <p/>
   * Normal types work the exact same way. First the type is checked and then its parents are checked until Object is
   * reached, in which case null is returned.
   * <p/>
   * Primitive values are treated as their wrapper classes. So, if int.class is passed into this method (queried) then
   * either a converter registered for Integer, or Number or null is returned depending on what converters have been
   * registered so far.
   *
   * @param type The type to start with when looking for converters
   * @return The converter or null if one was not found
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
      GlobalConverter converter = converters.get(localType);
      if (converter == null) {
        localType = localType.getSuperclass();
      } else {
        return converter;
      }
    }

    localType = type;
    Queue<Class<?>> interfaces = new LinkedList<Class<?>>(asList(type.getInterfaces()));
    Class<?> inter;
    while ((inter = interfaces.poll()) != null) {
      // First, check the interface
      GlobalConverter converter = converters.get(inter);
      if (converter != null) {
        return converter;
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
   * Returns the Converter for the given annotation.
   *
   * @param annotation The annotation.
   * @return The Converter.
   */
  public AnnotationConverter lookup(Annotation annotation) {
    ConverterAnnotation ra = annotation.annotationType().getAnnotation(ConverterAnnotation.class);
    return injector.getInstance(ra.value());
  }
}