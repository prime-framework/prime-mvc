/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.action.config;

import java.util.Map;
import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.example.action.Simple;
import org.example.action.user.Index;
import org.jcatapult.mvc.util.DefaultURIBuilder;
import org.jcatapult.test.Capture;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>
 * This class tests the default action configuration provider.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultActionConfigurationProviderTest {
    @Test
    public void testConfigure() {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        Capture c = new Capture();
        context.setAttribute(eq(DefaultActionConfigurationProvider.ACTION_CONFIGURATION_KEY), c.capture());
        EasyMock.replay(context);

        new DefaultActionConfigurationProvider(context, new DefaultURIBuilder());

        Map<String, ActionConfiguration> config = (Map<String, ActionConfiguration>) c.object;
        assertNotNull(config.get("/simple"));
        assertSame(Simple.class, config.get("/simple").actionClass());
        assertEquals("/simple", config.get("/simple").uri());

        assertNotNull(config.get("/user/index"));
        assertSame(Index.class, config.get("/user/index").actionClass());
        assertEquals("/user/index", config.get("/user/index").uri());
    }

}