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
package org.primeframework.mvc.validation;

import java.lang.annotation.Annotation;

import org.primeframework.mvc.ObjectFactory;
import org.primeframework.mvc.validation.annotation.ValidatorAnnotation;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the manager for all the Validators. It loads the Validators from
 * Guice. Therefore, if you want to define a custom validator, create a new
 * annotation, annotate your annotation with the
 * {@link org.primeframework.mvc.validation.annotation.ValidatorAnnotation}
 * annotation, and then begin annotating classes. This class will taken care
 * for using the ResultAnnotation's value to determine the Result implementation
 * to construct and use.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultValidatorProvider implements ValidatorProvider {
    private final ObjectFactory objectFactory;

    @Inject
    public DefaultValidatorProvider(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * <p>
     * Returns the Validator for the given annotation.
     * </p>
     *
     * @param   annotation The annotation.
     * @return  The Validator.
     */
    public Validator lookup(Class<? extends Annotation> annotation) {
        ValidatorAnnotation ra = annotation.getAnnotation(ValidatorAnnotation.class);
        return objectFactory.create(ra.value());
    }
}