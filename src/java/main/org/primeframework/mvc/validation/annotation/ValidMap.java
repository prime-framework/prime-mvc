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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * <p>
 * This annotation marks a Map in a bean as requiring validation.
 * By default, the validation framework does not traverse down the object
 * graph.
 * </p>
 *
 * @author Brian Pontarelli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidMap {
    /**
     * @return  A list of groups that the value should be validated for. By default it is validated
     *          for all groups.
     */
    String[] groups() default {};

    /**
     * @return  The required keys that should be in the Map. If the Map is null, these are used to
     *          validate the component type of the Map (i.e. the second generic type
     *          Map&lt;String, Address> is Address).
     */
    String[] keys();
}