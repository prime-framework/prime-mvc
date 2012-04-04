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
package org.example.action;

import javax.validation.Validator;
import java.util.Set;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Header;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.jsr303.Validatable;

import com.google.inject.Inject;

/**
 * @author Brian Pontarelli
 */
@Action("{name}/{value}/static/{foo}")
@Forward.List({
  @Forward(code = "forward1", page = "/WEB-INF/forward1.ftl", contentType = "text"),
  @Forward(code = "forward2", page = "/WEB-INF/forward2.ftl", contentType = "bin", status = 300, statusStr = "foo")
})
@Redirect.List({
  @Redirect(code = "redirect1", uri = "/redirect1", perm = true),
  @Redirect(code = "redirect2", uri = "/redirect2", perm = false)
})
@Header(code = "header", status = 300)
public class KitchenSink extends KitchenSinkSuperclass implements Validatable {
  private final MessageStore messageStore;

  @Inject
  public KitchenSink(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  @ValidationMethod
  public void validate() {
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "foo", "ValidationMethod message"));
  }

  @Override
  public Set validate(Validator validator) {
    return null;
  }

  public String get() {
    return "success";
  }

  public String post() {
    return "success";
  }
}
