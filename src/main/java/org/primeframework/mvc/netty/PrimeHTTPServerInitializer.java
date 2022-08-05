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
package org.primeframework.mvc.netty;

import javax.net.ssl.SSLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.PrimeMVCRequestHandler;

/**
 * The Prime HTTP server initializer
 *
 * @author Brian Pontarelli
 */
public class PrimeHTTPServerInitializer extends ChannelInitializer<SocketChannel> {
  private final PrimeHTTPListenerConfiguration listenerConfiguration;

  private final PrimeMVCRequestHandler main;

  public PrimeHTTPServerInitializer(PrimeHTTPListenerConfiguration listenerConfiguration, PrimeMVCRequestHandler main) {
    this.listenerConfiguration = listenerConfiguration;
    this.main = main;
  }

  @Override
  public void initChannel(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    if (ch.localAddress().getPort() == listenerConfiguration.httpsPort) {
      pipeline.addLast(buildSslContext().newHandler(ch.alloc()));
    }

    ch.pipeline()
      .addLast(new HttpServerCodec(listenerConfiguration.maxInitialLineLength, listenerConfiguration.maxHeaderSize, listenerConfiguration.maxChunkSize))
      .addLast(new HttpContentCompressor())
      .addLast(new ReadTimeoutHandler(listenerConfiguration.readTimeout, TimeUnit.SECONDS))
      .addLast(new HttpServerKeepAliveHandler())
      .addLast(new PrimeHTTPServerHandler(listenerConfiguration, main))
      .addLast(new PrimeChannelFinalExceptionHandler());
  }

  private SslContext buildSslContext() {
    Objects.requireNonNull(listenerConfiguration.privateKey);
    Objects.requireNonNull(listenerConfiguration.x509Certificates);

    try {
      return SslContextBuilder.forServer(listenerConfiguration.privateKey, listenerConfiguration.x509Certificates).build();
    } catch (SSLException e) {
      throw new ErrorException("error", e);
    }
  }
}
