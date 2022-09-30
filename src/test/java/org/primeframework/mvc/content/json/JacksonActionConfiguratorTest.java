/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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

import java.util.HashMap;
import java.util.Map;

import io.fusionauth.http.HTTPMethod;
import org.example.action.KitchenSinkAction;
import org.example.domain.UserField;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the jackson configurator test.
 *
 * @author Brian Pontarelli
 */
public class JacksonActionConfiguratorTest extends PrimeBaseTest {
  @Test
  public void configure() {
    JacksonActionConfiguration config = (JacksonActionConfiguration) new JacksonActionConfigurator().configure(KitchenSinkAction.class);

    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.POST, new RequestMember("jsonRequest", UserField.class));
    requestMembers.put(HTTPMethod.PUT, new RequestMember("jsonRequest", UserField.class));
    requestMembers.put(HTTPMethod.DELETE, new RequestMember("jsonRequest", UserField.class));
    requestMembers.put(HTTPMethod.GET, new RequestMember("jsonRequest", UserField.class));
    requestMembers.put(HTTPMethod.PATCH, new RequestMember("jsonRequest", UserField.class));

    assertEquals(config.requestMembers.size(), requestMembers.size());
    assertTrue(config.requestMembers.keySet().containsAll(requestMembers.keySet()));
    assertEquals(config.requestMembers.get(HTTPMethod.POST).name, requestMembers.get(HTTPMethod.POST).name);
    assertEquals(config.requestMembers.get(HTTPMethod.PUT).name, requestMembers.get(HTTPMethod.PUT).name);
    assertEquals(config.requestMembers.get(HTTPMethod.GET).name, requestMembers.get(HTTPMethod.GET).name);
    assertEquals(config.requestMembers.get(HTTPMethod.DELETE).name, requestMembers.get(HTTPMethod.DELETE).name);
    assertEquals(config.requestMembers.get(HTTPMethod.PATCH).name, requestMembers.get(HTTPMethod.PATCH).name);

    assertEquals(config.requestMembers.get(HTTPMethod.POST).type, requestMembers.get(HTTPMethod.POST).type);
    assertEquals(config.requestMembers.get(HTTPMethod.PUT).type, requestMembers.get(HTTPMethod.PUT).type);
    assertEquals(config.requestMembers.get(HTTPMethod.GET).type, requestMembers.get(HTTPMethod.GET).type);
    assertEquals(config.requestMembers.get(HTTPMethod.DELETE).type, requestMembers.get(HTTPMethod.DELETE).type);
    assertEquals(config.requestMembers.get(HTTPMethod.PATCH).type, requestMembers.get(HTTPMethod.PATCH).type);

    assertEquals(config.responseMember.name, "jsonResponse");
  }
}