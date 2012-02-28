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
package org.primeframework.test.servlet;

import javax.servlet.ServletInputStream;
import java.io.IOException;

/**
 * <p>
 * This class is a mock servlet input stream.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class MockServletInputStream extends ServletInputStream {
    private final byte[] bytes;
    private int index = 0;

    public MockServletInputStream() {
        this.bytes = new byte[0];
    }

    public MockServletInputStream(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int available() throws IOException {
        return bytes.length;
    }

    public int read() throws IOException {
        if (index == bytes.length) {
            return -1;
        }
        return bytes[index++];
    }
}
