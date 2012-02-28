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
package org.primeframework.mvc.scope;

import org.primeframework.servlet.Workflow;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface marks a class as the scope retrieval part of the MVC
 * workflow. This part is how the JCatapult MVC loads values from the action
 * into various scopes.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ImplementedBy(DefaultScopeRetrievalWorkflow.class)
public interface ScopeRetrievalWorkflow extends Workflow {
}
