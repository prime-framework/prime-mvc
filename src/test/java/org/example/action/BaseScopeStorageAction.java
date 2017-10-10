/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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
package org.example.action;

import org.primeframework.mvc.scope.annotation.ActionSession;
import org.primeframework.mvc.scope.annotation.Context;
import org.primeframework.mvc.scope.annotation.Request;
import org.primeframework.mvc.scope.annotation.Session;

/**
 * @author Daniel DeGroff
 */
public abstract class BaseScopeStorageAction {
  @Session
  public Object sessionObject;

  @Context
  public Object contextObject;

  @Request
  public Object requestObject;

  @ActionSession
  public Object actionSessionObject;
}
