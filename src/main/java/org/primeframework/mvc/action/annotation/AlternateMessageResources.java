/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Customize message resource behavior for an action class
 *
 * @author Brady Wied
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AlternateMessageResources {
  /**
   * @return By default, will look for messages, via the {@link org.primeframework.mvc.message.l10n.MessageProvider}
   *     interface, in an implementation specific search path (see {@link org.primeframework.mvc.message.l10n.ResourceBundleMessageProvider} for an
   *     example).
   *     If you want an additional action's messages in the search path, use this attribute.
   */
  Class<?>[] actions();
}
