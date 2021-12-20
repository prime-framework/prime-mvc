/*
 * Copyright (c) 2012-2016, Inversoft Inc., All Rights Reserved
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
package org.example.action;

import java.nio.file.Path;

import com.google.inject.Inject;
import org.example.domain.User;
import org.example.domain.UserField;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Binary;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.Header;
import org.primeframework.mvc.content.binary.annotation.BinaryRequest;
import org.primeframework.mvc.content.binary.annotation.BinaryResponse;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.validation.Validatable;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

/**
 * @author Brian Pontarelli
 */
@Action(value = "{name}/{value}/static/{foo}")
@Forward.List({
  @Forward(code = "forward1", page = "/WEB-INF/forward1.ftl", contentType = "text"),
  @Forward(code = "forward2", page = "/WEB-INF/forward2.ftl", contentType = "bin", status = 300, statusStr = "foo")
})
@Redirect.List({
  @Redirect(code = "redirect1", uri = "/redirect1", perm = true),
  @Redirect(code = "redirect2", uri = "/redirect2", perm = false)
})
@Status(code = "status", status = 300, statusStr = "hello world", headers = {@Header(name = "foo", value = "bar"), @Header(name = "baz", value = "fred")})
@JSON(code = "json", status = 201)
@Binary(code = "binary", status = 201)
@TestAnnotation
public class KitchenSinkAction extends KitchenSinkSuperclass implements Validatable {
  private final MessageStore messageStore;

  public Object sessionObject;

  @JSONRequest
  public UserField jsonRequest;

  @BinaryRequest
  public Path binaryRequest;

  @BinaryResponse
  public Path binaryResponse;

  @JSONResponse
  public User jsonResponse;

  @Inject
  public KitchenSinkAction(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  @Override
  @ValidationMethod
  public void validate() {
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "foo", "code", "ValidationMethod message"));
  }

  @TestAnnotation
  public String get() {
    return "success";
  }

  public String post() {
    return "success";
  }

  @PreParameterMethod
  public void preParameter() {}

  @PostParameterMethod
  public void postParameter() {}

  @PreValidationMethod
  public void preValidation() {}

  @PostValidationMethod
  public void postValidation() {}
}
