/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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

/**
 * This class is an abstract implementation of the {@link MVCConfiguration} interface. It provides good default
 * values for most of the methods on that interface but leaves a few methods to be implemented by your application. To
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

  @Override
  public String[] fileUploadAllowedTypes() {
    return ALLOWED_TYPES;
  }

  @Override
  public long fileUploadMaxSize() {
    return MAX_SIZE;
  }

  @Override
  public boolean staticResourcesEnabled() {
    return true;
  }

  @Override
  public String[] staticResourcePrefixes() {
    return STATIC_PREFIXES;
  }

  @Override
  public boolean propagateRuntimeExceptions() {
    return true;
  }

  @Override
  public boolean ignoreEmptyParameters() {
    return false;
  }

  @Override
  public boolean emptyParametersAreNull() {
    return true;
  }

  @Override
  public String exceptionResultCode() {
    return "error";
  }
}
