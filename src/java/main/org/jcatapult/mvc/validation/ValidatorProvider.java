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
package org.jcatapult.mvc.validation;

import java.lang.annotation.Annotation;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines the provider that creates validators based on
 * annotations.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ImplementedBy(DefaultValidatorProvider.class)
public interface ValidatorProvider {
    /**
     * <p>
     * Returns the validator for the given annotation.
     * </p>
     *
     * @param   annotation The annotation.
     * @return  The Validator or null if one was not found
     */
    Validator lookup(Class<? extends Annotation> annotation);
}