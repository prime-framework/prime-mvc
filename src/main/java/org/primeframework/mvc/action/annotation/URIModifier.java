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
package org.primeframework.mvc.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to modify the way the Java Package is used to define the URI.
 * <p>
 * For example, take the following Java package and Action:
 * <code>com.example.action.wellKnown.OpenidConfigurationAction</code>
 * <p>Due to Java package name restriction it is not possible to use this convention based schema and have a URI for
 * this action of
 * <p>
 * <code>http://www.example.com/.well-known/openid-configuration</code>
 * <p>
 * This modifier could be used to modify the URI during the action configuration from <code>well-known</code> to
 * <code>.well-known</code>.
 *
 * @author Daniel DeGroff
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface URIModifier {

  /**
   * Modifies the name of the package when building the URI in the Action Configuration.
   *
   * @return the name of the package used to build the URL segment.
   */
  String value();
}
