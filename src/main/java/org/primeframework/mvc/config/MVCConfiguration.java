/*
 * Copyright (c) 2012-2024, Inversoft Inc., All Rights Reserved
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
import java.nio.file.Path;
import java.security.Key;
import java.util.List;
import java.util.Set;

import io.fusionauth.http.Cookie.SameSite;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * The main configuration interface for Prime that outlines all the configurable values for the framework. This
 * interface can easily be implemented to suit any needs in your application.
 *
 * @author Brian Pontarelli
 */
public interface MVCConfiguration {
  /**
   * In most cases you should disable this feature. While it may be useful, modifying the URI may have un-intended consequences.
   *
   * @return true if alternate actions can be specified by using the {@link DefaultParameterParser#ACTION_PREFIX} prefix.
   */
  boolean allowActionParameterDuringActionMappingWorkflow();

  /**
   * @return true if unknown parameters should be allowed, false if they are not allowed.
   */
  boolean allowUnknownParameters();

  /**
   * @return true if auto HTML escaping will be enabled for all templates.
   */
  boolean autoHTMLEscapingEnabled();

  /**
   * @return The base directory of the entire web application.
   */
  Path baseDirectory();

  /**
   * @return The directory where the control templates are stored.
   */
  String controlTemplateDirectory();

  /**
   * @return The encryption key that is used to encrypt cookies.
   */
  Key cookieEncryptionKey();

  /**
   * @return true if CSRF handling is enabled.
   */
  boolean csrfEnabled();

  /**
   * @return true if empty HTTP request parameters should be considered null values by the conversion system.
   */
  boolean emptyParametersAreNull();

  /**
   * @return the result code to use when exceptions get thrown.  Defaults to 'error'
   */
  String exceptionResultCode();

  /**
   * @return The types of files that are allowed to be uploaded.
   */
  Set<String> fileUploadAllowedTypes();

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
   * @return The cookie name used to store the locale.
   */
  String localeCookieName();

  /**
   * @return The directory where the messages are stored.
   */
  String messageDirectory();

  /**
   * @return The name of the message flash cookie.
   */
  String messageFlashScopeCookieName();

  /**
   * @return The path of the action/result that handle missing requests (i.e. 404s).
   */
  String missingPath();

  /**
   * @return The maximum size in bytes of the save request cookie. Defaults to 16kb.
   */
  int savedRequestCookieMaximumSize();

  /**
   * @return The name of the cookie used to store Saved Request information.
   */
  String savedRequestCookieName();

  /**
   * @return The SameSite attribute for the Saved Request cookie.
   */
  SameSite savedRequestCookieSameSite();

  /**
   * @return The name of the static directory in the webapp.
   */
  String staticDirectory();

  /**
   * @return The number of seconds to check for Freemarker template updates (max integer means never and 0 means
   *     always).
   */
  int templateCheckSeconds();

  /**
   * @return The directory where the templates are stored.
   */
  String templateDirectory();

  /**
   * @return The annotations that identify a field to be un-wrapped - or be considered transparent by the
   *     {@link ExpressionEvaluator}.
   */
  List<Class<? extends Annotation>> unwrapAnnotations();
}
