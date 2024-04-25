/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.primeframework.mvc.util.EncodingUtils;

/**
 * Converts {@link Multiparts} of data to an encoded byte array, properly formatted for the
 * multipart/form-data content type
 *
 * @author Brian Pontarelli
 */
public class MultipartBodyHandler {
  public final String boundary = UUID.randomUUID().toString().replace("-", "");

  private final Multiparts request;

  private byte[] body;

  public MultipartBodyHandler(Multiparts request) {
    this.request = request;
  }

  public byte[] getBody() {
    if (request.files != null || request.parameters != null) {
      serializeRequest();
    }
    return body;
  }

  private void serializeRequest() {
    if (body == null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Writer writer = new OutputStreamWriter(baos);

      try {
        if (request.files != null) {
          for (MultipartFileUpload file : request.files) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"").append(URLEncoder.encode(file.name, "UTF-8")).append("\"")
                  .append("; filename=\"").append(EncodingUtils.escapedQuotedString(file.fileName))
                  .append("\"; filename*=UTF-8''").append(EncodingUtils.rfc5987_encode(file.fileName));
            if (file.contentType != null) {
              writer.append("\r\nContent-Type: ").append(file.contentType);
            }
            writer.append("\r\n\r\n");
            writer.flush();
            baos.write(Files.readAllBytes(file.file));
            baos.flush();
            writer.append("\r\n");
            writer.flush();
          }
        }

        if (request.parameters != null) {
          for (Map.Entry<String, List<String>> entry : request.parameters.entrySet()) {
            for (String value : entry.getValue()) {
              writer.append("--").append(boundary).append("\r\n");
              writer.append("Content-Disposition: form-data; name=\"").append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("\"\r\n\r\n").append(value).append("\r\n");
            }
          }
        }

        writer.append("--").append(boundary).append("--");
        writer.flush();
        baos.flush();
        body = baos.toByteArray();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  public static class Multiparts {
    public final List<MultipartFileUpload> files;

    public final Map<String, List<String>> parameters;

    public Multiparts(List<MultipartFileUpload> files, Map<String, List<String>> parameters) {
      this.files = files;
      this.parameters = parameters;
    }
  }
}
