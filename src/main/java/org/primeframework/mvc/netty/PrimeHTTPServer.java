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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.primeframework.mvc.PrimeMVCRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HTTP server for Prime MVC.
 *
 * @author Brian Pontarelli
 */
public class PrimeHTTPServer {
  private static final Logger logger = LoggerFactory.getLogger(PrimeHTTPServer.class);

  private final PrimeMVCRequestHandler main;

  private final int port;

  private Channel channel;

  public PrimeHTTPServer(int port, PrimeMVCRequestHandler main) {
    this.port = port;
    this.main = main;
  }

  public int getPort() {
    return port;
  }

  public void shutdown() {
    logger.info("Shutting down the Prime HTTP server");
    if (channel != null) {
      ChannelFuture future = channel.close();

      try {
        future.get(10_000, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.error("Interrupted while shutting down the server.", e);
      } catch (ExecutionException e) {
        logger.error("Error while shutting down the server.", e);
      } catch (TimeoutException e) {
        logger.error("Timed out after 10 seconds while shutting down the server.", e);
      }
    }
  }

  public void start() {
    logger.info("Starting Prime HTTP server on port [{}]", port);
    NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup);
      bootstrap.channel(NioServerSocketChannel.class);
      bootstrap.childHandler(new PrimeHTTPServerInitializer(port, "http", main));

      channel = bootstrap.bind(port).sync().channel();
      channel.closeFuture().sync();
    } catch (InterruptedException e) {
      logger.error("Unable to start Prime HTTP server", e);
      throw new IllegalStateException(e);
    } finally {
      logger.info("Gracefully closing the server resources");
      try {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
