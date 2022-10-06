/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

/**
 * This HTTP Method is not implemented by this MVC. Generally you'll want to return a <code>501</code> for this result
 * code.
 *
 * @author Daniel DeGroff
 */
public class NotImplementedException extends ErrorException {
  public NotImplementedException() {
    super("not-implemented");
    lookUpMessage = false;
  }
}
