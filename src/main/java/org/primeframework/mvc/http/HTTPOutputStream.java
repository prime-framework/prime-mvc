/*
 * Copyright (c) 2021-2022, Inversoft Inc., All Rights Reserved
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
import java.io.OutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.CookieHeaderNames.SameSite;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.primeframework.mvc.http.HTTPStrings.Headers;

/**
 * Defines an OutputStream that can handle writing an HTTP response back to the client without having to maintain the
 * entire body in memory. This manages the HTTP headers and flushing the response as needed.
 *
 * @author Brian Pontarelli
 */
public class HTTPOutputStream extends OutputStream {
  private final byte[] buf;

  private final ChannelHandlerContext context;

  private int index;

  private boolean oneByteWritten;

  private DefaultHTTPResponse response;

  private State state = State.None;

  public HTTPOutputStream(ChannelHandlerContext context, byte[] buf) {
    this.context = context;
    this.buf = buf;
  }

  @Override
  public void close() throws IOException {
    if (index > 0) {
      _flush(true);
    } else if (state == State.None) {
      writeHeaders();
    }

    state = State.Closed;
  }

  public void setResponse(DefaultHTTPResponse response) {
    this.response = response;
  }

  public boolean wasOneByteWritten() {
    return oneByteWritten;
  }

  @Override
  public void write(int b) throws IOException {
    BufferResult result = buffer(b);
    if (result == BufferResult.Buffered) {
      return;
    }

    _flush(false);
  }

  private void _flush(boolean closing) {
    if (index == 0) {
      return;
    }

    if (state == State.None) {
      if (closing && response.getContentLength() == null) {
        response.setContentLength((long) index);
      }

      writeHeaders();
      writeBuffer();
      state = State.BodyInProgress;
    } else if (state == State.BodyInProgress) {
      writeBuffer();
    } else {
      throw new IllegalStateException("The OutputStream was closed but then continued to be written to and/or flushed.");
    }
  }

  private BufferResult buffer(int b) {
    buf[index] = (byte) b;
    index++;
    return index == buf.length ? BufferResult.Full : BufferResult.Buffered;
  }

  private io.netty.handler.codec.http.cookie.Cookie toNettyCookie(org.primeframework.mvc.http.Cookie cookie) {
    DefaultCookie nettyCookie = new DefaultCookie(cookie.name, cookie.value != null ? cookie.value : "");
    nettyCookie.setDomain(cookie.domain);
    nettyCookie.setHttpOnly(cookie.httpOnly);
    nettyCookie.setPath(cookie.path);
    nettyCookie.setSameSite(cookie.sameSite != null ? SameSite.valueOf(cookie.sameSite.name()) : null);
    nettyCookie.setSecure(cookie.secure);
    if (cookie.maxAge != null) {
      nettyCookie.setMaxAge(cookie.maxAge);
    }
    return nettyCookie;
  }

  private void writeBuffer() {
    ByteBuf nettyBuf = Unpooled.wrappedBuffer(buf, 0, index);
    // Note, you have to wrap this buffer to get Netty to chunk it.
    context.writeAndFlush(new DefaultHttpContent(nettyBuf));
    index = 0;
  }

  private void writeHeaders() {
    HttpResponseStatus status = HttpResponseStatus.valueOf(response.getStatus());
    HttpHeaders headers = new DefaultHttpHeaders(true);
    response.getHeadersMap().forEach(headers::add);
    response.getCookies()
            .stream()
            .filter(c -> c.name != null)
            .forEach(cookie -> headers.add(Headers.SetCookie, ServerCookieEncoder.LAX.encode(toNettyCookie(cookie))));
    DefaultHttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status, headers);
    context.writeAndFlush(nettyResponse);
    oneByteWritten = true;
  }

  enum BufferResult {
    Buffered,
    Full
  }

  enum State {
    None,
    BodyInProgress,
    Closed
  }
}
