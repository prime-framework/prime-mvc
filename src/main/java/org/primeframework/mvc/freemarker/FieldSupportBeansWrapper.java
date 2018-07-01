/*
 * Copyright (c) 2012-2018, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

/**
 * This class is the default FreeMarker wrapper for Prime. It supports fields, beans, and simple Maps.
 *
 * @author Brian Pontarelli
 */
public class FieldSupportBeansWrapper extends BeansWrapper {
  /**
   * Singleton instance.
   */
  public static final BeansWrapper INSTANCE;

  static {
    BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_28);
    builder.setExposeFields(true);
    builder.setSimpleMapWrapper(true);
    INSTANCE = builder.build();
  }

  /**
   * Sets the flags on the parent class.
   *
   * @deprecated Use {@link #INSTANCE} for same behavior or {@link BeansWrapperBuilder} to create a new wrapper
   */
  @Deprecated
  public FieldSupportBeansWrapper() {
    setExposeFields(true);
    setSimpleMapWrapper(true);
  }
}
