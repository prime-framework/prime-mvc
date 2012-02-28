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
package org.jcatapult.l10n;

/**
 * <p>
 * This exception is thrown when error messages are missing.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class MissingMessageException extends RuntimeException {
    public MissingMessageException() {
        super();
    }

    public MissingMessageException(String message) {
        super(message);
    }

    public MissingMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingMessageException(Throwable cause) {
        super(cause);
    }
}