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

package org.primeframework.config;

/**
 * The main PrimeMVCConfiguration interface for JCatapult. This configuration interface is preferred over the Apache
 * Commons configuration because it is read only and it is also JCatapult specific and easy to Mock, override,
 * implement, extend, etc.
 *
 * @author Brian Pontarelli
 */
public interface PrimeMVCConfiguration {
  /**
   * @return The number of seconds to check for Freemarker template updates (max integer means never and 0 means always).
   */
  int freemarkerCheckSeconds();

  /**
   * @return The number of seconds to check for message bundles updates (max integer means never and 0 means always).
   */
  int l10nReloadSeconds();

  /**
   * @return The types of files that are allowed to be uploaded.
   */
  String[] fileUploadAllowedTypes();

  /**
   * @return
   */
  long fileUploadMaxSize();
  
  boolean staticResourcesEnabled();

  String[] staticResourcePrefixes();

  boolean propagateRuntimeExceptions();

  boolean allowUnknownParameters();
}
