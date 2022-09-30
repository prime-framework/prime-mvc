/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.freemarker.http;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.fusionauth.http.server.HTTPContext;

/**
 * TemplateHashModel wrapper for a Context attributes.
 */
public final class ContextModel implements TemplateHashModel {
  private final HTTPContext context;

  private final ObjectWrapper wrapper;

  public ContextModel(HTTPContext context, ObjectWrapper wrapper) {
    this.context = context;
    this.wrapper = wrapper;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    return wrapper.wrap(context.getAttribute(key));
  }

  @Override
  public boolean isEmpty() {
    return context.getAttributes().isEmpty();
  }
}
