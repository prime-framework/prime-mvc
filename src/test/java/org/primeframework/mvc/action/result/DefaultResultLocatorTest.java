/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import java.net.URL;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.http.HTTPContext;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * Tests the result locator.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultLocatorTest extends PrimeBaseTest {
  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void locate() throws Exception {
    ActionInvocation ai = new ActionInvocation(null, null, "/action", "js", null);

    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    ResultStore rs = createStrictMock(ResultStore.class);
    expect(rs.get()).andReturn("failure");
    replay(rs);

    HTTPContext context = createStrictMock(HTTPContext.class);
    expect(context.getResource("/WEB-INF/templates/action-js-failure.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/action-js.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/action-failure.ftl")).andReturn(null);
    expect(context.getResource("/WEB-INF/templates/action.ftl")).andReturn(new URL("http://localhost"));
    replay(context);


    DefaultResourceLocator locator = new DefaultResourceLocator(ais, rs, context);
    String result = locator.locate("/WEB-INF/templates");
    assertEquals(result, "/WEB-INF/templates/action.ftl");

    verify(ais, rs, context);
  }
}
