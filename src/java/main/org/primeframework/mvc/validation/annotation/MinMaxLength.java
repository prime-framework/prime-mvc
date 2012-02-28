/*
 * Copyright (c) 2001-2011, JCatapult.org, All Rights Reserved
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
package org.primeframework.mvc.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.validation.MinMaxLengthValidator;

/**
 * <p>
 * This class can be used to annotation fields such that the field is
 * validated to ensure that the length of a value is within the bounds
 * set in the annotation. This will work on Strings and check the length
 * of the String, Collections and check the size of the collection,
 * Maps and check the size of the Map, and arrays and check the length of
 * the array.
 * </p>
 *
 * @author  Chadwick K. Boggs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ValidatorAnnotation(MinMaxLengthValidator.class)
public @interface MinMaxLength {
    /**
     * @return  Sets the min length for the field. This defaults to 0.
     */
    int min() default 0;

    /**
     * @return  Sets the max length for the field. This defaults to Integer.MAX_VALUE.
     */
    int max() default Integer.MAX_VALUE;

    /**
     * @return  The message key that is used if the field is null. The default is based on the fields
     *          fully qualified name in the current parameters and the String <code>minmaxlength</code>
     *          is appended to that. For example: <code>user.description.minmaxlength</code>.
     */
    String key() default "";

    /**
     * @return  The validation groups that this validation will be run in. Groups are specified in
     *          an action and can control how validation is performed. This defaults to all groups.
     */
    String[] groups() default {};
}
