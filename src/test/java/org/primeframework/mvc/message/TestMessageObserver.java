/*
 * Copyright (c) 2021-2023, Inversoft Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.message.scope.MessageScope;

/**
 * Used for testing to assert on messages.
 *
 * @author Brian Pontarelli
 */
public class TestMessageObserver implements MessageObserver {
  public final static String ObserverMessageStoreId = "X-Observer-MessageStoreId";

  public final Map<String, Map<MessageScope, List<Message>>> messages = new ConcurrentHashMap<>();

  private String messageStoreId;

  public Map<String, List<FieldMessage>> getFieldMessages() {
    if (messageStoreId == null || !messages.containsKey(messageStoreId)) {
      return Collections.emptyMap();
    }

    List<FieldMessage> fieldMessages = messages.get(messageStoreId)
                                               .values()
                                               .stream()
                                               .flatMap(Collection::stream)
                                               .filter(m -> m instanceof FieldMessage)
                                               .map(m -> (FieldMessage) m)
                                               .toList();

    Map<String, List<FieldMessage>> result = new HashMap<>();
    for (FieldMessage fieldMessage : fieldMessages) {
      result.computeIfAbsent(fieldMessage.getField(), key -> new ArrayList<>()).add(fieldMessage);
    }

    return result;
  }

  public List<Message> getGeneralMessages() {
    if (messageStoreId == null || !messages.containsKey(messageStoreId)) {
      return Collections.emptyList();
    }

    return messages.get(messageStoreId)
                   .values()
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(m -> !(m instanceof FieldMessage))
                   .collect(Collectors.toList());
  }

  @Override
  public void messageAdded(HTTPRequest httpRequest, MessageScope scope, Message message) {
    String messageStoreId = httpRequest.getHeader(ObserverMessageStoreId);
    if (messageStoreId != null) {
      messages.computeIfAbsent(messageStoreId, key -> new HashMap<>())
              .computeIfAbsent(scope, key -> new ArrayList<>()).add(message);
    }
  }

  @Override
  public void reset() {
    messages.clear();
  }

  @Override
  public void scopeCleared(HTTPRequest httpRequest, MessageScope scope) {
    String messageStoreId = httpRequest.getHeader(ObserverMessageStoreId);
    if (messageStoreId != null) {
      if (messages.containsKey(messageStoreId)) {
        messages.get(messageStoreId).remove(scope);
      }
    }
  }

  public void setMessageStoreId(String messageStoreId) {
    this.messageStoreId = messageStoreId;
  }
}
