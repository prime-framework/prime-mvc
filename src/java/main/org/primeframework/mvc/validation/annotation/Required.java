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
package org.primeframework.mvc.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.validation.RequiredValidator;

/**
 * <p>
 * This annotation is a validation annotation that indicates a field is
 * required.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ValidatorAnnotation(RequiredValidator.class)
public @interface Required {
    /**
     * @return  The message key that is used if the field is null. The default is based on the fields
     *          fully qualified name in the current parameters and the String <code>required</code>
     *          is appended to that. For example: <code>user.name.required</code>.
     */
    String key() default "";

    /**
     * @return  The validation groups that this validation will be run in. Groups are specified in
     *          an action and can control how validation is performed. This defaults to all groups.
     */
    String[] groups() default {};
}