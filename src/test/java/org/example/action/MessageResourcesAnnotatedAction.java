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
import org.primeframework.mvc.action.annotation.MessageResources;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;

@Action
@Status
@MessageResources(fallback = NestedMessageAction.class)
public class MessageResourcesAnnotatedAction {
  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  @Inject
  public MessageResourcesAnnotatedAction(MessageProvider messageProvider, MessageStore messageStore) {
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  public String get() {
    try {
      // this message exists in a standard path by convention - src/test/web/messages/message-resources-annotated.properties
      messageStore.add(new SimpleMessage(MessageType.INFO, "normal_message", messageProvider.getMessage("normal_message")));
      // this message only exists in NestedMessageAction's path - in src/test/web/messages/nested/nested-message.properties
      messageStore.add(new SimpleMessage(MessageType.INFO, "nested_message", messageProvider.getMessage("nested_message")));
    } catch (MissingMessageException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return "success";
  }
}
