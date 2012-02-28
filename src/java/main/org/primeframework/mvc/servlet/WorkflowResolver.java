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
package org.primeframework.mvc.servlet;

import java.util.List;

/**
 * <p> This class is used by the {@link JCatapultFilter} to allow additional processing to occur during an HTTP request
 * rather than defining HTTP filters in web.xml. The JCatapultFilter uses this interface to determine the list of {@link
 * Workflow} implementations to invoke and the order to invoke them in. </p>
 *
 * @author Brian Pontarelli
 */
public interface WorkflowResolver {
  /**
   * Called in the {@link JCatapultFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   * javax.servlet.FilterChain)} method in order to get the list of Workflow implementations that should be invoked.
   *
   * @return The list of workflows that should be invoked by the JCatapultFilter.
   */
  List<Workflow> resolve();

  /**
   * Called in the {@link JCatapultFilter#destroy()} method in order to get the list of Workflow classes that should
   * destroyed.
   *
   * @return The list of workflow classes.
   */
  List<Class<? extends Workflow>> getTypes();
}