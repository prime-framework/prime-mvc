/*
 * Copyright (c) 2016-2024, Inversoft Inc., All Rights Reserved
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

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwt.json.JacksonModule;

/**
 * Response handler that reads the body as JSON using Jackson. The default ObjectMapper uses Jackson's standard
 * ObjectMapper configuration for deserializing. It also uses the JacksonModule from the
 * <code>io.fusionauth.jwt</code> library for handling various type conversions.
 * <p>
 * The primary way to use this class is via the {@link java.net.http.HttpClient}
 * {@link BodyHandler} interface.
 *
 * @author Brian Pontarelli
 */
public class JSONResponseBodyHandler<T> implements BodyHandler<T> {
  private final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JacksonModule());

  private final Class<T> type;

  /**
   * Constructs handler for the given type
   *
   * @param type responses will be deserialized using this type
   */
  public JSONResponseBodyHandler(Class<T> type) {
    this.type = type;
  }

  /**
   * Creates a subscriber to deserialize content
   *
   * @param responseInfo the response info
   * @return a subscriber that goes from {@link InputStream} to the desired
   *     class.
   */
  @Override
  public BodySubscriber<T> apply(ResponseInfo responseInfo) {
    // Use the InputStream subscriber first, then map using our Jackson function
    return HttpResponse.BodySubscribers.mapping(BodySubscribers.ofInputStream(),
                                                this::apply);
  }

  /**
   * Testing method to directly deserialize using the stream.
   *
   * @param is stream of JSON
   * @return deserialized object
   */
  T apply(InputStream is) {
    if (is == null) {
      return null;
    }
    BetterBufferedInputStream bis;

    try {
      // Read a single byte of data to see if the stream is empty but then reset the stream back 0
      bis = new BetterBufferedInputStream(is, 1024, 1024);
      bis.mark(1024);
      int c = bis.read();
      if (c == -1) {
        return null;
      }

      bis.reset();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      return objectMapper.readValue(bis, type);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to parse the HTTP response as JSON. Actual HTTP response body:\n" +
                                         (bis.isObservableTruncated()
                                             ? ("Note: Output has been truncated to the first " + bis.getObservableLength() + " of " + bis.actualLength + " bytes.\n\n") : "") +
                                         bis.getObservableAsString(), e);
    }
  }

  public static class BetterBufferedInputStream extends BufferedInputStream {
    private final int maximumBytesToObserve;

    private final byte[] observableBuffer;

    private int actualLength;

    private int index;

    public BetterBufferedInputStream(InputStream in, int size, int maximumBytesToObserve) {
      super(in, size);
      this.maximumBytesToObserve = maximumBytesToObserve;
      observableBuffer = new byte[maximumBytesToObserve];
    }

    public String getObservableAsString() {
      return new String(observableBuffer, 0, Math.min(index, maximumBytesToObserve), StandardCharsets.UTF_8);
    }

    public int getObservableLength() {
      return Math.min(index, maximumBytesToObserve);
    }

    public boolean isObservableTruncated() {
      return actualLength > maximumBytesToObserve;
    }

    @Override
    public synchronized int read() throws IOException {
      int c = super.read();
      if (c != -1 && index < maximumBytesToObserve) {
        observableBuffer[index++] = (byte) c;
      }

      actualLength++;
      return c;
    }

    @Override
    public synchronized int read(@Nonnull byte[] b, int off, int len) throws IOException {
      int read = super.read(b, off, len);
      if (read == -1) {
        return read;
      }

      int copyToObservable = Math.min(read, maximumBytesToObserve - index);
      if (index < maximumBytesToObserve) {
        System.arraycopy(b, 0, observableBuffer, index, copyToObservable);
      }

      actualLength += read;
      index += copyToObservable;
      return read;
    }

    @Override
    public synchronized void reset() throws IOException {
      super.reset();
      actualLength = 0;
      index = 0;
    }
  }
}
