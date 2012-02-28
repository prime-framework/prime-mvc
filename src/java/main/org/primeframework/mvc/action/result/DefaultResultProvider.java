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
package org.primeframework.mvc.action.result;

import java.lang.annotation.Annotation;

import org.primeframework.mvc.ObjectFactory;
import org.primeframework.mvc.action.result.annotation.ResultAnnotation;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the manager for all the Results. It loads the Results from
 * Guice. Therefore, if you want to define a custom result, create a new
 * annotation, annotate your annotation with the
 * {@link org.primeframework.mvc.action.result.annotation.ResultAnnotation}
 * annotation, and then begin annotating classes. This class will taken care
 * for using the ResultAnnotation's value to determine the Result implementation
 * to construct and use.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultResultProvider implements ResultProvider {
    private final ObjectFactory objectFactory;

    @Inject
    public DefaultResultProvider(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * <p>
     * Returns the result for the given annotation.
     * </p>
     *
     * @param   annotation The annotation.
     * @return  The Result or null if one was not found
     */
    public Result lookup(Class<? extends Annotation> annotation) {
        ResultAnnotation ra = annotation.getAnnotation(ResultAnnotation.class);
        return objectFactory.create(ra.value());
    }
}