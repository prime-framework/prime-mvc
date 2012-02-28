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
package org.primeframework.mvc.scope.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.scope.ContextScope;

/**
 * <p>
 * This annotation should be used to mark member fields of actions that should be
 * fetched and stored in the ServletContext.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation(ContextScope.class)
@Target(ElementType.FIELD)
public @interface Context {
    /**
     * @return  This attribute determines the name under which that the value is stored in the context.
     *          The default name is the name of the field that the annotation is put on.
     */
    String value() default "##field-name##";
}