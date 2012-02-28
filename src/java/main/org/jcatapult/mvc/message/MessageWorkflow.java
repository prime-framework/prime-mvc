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
package org.jcatapult.mvc.message;

import org.jcatapult.servlet.Workflow;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines the workflow process that handles messages.
 * The most common processing is for the flash scope. Messages in the
 * flash scope usually need to be transferred into the request during
 * request processing.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ImplementedBy(DefaultMessageWorkflow.class)
public interface MessageWorkflow extends Workflow {
}
