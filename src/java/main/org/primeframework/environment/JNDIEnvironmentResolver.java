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
import javax.naming.NamingException;

import com.google.inject.Singleton;

/**
 * <p>
 * This class looks in the JNDI tree to locate an instance of the
 * class {@link Environment}, which is a simple POJO that contains
 * the current environment name. The name that this Object is stored
 * under is:
 * </p>
 *
 * <pre>
 * java:comp/env/environment
 * </pre>
 *
 * @author  Brian Pontarelli
 */
@Singleton
public class JNDIEnvironmentResolver implements EnvironmentResolver {
    public String getEnvironment() {
        try {
            return new InitialContext().lookup("java:comp/env/environment").toString();
        } catch (NamingException e) {
            // Couldn't find it
            throw new IllegalStateException("Unable to locate the current environment in the JNDI tree." +
                " You need to ensure that an instance of the Environment class is in the JNDI tree under" +
                " the name [java:comp/env/environment]");
        }
    }
}