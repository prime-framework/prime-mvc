/*
 * Copyright (c) 2001-2010, JCatapult.org, All Rights Reserved
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
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.primeframework.servlet.multipart.FileInfo;

/**
 * <p>
 * This class is a servlet input stream of multipart handling.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class MultipartInputStream extends ServletInputStream {
    public static final byte[] BOUNDARY = getBytes("--jcatapultmultipartuploadLKAlskld09309djoid");
    public static final byte[] CLOSE_BOUNDARY = getBytes("--");
    public static final byte[] CRLF = getBytes("\r\n");
    public static final byte[] CONTENT_DISPOSITION = getBytes("Content-Disposition: form-data; name=");
    public static final byte[] CONTENT_TYPE = getBytes("Content-Type: ");
    public static final byte[] CONTENT_TRANSFER_ENCODING = getBytes("Content-Transfer-Encoding: binary");
    public static final byte[] FILENAME = getBytes("; filename=");
    public static final byte[] QUOTE = getBytes("\"");
    private final byte[] bytes;
    private int index = 0;

    public MultipartInputStream(Map<String, List<String>> parameters, Map<String, FileInfo> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String key : parameters.keySet()) {
            List<String> values = parameters.get(key);
            for (String value : values) {
                baos.write(BOUNDARY);
                baos.write(CRLF);

                // Content disposition header
                baos.write(CONTENT_DISPOSITION);
                baos.write(QUOTE);
                baos.write(getBytes(key));
                baos.write(QUOTE);

                // Header end
                baos.write(CRLF);
                baos.write(CRLF);

                // Data
                baos.write(getBytes(value));

                // End
                baos.write(CRLF);
            }
        }

        for (String key : files.keySet()) {
            FileInfo file = files.get(key);
            // Boundary
            baos.write(BOUNDARY);
            baos.write(CRLF);

            // Content disposition header
            baos.write(CONTENT_DISPOSITION);
            baos.write(QUOTE);
            baos.write(getBytes(key));
            baos.write(QUOTE);
            baos.write(FILENAME);
            baos.write(QUOTE);
            baos.write(getBytes(file.file.getName()));
            baos.write(QUOTE);

            // Content type header
            baos.write(CRLF);
            baos.write(CONTENT_TYPE);
            baos.write(getBytes(file.contentType));

            // Content transfer encoding header
            baos.write(CRLF);
            baos.write(CONTENT_TRANSFER_ENCODING);

            // Header end
            baos.write(CRLF);
            baos.write(CRLF);

            // Data
            byte[] ba = new byte[4096];
            InputStream is = new FileInputStream(file.file);
            int len;
            while ((len = is.read(ba)) != -1) {
                baos.write(ba, 0, len);
            }
            is.close();

            // End
            baos.write(CRLF);
        }

        baos.write(BOUNDARY);
        baos.write(CLOSE_BOUNDARY);
        baos.write(CRLF);
        baos.flush();
        bytes = baos.toByteArray();
        System.out.println("Body is " + bytes.length);
    }

    @Override
    public int available() throws IOException {
        return bytes.length;
    }

    public int read() throws IOException {
        return (index < bytes.length) ? (bytes[index++] & 0xff) : -1;
    }

    private static byte[] getBytes(String str) {
        try {
            return str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
