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
package org.primeframework.mvc.parameter.convert;

/**
 * <p>
 * This is a runtime exception thrown when the state of the current
 * conversion is such that conversion can never proceed. This is
 * normally fatal and requires developer correction.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ConverterStateException extends RuntimeException {
    public ConverterStateException() {
        super();
    }

    public ConverterStateException(String message) {
        super(message);
    }

    public ConverterStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConverterStateException(Throwable cause) {
        super(cause);
    }
}