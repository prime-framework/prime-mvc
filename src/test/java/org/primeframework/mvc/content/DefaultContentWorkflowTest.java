/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content;

import com.google.inject.Inject;
import org.example.action.KitchenSink;
import org.example.domain.UserField;
import org.example.domain.UserType;
import org.primeframework.mock.servlet.MockServletInputStream;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.guice.ContentHandlerFactory;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the content workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultContentWorkflowTest extends PrimeBaseTest {
  @Inject public ActionInvocationStore store;

  @Test
  public void missing() throws IOException, ServletException {
    request.setContentType("application/missing");

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    new DefaultContentWorkflow(request, new ContentHandlerFactory(injector)).perform(chain);

    verify(chain);
  }

  @Test
  public void callJSON() throws IOException, ServletException {
    String expected = "{" +
        "  \"active\":true," +
        "  \"addresses\":{" +
        "    \"home\":{" +
        "      \"city\":\"Broomfield\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80023\"" +
        "    }," +
        "    \"work\":{" +
        "      \"city\":\"Denver\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80202\"" +
        "    }" +
        "  }," +
        "  \"age\":37," +
        "  \"favoriteMonth\":5," +
        "  \"favoriteYear\":1976," +
        "  \"ids\":{" +
        "    \"0\":1," +
        "    \"1\":2" +
        "  }," +
        "  \"lifeStory\":\"Hello world\"," +
        "  \"securityQuestions\":[\"one\",\"two\",\"three\",\"four\"]," +
        "  \"siblings\":[{" +
        "    \"active\":false," +
        "    \"name\":\"Brett\"" +
        "  },{" +
        "    \"active\":false," +
        "    \"name\":\"Beth\"" +
        "  }]," +
        "  \"type\":\"COOL\"" +
        "}";

    request.setInputStream(new MockServletInputStream(expected.getBytes()));
    request.setContentType("application/json");

    Map<Class<?>, Object> additionalConfig = new HashMap<Class<?>, Object>();
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration("jsonRequest", UserField.class, null, null));

    KitchenSink action = new KitchenSink(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSink.class, null, null, null, null, null, null, null, null, null, null, null, null, additionalConfig, null);
    store.setCurrent(new ActionInvocation(action, null, null, null, config));

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    new DefaultContentWorkflow(request, new ContentHandlerFactory(injector)).perform(chain);

    assertEquals(action.jsonRequest.addresses.get("work").city, "Denver");
    assertEquals(action.jsonRequest.addresses.get("work").state, "Colorado");
    assertEquals(action.jsonRequest.addresses.get("work").zipcode, "80202");
    assertEquals(action.jsonRequest.addresses.get("home").city, "Broomfield");
    assertEquals(action.jsonRequest.addresses.get("home").state, "Colorado");
    assertEquals(action.jsonRequest.addresses.get("home").zipcode, "80023");
    assertTrue(action.jsonRequest.active);
    assertEquals((int) action.jsonRequest.age, 37);
    assertEquals((int) action.jsonRequest.favoriteMonth, 5);
    assertEquals((int) action.jsonRequest.favoriteYear, 1976);
    assertEquals((int) action.jsonRequest.ids.get(0), 1);
    assertEquals((int) action.jsonRequest.ids.get(1), 2);
    assertEquals(action.jsonRequest.lifeStory, "Hello world");
    assertEquals(action.jsonRequest.securityQuestions, new String[]{"one", "two", "three", "four"});
    assertEquals(action.jsonRequest.siblings.get(0).name, "Brett");
    assertEquals(action.jsonRequest.siblings.get(1).name, "Beth");
    assertEquals(action.jsonRequest.type, UserType.COOL);
  }
}
