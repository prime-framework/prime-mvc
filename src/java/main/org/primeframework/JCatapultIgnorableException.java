/*
 * Copyright (c) 2009, JCatapult.org, All Rights Reserved
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
package org.primeframework;

/**
 * <p>
 * This class is a runtime exception that the JCatapult framework will throw from
 * various locations that can be safely ignored in a production environment. This
 * was created to prevent DOS attacks on production servers. For example, the
 * JCatapult MVC throws an exception when an incoming parameter doesn't map to
 * the action. This exception is generally only useful in development mode, but
 * sometimes it proves useful in production. For this reason, it is ignorable in
 * most cases.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class JCatapultIgnorableException extends RuntimeException {
    public JCatapultIgnorableException() {
        super();
    }

    public JCatapultIgnorableException(String message) {
        super(message);
    }

    public JCatapultIgnorableException(String message, Throwable cause) {
        super(message, cause);
    }

    public JCatapultIgnorableException(Throwable cause) {
        super(cause);
    }
}
