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
package org.primeframework.mvc.action.result;

import java.lang.annotation.Annotation;

/**
 * <p> This class is the default result invocation. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocation implements ResultInvocation {
  private final Annotation annotation;
  private final String uri;
  private final String resultCode;

  public DefaultResultInvocation(Annotation annotation, String uri, String resultCode) {
    this.annotation = annotation;
    this.uri = uri;
    this.resultCode = resultCode;
  }

  public Annotation annotation() {
    return annotation;
  }

  public String uri() {
    return uri;
  }

  public String resultCode() {
    return resultCode;
  }
}