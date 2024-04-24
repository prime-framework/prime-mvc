/*
 * Copyright (c) 2016-2022, Inversoft Inc., All Rights Reserved
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Subscriber;

/**
 * @author Brian Pontarelli
 */
public class FormDataBodyHandler implements BodyPublisher {

  private byte[] body;

  private BodyPublisher publisher;

  public FormDataBodyHandler(Map<String, List<String>> request, boolean excludeNullValues) {
    body = getBody(request, excludeNullValues);
    // avoids duplicating all of the logic in ByteArrayPublisher
    publisher = BodyPublishers.ofByteArray(body);
  }

  public FormDataBodyHandler(Map<String, List<String>> request) {
    this(request, false);
  }

  public byte[] getBody() {
    return body;
  }

  private byte[] getBody(Map<String, List<String>> request, boolean excludeNullValues) {
    if (request != null) {
      return serializeRequest(request, excludeNullValues);
    }
    return null;
  }

  @Override
  public long contentLength() {
    return publisher.contentLength();
  }

  @Override
  public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
    publisher.subscribe(subscriber);
  }

  private void append(StringBuilder build, String key, String value) {
    if (build.length() > 0) {
      build.append("&");
    }

    build.append(encode(key)).append("=");
    if (value != null) {
      build.append(encode(value));
    }
  }

  private String encode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  private byte[] serializeRequest(Map<String, List<String>> request, boolean excludeNullValues) {
    StringBuilder build = new StringBuilder();
    request.forEach((key, values) -> {
      if (values == null) {
        if (!excludeNullValues) {
          append(build, key, null);
        }

        return;
      }

      // Values is non-null
      values.stream()
            .filter(v -> v != null || !excludeNullValues)
            .forEach(v -> append(build, key, v));
    });

    return build.toString().getBytes(StandardCharsets.UTF_8);
  }
}
