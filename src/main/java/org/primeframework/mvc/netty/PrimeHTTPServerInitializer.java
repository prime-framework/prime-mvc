/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.primeframework.mvc.PrimeMVCRequestHandler;

public class PrimeHTTPServerInitializer extends ChannelInitializer<SocketChannel> {
  private final PrimeHTTPServerConfiguration configuration;

  private final PrimeMVCRequestHandler main;

  public PrimeHTTPServerInitializer(PrimeHTTPServerConfiguration configuration, PrimeMVCRequestHandler main) {
    this.configuration = configuration;
    this.main = main;
  }

  @Override
  public void initChannel(SocketChannel ch) {
    ch.pipeline()
      .addLast(new HttpServerCodec())
      .addLast(new ReadTimeoutHandler(configuration.readTimeout, TimeUnit.SECONDS))
      .addLast(new HttpServerKeepAliveHandler())
      .addLast(new PrimeHTTPServerHandler(configuration, main))
      .addLast(new PrimeChannelFinalExceptionHandler());
  }
}
