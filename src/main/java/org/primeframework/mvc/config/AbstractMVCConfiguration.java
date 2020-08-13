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
import java.util.Collections;
import java.util.List;

import org.primeframework.mvc.parameter.annotation.FieldUnwrapped;


/**
 * This class is an abstract implementation of the {@link MVCConfiguration} interface. It provides good default values
 * for most of the methods on that interface but leaves a few methods to be implemented by your application. To
 * accomplish this, sub-class this class and implement the missing methods. Then bind your implementation into the Guice
 * injector using a Module.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractMVCConfiguration implements MVCConfiguration {
  public static final String[] ALLOWED_TYPES = new String[]{
      "text/plain", "text/xml", "text/rtf", "text/richtext", "text/html", "text/css",
      "image/jpeg", "image/gif", "image/png", "image/pjpeg", "image/tiff",
      "video/dv", "video/h261", "video/h262", "video/h263", "video/h264", "video/jpeg", "video/mp4", "video/mpeg", "video/mpv", "video/ogg", "video/quicktime", "video/x-flv",
      "application/msword", "application/pdf", "application/msword", "application/msexcel", "application/mspowerpoint"};

  public static final long MAX_SIZE = 1024000;

  public static final String[] STATIC_PREFIXES = new String[]{"/static"};

  public boolean autoHTMLEscapingEnabled = true;

  public boolean csrfEnabled = false;

  public boolean emptyParametersAreNull = true;

  public String exceptionResultCode = "error";

  public String[] fileUploadAllowedTypes = ALLOWED_TYPES;

  public long fileUploadMaxSize = MAX_SIZE;

  public String userLoginSecurityContextCookieName = "prime-session";

  public boolean ignoreEmptyParameters = false;

  public String localeCookieName = "prime-locale";

  public String messageFlashScopeCookieName = "prime-mvc-msg-flash";

  public boolean propagateRuntimeException = true;

  public String resourceDirectory = "/WEB-INF";

  public int savedRequestCookieMaximumSize = 6 * 1024; // 6 KB

  public String savedRequestCookieName = "prime-mvc-saved-request";

  public String[] staticResourcePrefixes = STATIC_PREFIXES;

  public boolean staticResourcesEnabled = true;

  public List<Class<? extends Annotation>> unwrapAnnotations = Collections.singletonList(FieldUnwrapped.class);

  public boolean useCookieForFlashScope;

  @Override
  public boolean autoHTMLEscapingEnabled() {
    return autoHTMLEscapingEnabled;
  }

  @Override
  public boolean csrfEnabled() {
    return csrfEnabled;
  }

  @Override
  public boolean emptyParametersAreNull() {
    return emptyParametersAreNull;
  }

  @Override
  public String exceptionResultCode() {
    return exceptionResultCode;
  }

  @Override
  public String[] fileUploadAllowedTypes() {
    return fileUploadAllowedTypes;
  }

  @Override
  public long fileUploadMaxSize() {
    return fileUploadMaxSize;
  }

  @Override
  public boolean ignoreEmptyParameters() {
    return ignoreEmptyParameters;
  }

  @Override
  public String localeCookieName() {
    return localeCookieName;
  }

  @Override
  public String messageFlashScopeCookieName() {
    return messageFlashScopeCookieName;
  }

  @Override
  public boolean propagateRuntimeExceptions() {
    return propagateRuntimeException;
  }

  @Override
  public String resourceDirectory() {
    return resourceDirectory;
  }

  @Override
  public int savedRequestCookieMaximumSize() {
    return savedRequestCookieMaximumSize;
  }

  @Override
  public String savedRequestCookieName() {
    return savedRequestCookieName;
  }

  @Override
  public String[] staticResourcePrefixes() {
    return staticResourcePrefixes;
  }

  @Override
  public boolean staticResourcesEnabled() {
    return staticResourcesEnabled;
  }

  @Override
  public List<Class<? extends Annotation>> unwrapAnnotations() {
    return unwrapAnnotations;
  }

  @Override
  public boolean useCookieForMessageFlashScope() {
    return useCookieForFlashScope;
  }

  @Override
  public String userLoginSecurityContextCookieName() {
    return userLoginSecurityContextCookieName;
  }
}
