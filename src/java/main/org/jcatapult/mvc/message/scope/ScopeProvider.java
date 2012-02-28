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
package org.jcatapult.mvc.message.scope;

import java.util.List;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This is the provider for scopes.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ImplementedBy(DefaultScopeProvider.class)
public interface ScopeProvider {
    /**
     * Looks up the scope for the given annotation.
     *
     * @param   scope The scope enum value.
     * @return  The Scope and never null.
     */
    Scope lookup(MessageScope scope);

    /**
     * @return  All of the registered scopes.
     */
    List<Scope> getAllScopes();
}