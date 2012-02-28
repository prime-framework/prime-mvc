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
package org.jcatapult.mvc.parameter.el;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * <p>
 * This interface defines a mechanism for the {@link MemberAccessor} class
 * to verify that a JavaBean method is valid. These methods need not conform
 * to the JavaBean specification.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface MethodVerifier {
    /**
     * Verifies if the property method is valid or not. The BeanPropertyInfo in case anything additional
     * about the property is needed.
     *
     * @param   method The method to verify.
     * @param   info The info about the property if needed. This might not be completely filled out
     *          yet because it is built as the reflection occurs.
     * @return  An error if the property method is invali or null if the method is valid.
     */
    String isValid(Method method, PropertyInfo info);

    /**
     * Returns the property type for the property that the given method belongs to.
     *
     * @param   method The method to return the property for.
     * @return  The type and never null. This method is called after the isValid method is called and
     *          therefore the method is valid.
     */
    Class<?> determineType(Method method);

    /**
     * Whether or not this property is an indexed property.
     *
     * @param   method The method to determine if it is indexed.
     * @return  True if indexed or false otherwise.
     */
    boolean isIndexed(Method method);

    /**
     * Determines the generic type of the property.
     *
     * @param   method The method to pull the generic type from.
     * @return  The generic type.
     */
    Type determineGenericType(Method method);
}