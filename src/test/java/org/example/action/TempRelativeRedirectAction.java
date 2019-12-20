/*
 * Copyright (c) 2018-2019, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;

/**
 * @author Daniel DeGroff
 */
@Action
@Redirect(code = "redirect", uri = "temp-redirect-target")
public class TempRelativeRedirectAction {
  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  @Inject
  public TempRelativeRedirectAction(MessageProvider messageProvider, MessageStore messageStore) {
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  public String get() {
    messageStore.add(new SimpleMessage(MessageType.ERROR, "[ERROR]", messageProvider.getMessage("[ERROR]")));
    messageStore.add(new SimpleMessage(MessageType.INFO, "[INFO]", messageProvider.getMessage("[INFO]")));
    messageStore.add(new SimpleMessage(MessageType.WARNING, "[WARNING]", messageProvider.getMessage("[WARNING]")));
    return "redirect";
  }
}
