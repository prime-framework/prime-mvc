/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.primeframework.mvc.message.scope.MessageScope;

/**
 * Used for testing to assert on messages.
 *
 * @author Brian Pontarelli
 */
public class TestMessageObserver implements MessageObserver {
  public final Map<MessageScope, List<Message>> messages = new HashMap<>();

  @Override
  public void added(MessageScope scope, Message message) {
    messages.computeIfAbsent(scope, key -> new ArrayList<>()).add(message);
  }

  public void clear() {
    messages.clear();
  }

  @Override
  public void cleared(MessageScope scope) {
    messages.remove(scope);
  }

  public Map<String, List<FieldMessage>> getFieldMessages() {
    List<FieldMessage> fieldMessages = messages.values()
                                               .stream()
                                               .flatMap(Collection::stream)
                                               .filter(m -> m instanceof FieldMessage)
                                               .map(m -> (FieldMessage) m)
                                               .collect(Collectors.toList());
    Map<String, List<FieldMessage>> result = new HashMap<>();
    for (FieldMessage fieldMessage : fieldMessages) {
      result.computeIfAbsent(fieldMessage.getField(), key -> new ArrayList<>()).add(fieldMessage);
    }

    return result;
  }

  public List<Message> getGeneralMessages() {
    return messages.values()
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(m -> !(m instanceof FieldMessage))
                   .collect(Collectors.toList());
  }
}
