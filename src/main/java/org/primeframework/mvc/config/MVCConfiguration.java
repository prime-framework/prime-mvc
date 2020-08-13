/*
 * Copyright (c) 2012-2020, Inversoft Inc., All Rights Reserved
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
   * @return Whether or not auto HTML escaping will be enabled for all templates.
   */
  boolean autoHTMLEscapingEnabled();

  /**
   * @return The IV that is used to encrypt cookies.
   */
  AlgorithmParameterSpec cookieEncryptionIV();

  /**
   * @return The encryption key that is used to encrypt cookies.
   */
  Key cookieEncryptionKey();

  /**
   * @return Whether or not the CSRF handling is enabled or not.
   */
  boolean csrfEnabled();

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
   * @return The cookie name used to store the locale.
   */
  String localeCookieName();

  /**
   * @return The name of the message flash cookie.
   */
  String messageFlashScopeCookieName();

  /**
   * @return The resource directory where the templates, message bundles, emails, control templates, etc are loaded
   * from.
   */
  String resourceDirectory();

  /**
   * Tomcat limits the header size to 8192 bytes (8 KB). See maxHttpHeaderSize on https://tomcat.apache.org/tomcat-8.5-doc/config/http.html
   * <br>
   * If we write the save request cookie too big we will exceed 8 KB and the client will receive a 500 from Tomcat with
   * the following stack trace.
   * <pre>
   *    org.apache.coyote.http11.HeadersTooLargeException: An attempt was made to write more data to the response headers than there was room available in the buffer.
   *      Increase maxHttpHeaderSize on the connector or write less data into the response headers.
   * </pre>
   * <p>
   * For this reason we should have a configured limit to the size of the cookie to attempt to prevent this scenario.
   * Once this limit is exceeded Prime will choose not to write the save request cookie and the user will not be
   * redirected after login. This seems to be a better user experience than a 500. If you were to increase the
   * maxHttpHeaderSize configured in Tomcat, this value then could also be increased.
   *
   * @return The maximum size in bytes of the save request cookie.
   */
  int savedRequestCookieMaximumSize();

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
   * @return The annotations that identify a field to be un-wrapped - or be considered transparent by the {@link
   * ExpressionEvaluator}.
   */
  List<Class<? extends Annotation>> unwrapAnnotations();

  /**
   * @return true if cookies should be used instead of an HTTP session to store messages in FlashScope during a
   * redirect.
   */
  boolean useCookieForMessageFlashScope();

  /**
   * @return the cookie name used to store the user login security context cookie.
   */
  String userLoginSecurityContextCookieName();
}
