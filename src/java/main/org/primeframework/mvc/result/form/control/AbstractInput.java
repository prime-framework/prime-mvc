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
package org.primeframework.mvc.result.form.control;

import java.util.Map;

import org.primeframework.mvc.l10n.MessageProvider;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageType;
import org.primeframework.mvc.result.control.AbstractControl;

import com.google.inject.Inject;

/**
 * <p> This class is an abstract control implementation for input tags. In addition to the abstract control, it also
 * provides support for labels. </p>
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractInput extends AbstractControl {
  private final boolean labeled;
  protected MessageStore messageStore;
  protected MessageProvider messageProvider;

  protected AbstractInput(boolean labeled) {
    this.labeled = labeled;
  }

  /**
   * Sets the message services, which are used to lookup the value from the inputs label and fetch field messages.
   *
   * @param messageProvider The message provider.
   * @param messageStore    The message store.
   */
  @Inject
  public void setMessageProvider(MessageProvider messageProvider, MessageStore messageStore) {
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  /**
   * Adds a default ID if one doesn't exist.
   */
  protected void addAdditionalAttributes() {
    String id = (String) attributes.get("id");
    if (id == null) {
      id = makeID((String) attributes.get("name"));
      attributes.put("id", id);
    }
  }

  /**
   * Converts the name attribute to an ID.
   *
   * @param name The name attribute.
   * @return The ID.
   */
  protected String makeID(String name) {
    return name.replace('.', '_');
  }

  /**
   * Overrides the parameter map cration from the AbstractControl and adds the label for input tags. This also moves the
   * <code>labelposition</code> attribute from the tag to the returned Map (removes it from the attributes).
   *
   * @return The parameter map.
   */
  @Override
  protected Map<String, Object> makeParameters() {
    Map<String, Object> map = super.makeParameters();
    if (labeled) {
      String name = (String) attributes.get("name");
      String bundleName = determineBundleName(attributes);
      if (bundleName == null) {
        throw new IllegalStateException("Unable to locate the label message for the field named [" +
          name + "]. If you don't have an action class for the URL, you define the bundle to " +
          "use to localize the form. This bundle is specified either on the control tag or the " +
          "form tag.");
      }

      String labelKey = (String) attributes.remove("labelKey");
      String label = null;
      if (labelKey != null) {
        label = messageProvider.getMessage(bundleName, labelKey, locale, dynamicAttributes);
      }

      if (label == null) {
        label = messageProvider.getMessage(bundleName, name, locale, dynamicAttributes);
      }

      if (label == null) {
        throw new IllegalStateException("Missing localized label for the field named [" +
          name + "]. You must define the label in the resource bundle under under the " +
          "key [" + name + "], which is the name of the field, or using the [labelKey] " +
          "attribute " + (labelKey != null ? "(which is currently set to [" + labelKey + "] " +
          "but there is no label in for that key in the resource bundle) " : "") + "to " +
          "specify an alternate key into the resource bundle.");
      }

      map.put("label", label);

      // Add the field messages and errors as a list or null
      map.put("field_messages", messageStore.getFieldMessages(MessageType.PLAIN).get(name));
      map.put("field_errors", messageStore.getFieldMessages(MessageType.ERROR).get(name));

      // Remove the required attribute and move it up
      map.put("required", attributes.remove("required"));
    }

    return map;
  }

  /**
   * @return Always null since the input tags don't have a start.
   */
  @Override
  protected String startTemplateName() {
    return null;
  }
}