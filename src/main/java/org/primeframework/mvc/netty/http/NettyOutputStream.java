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
package org.primeframework.mvc.netty.http;

import java.io.OutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class NettyOutputStream extends OutputStream {
  private final ByteBuf buffer = Unpooled.buffer(1024);

  private final ChannelHandlerContext context;

  private final long maxLength;

  private int count;

  public NettyOutputStream(ChannelHandlerContext context, long maxLength) {
    this.context = context;
    this.maxLength = maxLength;
  }

  @Override
  public void flush() {
    if (buffer.readableBytes() > 0) {
      context.writeAndFlush(buffer);
    }
  }

  @Override
  public void write(int b) {
    if (count > maxLength - 1) {
      flush();
      return;
    }

    buffer.writeByte(b);
    count++;

    if (buffer.readableBytes() == 1024) {
      context.writeAndFlush(buffer);
      buffer.resetReaderIndex()
            .resetWriterIndex();
    }
  }
}
