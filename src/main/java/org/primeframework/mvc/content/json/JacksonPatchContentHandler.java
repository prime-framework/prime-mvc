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
package org.primeframework.mvc.content.json;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.Patch;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.google.inject.Inject;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JSON content handler for RFC 6902 application/json-patch+json and RFC 7386 application/merge-patch+json.
 *
 * @author Daniel DeGroff
 */
public class JacksonPatchContentHandler extends BaseJacksonContentHandler {
  private static final Logger logger = LoggerFactory.getLogger(JacksonPatchContentHandler.class);

  @Inject
  public JacksonPatchContentHandler(HTTPRequest request, ActionInvocationStore store, ObjectMapper objectMapper,
                                    ExpressionEvaluator expressionEvaluator, MessageProvider messageProvider,
                                    MessageStore messageStore) {
    super(request, store, objectMapper, expressionEvaluator, messageProvider, messageStore);
  }

  @Override
  protected void handle(Object action, Object currentValue, Long contentLength, String contentType,
                        RequestMember requestMember) throws IOException, JsonPatchException {

    if (requestMember.jsonPatch == null) {
      messageStore.add(new SimpleMessage(MessageType.ERROR, "[PatchNotSupported]", messageProvider.getMessage("[PatchNotSupported]", contentType)));
      throw new ValidationException();
    }

    // When performing a PATCH there must be a current value. Ideally the user of this handler will have already validated this state.
    if (currentValue == null) {
      logger.debug("Unable to process the request using [Content-Type] value of [" + contentType + "]. Missing an existing value to patch.");
      messageStore.add(new SimpleMessage(MessageType.ERROR, "[NotFoundException]", messageProvider.getMessage("[NotFoundException]")));
      throw new ErrorException("missing");
    }

    // Build the patch from the incoming request body
    Patch patch = objectMapper.readerFor(contentType.equals("application/json-patch+json")
                                  ? JsonPatch.class
                                  : JsonMergePatch.class)
                              .readValue(request.getBody().array(), 0, contentLength.intValue());

    // Patch the current object
    JsonNode patched = patch.apply(objectMapper.valueToTree(currentValue));
    Object patchedObject = objectMapper.readerFor(requestMember.type).readValue(patched);
    expressionEvaluator.setValue(requestMember.name, action, patchedObject);
  }
}
