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
import freemarker.template.ObjectWrapperAndUnwrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;

/**
 * TemplateHashModel wrapper for a HTTPRequest attributes.
 */
public final class HTTPRequestModel implements TemplateHashModelEx {
  private final HTTPRequest request;

  private final HTTPResponse response;

  private final ObjectWrapper wrapper;

  /**
   * @param wrapper Should be an {@link ObjectWrapperAndUnwrapper}, or else some features might won't work properly.
   *                (It's declared as {@link ObjectWrapper} only for backward compatibility.)
   */
  public HTTPRequestModel(HTTPRequest request, ObjectWrapper wrapper) {
    this(request, null, wrapper);
  }

  public HTTPRequestModel(HTTPRequest request, HTTPResponse response, ObjectWrapper wrapper) {
    this.request = request;
    this.response = response;
    this.wrapper = wrapper;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    return wrapper.wrap(request.getAttribute(key));
  }

  public ObjectWrapper getObjectWrapper() {
    return wrapper;
  }

  public HTTPRequest getRequest() {
    return request;
  }

  public HTTPResponse getResponse() {
    return response;
  }

  @Override
  public boolean isEmpty() {
    return request.getAttributes().isEmpty();
  }

  @Override
  public TemplateCollectionModel keys() {
    return new SimpleCollection(request.getAttributes().keySet(), wrapper);
  }

  @Override
  public int size() {
    return request.getAttributes().size();
  }

  @Override
  public TemplateCollectionModel values() {
    return new SimpleCollection(request.getAttributes().values(), wrapper);
  }
}
