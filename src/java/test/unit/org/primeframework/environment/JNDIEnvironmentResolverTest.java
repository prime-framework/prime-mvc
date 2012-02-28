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
package org.primeframework.environment;

import javax.naming.InitialContext;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import net.java.naming.MockJNDI;

/**
 * <p>
 * This tests the environment resolver for JNDI trees.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class JNDIEnvironmentResolverTest {
    @Test
    public void testGetEnvironment() throws Exception {
        MockJNDI jndi = new MockJNDI();
        Environment env = new Environment();
        env.setEnvironment("development");
        jndi.bind("java:comp/env/environment", env);
        jndi.activate();
        JNDIEnvironmentResolver resolver = new JNDIEnvironmentResolver();
        assertEquals("development", resolver.getEnvironment());
    }

    @Test
    public void testString() throws Exception {
        InitialContext context = new InitialContext();
        context.unbind("java:comp/env/environment");
        context.bind("java:comp/env/environment", "development");
        JNDIEnvironmentResolver resolver = new JNDIEnvironmentResolver();
        assertEquals("development", resolver.getEnvironment());
    }
}