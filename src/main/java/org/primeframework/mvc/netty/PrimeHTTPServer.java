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

import java.util.ArrayList;
import java.util.List;
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

  private final List<Channel> channels = new ArrayList<>(2);

  private final PrimeHTTPServerConfiguration configuration;

  private final PrimeMVCRequestHandler main;

  public PrimeHTTPServer(PrimeHTTPServerConfiguration configuration, PrimeMVCRequestHandler main) {
    this.configuration = configuration;
    this.main = main;
  }

  public void shutdown() {
    for (Channel channel : channels) {
      logger.info("Shutting down the Prime HTTP server [{}]", channel.localAddress());
      ChannelFuture future = channel.close();

      try {
        future.get(10_000, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.error("Interrupted while shutting down the server [{}].", channel.localAddress(), e);
      } catch (ExecutionException e) {
        logger.error("Error while shutting down the server [{}].", channel.localAddress(), e);
      } catch (TimeoutException e) {
        logger.error("Timed out after 10 seconds while shutting down the server [{}].", channel.localAddress(), e);
      }
    }
  }

  public void start() {
    NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    List<NioEventLoopGroup> workGroups = new ArrayList<>(configuration.listenerConfigurations.size());

    // Fun Netty reading.
    // https://stackoverflow.com/questions/47134860/is-it-a-good-idea-to-use-the-same-netty-eventloopgroup-for-both-serverbootstrap
    // https://stackoverflow.com/a/27112836/3892636

    try {
      for (PrimeHTTPListenerConfiguration listener : configuration.listenerConfigurations) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        workGroups.add(workerGroup);
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.handler(new PrimeMainChannelExceptionHandler());
        bootstrap.childHandler(new PrimeHTTPServerInitializer(listener, main));

        if (listener.httpPort > 0) {
          String description = listener.description != null ? listener.description : "Prime HTTP server";
          logger.info("Starting {} on port [{}]", description, listener.httpPort);
          channels.add(bootstrap.bind(listener.httpPort).sync().channel());
        }

        if (listener.httpsPort > 0) {
          String description = listener.description != null ? listener.description : "Prime HTTPS server";
          logger.info("Starting {} on port [{}]", description, listener.httpsPort);
          channels.add(bootstrap.bind(listener.httpsPort).sync().channel());
        }
      }

    } catch (InterruptedException e) {
      logger.error("Unable to start Prime HTTP server", e);
      throw new IllegalStateException(e);
    } finally {
      try {
        // This guy blocks! am I right?
        for (Channel channel : channels) {
          channel.closeFuture().sync();
        }

        logger.info("Gracefully closing the server resources");
        for (NioEventLoopGroup workerGroup : workGroups) {
          workerGroup.shutdownGracefully();
        }
        bossGroup.shutdownGracefully();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
