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
package org.jcatapult.mvc.scope;

import java.lang.annotation.Annotation;

import org.jcatapult.mvc.ObjectFactory;
import org.jcatapult.mvc.scope.annotation.ScopeAnnotation;

import com.google.inject.Inject;

/**
 * <p>
 * This class implements the scope provider interface.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultScopeProvider implements ScopeProvider {
    private final ObjectFactory objectFactory;

    @Inject
    public DefaultScopeProvider(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * {@inheritDoc}
     */
    public Scope lookup(Class<? extends Annotation> scopeAnnotation) {
        ScopeAnnotation ca = scopeAnnotation.getAnnotation(ScopeAnnotation.class);
        return objectFactory.create(ca.value());
    }
}