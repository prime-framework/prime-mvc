/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import org.example.action.nested.NestedMessageAction;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.annotation.AlternateMessageResources;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;

@Action
@Status
@AlternateMessageResources(actions = NestedMessageAction.class)
public class AlternateMessageResourcesAnnotatedAction {
  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  public String messageKey;

  @Inject
  public AlternateMessageResourcesAnnotatedAction(MessageProvider messageProvider, MessageStore messageStore) {
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  public String get() {
    try {
      messageStore.add(new SimpleMessage(MessageType.INFO, messageKey, messageProvider.getMessage(messageKey)));
    } catch (Exception e) {
      messageStore.add(new SimpleMessage(MessageType.ERROR, messageKey, "not found"));
    }
    return "success";
  }
}
