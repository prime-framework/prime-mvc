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
package org.primeframework.mvc.result.message.control;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.result.control.AbstractControl;
import org.primeframework.mvc.result.control.annotation.ControlAttribute;
import org.primeframework.mvc.result.control.annotation.ControlAttributes;

import com.google.inject.Inject;

/**
 * <p> This class is the control that outputs the field messages. </p>
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
  required = {
    @ControlAttribute(name = "errors", types = {boolean.class, Boolean.class})
  }
)
public class FieldMessages extends AbstractControl {
  private final MessageStore messageStore;

  @Inject
  public FieldMessages(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  /**
   * Adds the field messages.
   *
   * @return The parameters.
   */
  @Override
  protected Map<String, Object> makeParameters() {
    Map<String, Object> parameters = super.makeParameters();
    parameters.put("field_messages", trim(messageStore.getFieldMessages(MessageType.PLAIN), (String) attributes.get("fields")));
    parameters.put("field_errors", trim(messageStore.getFieldMessages(MessageType.ERROR), (String) attributes.get("fields")));
    return parameters;
  }

  /**
   * If the fields parameter is null, this does not change the field messages map. If it is not null, this splits the
   * fields by commas and removes all the fields that aren't in the list.
   *
   * @param fieldMessages The field messages to trim.
   * @param fields        The list of fields to display.
   * @return The result.
   */
  protected Map<String, List<String>> trim(Map<String, List<String>> fieldMessages, String fields) {
    if (fields == null) {
      return fieldMessages;
    }

    String[] names = fields.split(",");
    Set<String> set = new HashSet<String>();
    for (String name : names) {
      set.add(name.trim());
    }

    fieldMessages.keySet().retainAll(set);
    return fieldMessages;
  }

  /**
   * @return Null.
   */
  protected String startTemplateName() {
    return null;
  }

  /**
   * @return The actionmessages.ftl.
   */
  protected String endTemplateName() {
    return "field-messages.ftl";
  }
}