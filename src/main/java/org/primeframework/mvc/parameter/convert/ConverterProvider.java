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

import com.google.inject.ImplementedBy;

/**
 * This interface defines the method of getting Converters.
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultConverterProvider.class)
public interface ConverterProvider {
  /**
   * Returns the type converter for the given type. This converter is either the converter associated with the given
   * type of associated with a super class of the given type (not interfaces). This principal also works with arrays. If
   * the type is an array, then what happens is that the array type is asked for its component type using the method
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
  GlobalConverter lookup(Class<?> type);

  /**
   * Returns the Converter for the given annotation.
   *
   * @param annotation The annotation.
   * @return The Converter.
   */
  AnnotationConverter lookup(Annotation annotation);
}