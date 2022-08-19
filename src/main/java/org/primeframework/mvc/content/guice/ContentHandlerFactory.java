/*
 * Copyright (c) 2012-2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.guice;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.primeframework.mvc.content.ContentHandler;

/**
 * Builds {@link ContentHandler}s on demand.
 *
 * @author Brian Pontarelli
 */
public class ContentHandlerFactory {
  private static final Map<String, Class<? extends ContentHandler>> bindings = new HashMap<>();

  private final Injector injector;

  @Inject
  public ContentHandlerFactory(Injector injector) {
    this.injector = injector;
  }

  /**
   * Adds a binding to a ContentHandler for a specific Content-Type.
   *
   * @param binder      The binder to add a hard binding for the handler.
   * @param contentType The content type.
   * @param handlerType The handler type.
   */
  public static void addContentHandler(Binder binder, String contentType, Class<? extends ContentHandler> handlerType) {
    binder.bind(handlerType);
    bindings.put(contentType, handlerType);
  }

  /**
   * Builds a ContentHandler.
   *
   * @param contentType The content type.
   * @return The ContentHandler or null.
   */
  public ContentHandler build(String contentType) {
    Class<? extends ContentHandler> handlerType = bindings.get(contentType);
    if (handlerType == null && contentType != null) {
      handlerType = bindings.get(contentType);
    }

    if (handlerType == null) {
      return null;
    }

    return injector.getInstance(handlerType);
  }
}
