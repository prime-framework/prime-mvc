/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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

import java.net.URI;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.RESTClient.HTTPMethod;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;

/**
 * @author Daniel DeGroff
 */
@Action
@Status
public class CallbackAction {
  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  private final HTTPRequest request;

  @Inject
  public CallbackAction(HTTPRequest request, MessageProvider messageProvider, MessageStore messageStore) {
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
    this.request = request;
  }

  public String get() {
    messageStore.add(new SimpleMessage(MessageType.ERROR, "[ERROR]", messageProvider.getMessage("[ERROR]")));
    messageStore.add(new SimpleMessage(MessageType.INFO, "[INFO]", messageProvider.getMessage("[INFO]")));
    messageStore.add(new SimpleMessage(MessageType.WARNING, "[WARNING]", messageProvider.getMessage("[WARNING]")));

    // Call another endpoint that adds messages to the message store
    URI url = URI.create(request.getBaseURL());
    ClientResponse<Void, Void> response = new RESTClient<>(Void.TYPE, Void.TYPE)
        .connectTimeout(0)
        .url(url + "/message-store")
        .method(HTTPMethod.GET)
        .go();

    return "success";
  }
}
