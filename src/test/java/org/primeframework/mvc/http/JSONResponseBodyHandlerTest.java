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

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class JSONResponseBodyHandlerTest {

  @Test
  public void apply_inputstream_empty() throws IOException {
    // arrange
    var handler = new JSONResponseBodyHandler<>(Map.class);

    // act
    var result = handler.apply(new InputStream() {
      @Override
      public int available() {
        return 0;
      }

      public int read() {
        return -1;
      }
    });

    // assert
    assertNull(result);
  }

  @Test
  public void apply_inputstream_null() throws Exception {
    // arrange
    var handler = new JSONResponseBodyHandler<>(Map.class);

    // act
    var result = handler.apply((InputStream) null);

    // assert
    assertNull(result);
  }

  @Test
  public void apply_subscriber() throws Exception {
    // arrange
    var handler = new JSONResponseBodyHandler<>(Map.class);
    BodySubscriber<Map> subscriber = handler.apply(new ResponseInfo() {
      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public int statusCode() {
        return 200;
      }

      @Override
      public Version version() {
        return Version.HTTP_1_1;
      }
    });
    try (var jsonPublisher = new SubmissionPublisher<List<ByteBuffer>>()) {
      jsonPublisher.subscribe(subscriber);
      jsonPublisher.submit(List.of(ByteBuffer.wrap("{\"test1\":\"value1\",\"test2\":\"value2\"}".getBytes())));
    }

    // act
    var result = subscriber.getBody()
                           .toCompletableFuture()
                           .get();

    // assert
    assertEquals(result.get("test1"), "value1");
  }
}
