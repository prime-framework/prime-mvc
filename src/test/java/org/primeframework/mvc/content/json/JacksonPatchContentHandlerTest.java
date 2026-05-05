/*
 * Copyright (c) 2026, Inversoft Inc., All Rights Reserved
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

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import org.example.action.patch.PatchActionRequest;
import org.example.action.patch.PatchActionRequest.CoolObject;
import org.example.action.patch.TestAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import com.google.inject.Inject;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Focused handler-level regression tests for PATCH requests when the action has a final @JSONRequest field.
 *
 * We intentionally bypass full action scanning and construct only the configuration needed for this handler path.
 */
public class JacksonPatchContentHandlerTest extends PrimeBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  @Test
  public void json_patch_remove_nulls_removed_field_on_final_request_object() throws Exception {
    FinalPatchAction action = new FinalPatchAction();
    PatchActionRequest originalRequest = action.request;
    action.request.data = new CoolObject();
    action.request.data.email = "jim@example.com";
    action.request.data.name = "Jim Bob";

    ActionInvocationStore store = buildStore(action, patchRequestMember());
    HTTPRequest request = patchRequest("""
        [
          {
            "op": "remove",
            "path": "/data/email"
          }
        ]
        """, "application/json-patch+json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageProvider, messageStore);

    JacksonPatchContentHandler handler = new JacksonPatchContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    handler.handle();

    // Confirms the final request field was not reassigned.
    assertSame(action.request, originalRequest);
    assertNull(action.request.data.email);
    assertEquals(action.request.data.name, "Jim Bob");

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void merge_patch_null_removes_field_on_final_request_object() throws Exception {
    FinalPatchAction action = new FinalPatchAction();
    PatchActionRequest originalRequest = action.request;
    action.request.data = new CoolObject();
    action.request.data.email = "jim@example.com";
    action.request.data.name = "Jim Bob";

    ActionInvocationStore store = buildStore(action, patchRequestMember());
    HTTPRequest request = patchRequest("""
        {
          "data": {
            "email": null
          }
        }
        """, "application/merge-patch+json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageProvider, messageStore);

    JacksonPatchContentHandler handler = new JacksonPatchContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    handler.handle();

    // Confirms the final request field was not reassigned.
    assertSame(action.request, originalRequest);
    assertNull(action.request.data.email);
    assertEquals(action.request.data.name, "Jim Bob");

    verify(store, messageProvider, messageStore);
  }

  private ActionInvocationStore buildStore(Object action, RequestMember requestMember) {
    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.PATCH, requestMember);

    Map<Class<?>, Object> additionalConfig = new HashMap<>();
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(requestMembers, null, null));

    // Use TestAction for ActionConfiguration metadata so @Action and execute-method requirements are satisfied.
    // The ActionInvocation still uses the local FinalPatchAction instance to exercise final-field behavior.
    ActionConfiguration configuration = new ActionConfiguration(TestAction.class,
                                                                false,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                Collections.emptyList(),
                                                                null,
                                                                additionalConfig,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.PATCH, null, null), "/patch/test", null, configuration));
    replay(store);
    return store;
  }

  private HTTPRequest patchRequest(String body, String contentType) {
    byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(bytes));
    request.setContentLength((long) bytes.length);
    request.setContentType(contentType);
    return request;
  }

  private RequestMember patchRequestMember() {
    // Stub annotation instance to enable PATCH handling for this request member.
    return new RequestMember("request", PatchActionRequest.class, new TestJSONPatch());
  }

  // Must be public static to allow reflective field access during expression evaluation.
  // Action with final request field.
  public static class FinalPatchAction {
    public final PatchActionRequest request = new PatchActionRequest();
  }

  // Needed to provide a @JSONPatch annotation instance for the request member configuration.
  private static class TestJSONPatch implements JSONPatch {
    @Override
    public Class<? extends Annotation> annotationType() {
      return JSONPatch.class;
    }
  }
}
