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
 *
 */
package org.primeframework.servlet;

/**
 * <p>
 * This is a specialized workflow that can be destoryed. Most workflows
 * are request based and won't need to be destoryed, but sometimes things
 * need to be cleaned up and this is how Workflows can indicate that they
 * want to be destroyed by the JCatapultFilter.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface DestroyableWorkflow extends Workflow {
    /**
     * Performs any clean up on the workflow that is necessary.
     */
    void destroy();
}