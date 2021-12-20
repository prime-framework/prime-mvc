/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.control.message;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.control.AbstractControl;
import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;

import com.google.inject.Inject;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * This class a FreeMarker method model and a control for retrieving messages.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "key", types = {String.class})
    },
    optional = {
        @ControlAttribute(name = "values", types = List.class)
    }
)
public class Message extends AbstractControl implements TemplateMethodModelEx {
  private final MessageProvider messageProvider;

  @Inject
  public Message(MessageProvider messageProvider) {
    this.messageProvider = messageProvider;
  }

  /**
   * Calls the {@link #renderStart(Writer, Map, Map)} and {@link #renderEnd(Writer)} methods using a StringWriter to
   * collect the result and the first and second parameters to the method. The first is the key and the second is the
   * bundle, which can be left out.
   *
   * @param arguments The method arguments.
   * @return The result.
   * @throws TemplateModelException If the action is null and bundle is not specified.
   */
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() < 1) {
      throw new TemplateModelException("Invalid parameters to the message method. This method takes one or more parameters like this: message(key) or message(key, values...)");
    }

    String key = arguments.get(0).toString();
    return messageProvider.getMessage(key, arguments.subList(1, arguments.size()).toArray());
  }

  /**
   * Determines the bundle and then gets the message and puts it into the attributes.
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void addAdditionalAttributes() {
    String key = (String) attributes.remove("key");
    final String defaultMesg = (String) attributes.remove("default");
    List<Object> values = (List<Object>) attributes.remove("values");
    if (values == null) {
      values = new ArrayList<>();
    }

    String message;
    try {
      message = messageProvider.getMessage(key, values.toArray());
    } catch (MissingMessageException e) {
      message = defaultMesg;
    }

    if (message == null) {
      throw new PrimeException("The message for the key [" + key + "] is missing and there was no default set using the [default] attribute.");
    }

    attributes.put("message", message);
  }

  @Override
  protected String startTemplateName() {
    return null;
  }

  @Override
  protected String endTemplateName() {
    return "message.ftl";
  }
}