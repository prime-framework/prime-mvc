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
package org.jcatapult;

/**
 * <p>
 * This class is a runtime exception that defines fatal errors from the
 * JCatapult framework. These errors should rarely be ignored, even in a
 * production environment.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class JCatapultFatalException extends RuntimeException {
    public JCatapultFatalException() {
        super();
    }

    public JCatapultFatalException(String message) {
        super(message);
    }

    public JCatapultFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public JCatapultFatalException(Throwable cause) {
        super(cause);
    }
}
