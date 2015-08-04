/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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

import java.util.Deque;

/**
 * This class defines the mechanism used to locate action invocation objects. During a request, the invocation is set
 * using the {@link #setCurrent(ActionInvocation)} method and then can be retrieved any number of times using the
 * {@link
 * #getCurrent()} method.
 * <p>
 * Action invocations are always stored in a stack in order to allow different actions to be invoked on top of each
 * other. This is useful for invoking actions from FTL files and JSP tags.
 *
 * @author Brian Pontarelli
 */
public interface ActionInvocationStore {
  /**
   * Gets the current action invocation. Once the action invocation is set using the {@link
   * #setCurrent(ActionInvocation)} method, multiple calls to this method for a single request will always return the
   * same value.
   *
   * @return The action invocation or null if it hasn't been set yet.
   */
  ActionInvocation getCurrent();

  /**
   * Sets the invocation into the provider so that it can be fetched later.
   *
   * @param actionInvocation The invocation to set.
   */
  void setCurrent(ActionInvocation actionInvocation);

  /**
   * @return The deque that contains the action invocations.
   */
  Deque<ActionInvocation> getDeque();

  /**
   * Pops the current action from the stack.
   */
  void removeCurrent();
}