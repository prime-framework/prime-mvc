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
package org.primeframework.mvc.action;

import org.primeframework.mvc.workflow.Workflow;

/**
 * This class defines the workflow process that uses the incoming request URI to determine the action to invoke. This
 * locates the {@link ActionInvocation} and ensures that it can be accessed during the current request.
 *
 * @author Brian Pontarelli
 */
public interface ActionMappingWorkflow extends Workflow {
}