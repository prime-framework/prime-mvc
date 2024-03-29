/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.binary.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a member as the recipient of a {@link Path} object.</p>
 * <p>
 * The object is written to a temporary file from reading bytes from the HTTP request body when the request is made
 * with
 * <code>Content-Type: application/octet-stream</code>.
 *
 * @author Daniel DeGroff
 */
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface BinaryRequest {
  /**
   * @return True if you want the temp file to be deleted after the action completes. False if you want the file to
   *     stick around. Keeping the file might be necessary if it will be processed later.
   */
  boolean deleteUponCompletion() default true;
}
