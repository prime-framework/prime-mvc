/*
 * Copyright (c) 2012-2023, Inversoft Inc., All Rights Reserved
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
import java.util.Set;

import io.fusionauth.http.Cookie.SameSite;
import org.primeframework.mvc.parameter.annotation.FieldUnwrapped;

/**
 * This class is an abstract implementation of the {@link MVCConfiguration} interface. It provides good default values
 * for most of the methods on that interface but leaves a few methods to be implemented by your application. To
 * accomplish this, subclass this class and implement the missing methods. Then bind your implementation into the Guice
 * injector using a Module.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractMVCConfiguration implements MVCConfiguration {
  public static final Set<String> ALLOWED_TYPES = Set.of(
      "text/plain", "text/xml", "text/rtf", "text/richtext", "text/html", "text/css",
      "image/jpeg", "image/gif", "image/png", "image/pjpeg", "image/tiff",
      "video/dv", "video/h261", "video/h262", "video/h263", "video/h264", "video/jpeg", "video/mp4", "video/mpeg", "video/mpv", "video/ogg", "video/quicktime", "video/x-flv",
      "application/octet-stream", "application/pdf", "application/msword", "application/msexcel", "application/mspowerpoint");

  public static final long MAX_SIZE = 1024000;

  public boolean autoHTMLEscapingEnabled = true;

  public String controlTemplateDirectory = "control-templates";

  public boolean csrfEnabled;

  public boolean emptyParametersAreNull = true;

  public String exceptionResultCode = "error";

  public Set<String> fileUploadAllowedTypes = ALLOWED_TYPES;

  public long fileUploadMaxSize = MAX_SIZE;

  public boolean ignoreEmptyParameters;

  public String localeCookieName = "prime-locale";

  public String messageDirectory = "messages";

  public String messageFlashScopeCookieName = "prime-mvc-msg-flash";

  public String missingPath = "/missing";

  public int savedRequestCookieMaximumSize = 16 * 1024; // 6 KB

  public String savedRequestCookieName = "prime-mvc-saved-request";

  public SameSite savedRequestSameSite = SameSite.Strict;

  public String staticDirectory = "static";

  public String templateDirectory = "templates";

  public List<Class<? extends Annotation>> unwrapAnnotations = Collections.singletonList(FieldUnwrapped.class);

  @Override
  public boolean autoHTMLEscapingEnabled() {
    return autoHTMLEscapingEnabled;
  }

  @Override
  public String controlTemplateDirectory() {
    return controlTemplateDirectory;
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
  public Set<String> fileUploadAllowedTypes() {
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
  public String messageDirectory() {
    return messageDirectory;
  }

  @Override
  public String messageFlashScopeCookieName() {
    return messageFlashScopeCookieName;
  }

  @Override
  public String missingPath() {
    return missingPath;
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
  public SameSite savedRequestCookieSameSite() {
    return savedRequestSameSite;
  }

  @Override
  public String staticDirectory() {
    return staticDirectory;
  }

  @Override
  public String templateDirectory() {
    return templateDirectory;
  }

  @Override
  public List<Class<? extends Annotation>> unwrapAnnotations() {
    return unwrapAnnotations;
  }
}
