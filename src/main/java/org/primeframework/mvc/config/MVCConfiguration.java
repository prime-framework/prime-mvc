/*
 * Copyright (c) 2012-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.config;

import java.lang.annotation.Annotation;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * The main configuration interface for Prime that outlines all of the configurable values for the framework. This
 * interface can easily be implemented to suit any needs in your application.
 *
 * @author Brian Pontarelli
 */
public interface MVCConfiguration {
  /**
   * @return Whether or not unknown parameters should be allowed or if they should throw an exception.
   */
  boolean allowUnknownParameters();

  /**
   * @return The IV that is used to encrypt cookies.
   */
  AlgorithmParameterSpec cookieEncryptionIV();

  /**
   * @return The encryption key that is used to encrypt cookies.
   */
  Key cookieEncryptionKey();

  /**
   * @return True if empty HTTP request parameters should be considered null values by the conversion system.
   */
  boolean emptyParametersAreNull();

  /**
   * @return the result code to use when exceptions get thrown.  Defaults to 'error'
   */
  String exceptionResultCode();

  /**
   * @return The types of files that are allowed to be uploaded.
   */
  String[] fileUploadAllowedTypes();

  /**
   * @return The maximum size for an uploaded file.
   */
  long fileUploadMaxSize();

  /**
   * Set to true if actions should ignore empty parameters.  Defaults to false
   *
   * @return true or false
   */
  boolean ignoreEmptyParameters();

  /**
   * @return The number of seconds to check for message bundles updates (max integer means never and 0 means always).
   */
  int l10nReloadSeconds();

  /**
   * @return Whether or not runtime exceptions should be re-thrown out of Prime MVC.
   */
  boolean propagateRuntimeExceptions();

  /**
   * @return The resource directory where the templates, message bundles, emails, control templates, etc are loaded
   * from.
   */
  String resourceDirectory();

  /**
   * @return The name of the cookie used to store Saved Request information.
   */
  String savedRequestCookieName();

  /**
   * @return The static resource prefixes.
   */
  String[] staticResourcePrefixes();

  /**
   * @return Whether or not static resource loading is enabled.
   */
  boolean staticResourcesEnabled();

  /**
   * @return The number of seconds to check for Freemarker template updates (max integer means never and 0 means
   * always).
   */
  int templateCheckSeconds();

  /**
   * @return The annotations that identify a field to be un-wrapped - or be considered transparent by the {@link ExpressionEvaluator}.
   */
  List<Class<? extends Annotation>> unwrapAnnotations();
}
