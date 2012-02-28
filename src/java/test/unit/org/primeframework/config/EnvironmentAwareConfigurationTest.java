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
package org.primeframework.config;

import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import org.primeframework.container.ServletContainerResolver;
import org.primeframework.environment.EnvironmentResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p>
 * This tests the environment aware configuration.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class EnvironmentAwareConfigurationTest {
    @Test
    public void testEnvironmentAwareConfiguration() throws Exception {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-development.xml")).
                andReturn("src/java/test/unit/org/jcatapult/config/config-development.xml");
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-default.xml")).
                andReturn("src/java/test/unit/org/jcatapult/config/config-default.xml");
        EasyMock.replay(context);

        EnvironmentAwareConfiguration config = new EnvironmentAwareConfiguration(new EnvironmentResolver() {
            public String getEnvironment() {
                return "development";
            }
        },
                new ServletContainerResolver(context), "/WEB-INF/config");
        assertEquals("dev-value", config.getString("dev"));
        assertEquals("default-value", config.getString("default"));
        EasyMock.verify(context);
    }

    @Test
    public void testOverride() throws Exception {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-development.xml")).
                andReturn("src/java/test/unit/org/jcatapult/config/config-development.xml");
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-default.xml")).
                andReturn("src/java/test/unit/org/jcatapult/config/config-default.xml");
        EasyMock.replay(context);

        EnvironmentAwareConfiguration config = new EnvironmentAwareConfiguration(new EnvironmentResolver() {
            public String getEnvironment() {
                return "development";
            }
        },
                new ServletContainerResolver(context), "/WEB-INF/config");
        String overrideValue = config.getString("override");
        Assert.assertNotNull(overrideValue);
        Assert.assertEquals("Development", overrideValue);
        EasyMock.verify(context);
    }

    @Test
    public void testFallThrough() throws Exception {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-production.xml")).
                andReturn("src/java/test/unit/org/jcatapult/config/config-production.xml");
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-default.xml")).
                andReturn("src/java/test/unit/org/jcatapult/config/config-default.xml");
        EasyMock.replay(context);

        EnvironmentAwareConfiguration config = new EnvironmentAwareConfiguration(new EnvironmentResolver() {
            public String getEnvironment() {
                return "production";
            }
        },
                new ServletContainerResolver(context), "/WEB-INF/config");
        String defaultValue = config.getString("override");
        Assert.assertNotNull(defaultValue);
        Assert.assertEquals("Default", defaultValue);
        EasyMock.verify(context);
    }

    @Test
    public void testMissingFile() throws Exception {
        ServletContext context = EasyMock.createStrictMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-production.xml")).
                andReturn(null);
        EasyMock.expect(context.getRealPath("/WEB-INF/config/config-default.xml")).
                andReturn("src/java/test/unit/org/jcatapult/config/config-default.xml");
        EasyMock.replay(context);

        EnvironmentAwareConfiguration config = new EnvironmentAwareConfiguration(new EnvironmentResolver() {
            public String getEnvironment() {
                return "production";
            }
        },
                new ServletContainerResolver(context), "/WEB-INF/config");
        String defaultValue = config.getString("override");
        Assert.assertNotNull(defaultValue);
        Assert.assertEquals("Default", defaultValue);
        EasyMock.verify(context);
    }
}