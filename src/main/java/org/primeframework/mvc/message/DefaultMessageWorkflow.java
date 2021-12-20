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
package org.primeframework.mvc.message;

import java.io.IOException;
import java.util.List;

import com.google.inject.Inject;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.workflow.WorkflowChain;

/**
 * This is the default message workflow implementation. It removes all flash messages from the session and places them
 * in the request.
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageWorkflow implements MessageWorkflow {
  private final FlashScope flashScope;

  private final MessageStore messageStore;

  @Inject
  public DefaultMessageWorkflow(FlashScope flashScope, MessageStore messageStore) {
    this.flashScope = flashScope;
    this.messageStore = messageStore;
  }

  /**
   * {@inheritDoc}
   */
  public void perform(WorkflowChain chain) throws IOException {
    List<Message> messages = flashScope.get();
    flashScope.clear();
    messageStore.addAll(MessageScope.REQUEST, messages);
    chain.continueWorkflow();
  }
}