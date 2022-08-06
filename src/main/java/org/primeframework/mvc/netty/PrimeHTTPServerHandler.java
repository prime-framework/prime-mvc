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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import org.primeframework.mvc.PrimeMVCRequestHandler;
import org.primeframework.mvc.http.DefaultHTTPRequest;
import org.primeframework.mvc.http.DefaultHTTPResponse;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPOutputStream;
import org.primeframework.mvc.io.PrimeByteArrayOutputStream;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Let's go ahead and handle some HTTP messages shall we?
 *
 * @author Brian Pontarelli
 */
public class PrimeHTTPServerHandler extends SimpleChannelInboundHandler<HttpObject> {
  private static final Logger logger = LoggerFactory.getLogger(PrimeHTTPServerHandler.class);

  private final byte[] buf = new byte[65536];

  private final PrimeHTTPListenerConfiguration listenerConfiguration;

  private final PrimeMVCRequestHandler requestHandler;

  private Path binaryFile;

  private long bytesRead = 0;

  private HttpPostRequestDecoder decoder;

  private OutputStream outputStream;

  private DefaultHTTPRequest primeRequest;

  private HttpRequest request;

  public PrimeHTTPServerHandler(PrimeHTTPListenerConfiguration listenerConfiguration,
                                PrimeMVCRequestHandler requestHandler) {
    this.listenerConfiguration = listenerConfiguration;
    this.requestHandler = requestHandler;
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    if (decoder != null) {
      decoder.cleanFiles();
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext context, HttpObject msg) {
    try {
      if (msg instanceof HttpRequest) {
        request = (HttpRequest) msg;

        // Parse the request and if it is invalid, bail with a 400 status code
        if (!parseRequest(request, context)) {
          sendErrorResponse(context, HttpResponseStatus.BAD_REQUEST, "Unable to parse request");
        }
      } else if (msg instanceof HttpContent chunk) {
        if (decoder != null) {
          decoder.offer(chunk);
        } else {
          if (primeRequest.getContentLength() == null) {
            sendErrorResponse(context, HttpResponseStatus.LENGTH_REQUIRED, "Missing Content-Length header");
            return;
          }

          ByteBuf content = chunk.content();
          int length = content.readableBytes();
          if (length + bytesRead > listenerConfiguration.maxBodySize) {
            sendErrorResponse(context, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, null);
            return;
          }

          content.getBytes(0, outputStream, length);
          bytesRead += length;
        }

        if (chunk instanceof LastHttpContent) {
          if (decoder != null) {
            parseBody();
          } else if (binaryFile != null) {
            outputStream.flush();
            outputStream.close();
            primeRequest.addFile(new FileInfo(binaryFile, null, "body", "application/octet-stream"));
          } else {
            primeRequest.body = ((PrimeByteArrayOutputStream) outputStream).toByteBuffer();
          }

          HTTPOutputStream outputStream = new HTTPOutputStream(context, buf);
          DefaultHTTPResponse primeResponse = new DefaultHTTPResponse(outputStream);
          outputStream.setResponse(primeResponse);

          // Call Prime MVC - FTW!
          requestHandler.handleRequest(primeRequest, primeResponse);
          primeResponse.getOutputStream().close();
          context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

          reset();
        }
      }
    } catch (Exception e) {
      logger.error("Prime encountered an exception while processing the request. This is likely an internal error.", e);
      FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
      ChannelFuture future = context.writeAndFlush(response);
      future.addListener(ChannelFutureListener.CLOSE);

      reset();
    }
  }

  private void parseBody() throws IOException {
    List<InterfaceHttpData> data = decoder.getBodyHttpDatas();
    for (InterfaceHttpData datum : data) {
      if (datum.getHttpDataType() == HttpDataType.Attribute) {
        Attribute attribute = (Attribute) datum;
        primeRequest.addParameter(attribute.getName(), attribute.getValue());
      } else if (datum.getHttpDataType() == HttpDataType.FileUpload) {
        FileUpload fileUpload = (FileUpload) datum;
        primeRequest.addFile(new FileInfo(fileUpload.getFile().toPath(), fileUpload.getFilename(), fileUpload.getName(), fileUpload.getContentType()));
      }
    }
  }

  private String parseHostHeader(String host) {
    if (host == null) {
      return null;
    }

    int index = host.indexOf(':');
    if (index > 0) {
      return host.substring(0, index);
    }

    return host;
  }

  private boolean parseRequest(HttpRequest msg, ChannelHandlerContext context) throws IOException {
    primeRequest = new DefaultHTTPRequest();

    // Handle the networking pieces
    InetSocketAddress localAddress = (InetSocketAddress) context.channel().localAddress();
    InetSocketAddress remoteAddress = (InetSocketAddress) context.channel().remoteAddress();
    primeRequest.setPort(localAddress.getPort());
    primeRequest.setIPAddress(remoteAddress.getAddress().getHostAddress());

    // Handle cookies
    Set<Cookie> cookies;
    String value = msg.headers().get(HttpHeaderNames.COOKIE);
    if (value == null) {
      cookies = Set.of();
    } else {
      cookies = ServerCookieDecoder.LAX.decode(value);
    }

    for (Cookie cookie : cookies) {
      primeRequest.addCookies(new org.primeframework.mvc.http.Cookie().with(c -> c.domain = cookie.domain())
                                                                      .with(c -> c.httpOnly = cookie.isHttpOnly())
                                                                      .with(c -> c.maxAge = cookie.maxAge())
                                                                      .with(c -> c.name = cookie.name())
                                                                      .with(c -> c.path = cookie.path())
                                                                      .with(c -> c.secure = cookie.isSecure())
                                                                      .with(c -> c.value = cookie.value()));
    }

    // Handle the headers
    HttpHeaders headers = msg.headers();
    for (Entry<String, String> header : headers) {
      primeRequest.addHeader(header.getKey(), header.getValue());
    }

    // Handle the request pieces
    QueryStringDecoder query = new QueryStringDecoder(msg.uri());
    primeRequest.setContentLength((long) HttpUtil.getContentLength(msg, 0));
    primeRequest.setContentType(HttpUtil.getMimeType(msg) != null ? HttpUtil.getMimeType(msg).toString() : null);
    primeRequest.setCharacterEncoding(HttpUtil.getCharset(msg, StandardCharsets.UTF_8));
    primeRequest.setHost(parseHostHeader(msg.headers().get("Host")));
    primeRequest.addLocales(LanguageRange.parse(msg.headers().get("Accept-Language", "en")) // Default to English
                                         .stream()
                                         .sorted(Comparator.comparing(LanguageRange::getWeight).reversed())
                                         .map(LanguageRange::getRange)
                                         .map(Locale::forLanguageTag)
                                         .collect(Collectors.toList()));
    primeRequest.setMethod(HTTPMethod.of(msg.method().name()));
    primeRequest.setMultipart(HttpPostRequestDecoder.isMultipart(request));
    primeRequest.setPath(query.rawPath());
    primeRequest.addParameters(query.parameters());
    primeRequest.setQueryString(query.rawQuery());
    primeRequest.setScheme(listenerConfiguration.getScheme(primeRequest.getPort()));

    // Validate the request before reading the body and processing everything via the MVC
    if (requestInvalid(primeRequest)) {
      return false;
    }

    // Set up the body handler
    String contentType = primeRequest.getContentType();
    contentType = contentType != null ? contentType : "";
    if (primeRequest.isMultipart() || contentType.equalsIgnoreCase(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())) {
      HttpDataFactory factory = new DefaultHttpDataFactory(primeRequest.isMultipart(), StandardCharsets.UTF_8);
      decoder = new HttpPostRequestDecoder(factory, request);
    } else if (contentType.equalsIgnoreCase(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString())) {
      binaryFile = Files.createTempFile("prime-mvc-binary-upload", null);
      outputStream = new BufferedOutputStream(Files.newOutputStream(binaryFile));
    } else {
      outputStream = new PrimeByteArrayOutputStream();
    }

    return true;
  }

  private boolean requestInvalid(DefaultHTTPRequest primeRequest) {
    return primeRequest.getHost() == null || primeRequest.getHost().isBlank();
  }

  private void reset() {
    bytesRead = 0;
    outputStream = null;
    primeRequest = null;

    if (decoder != null) {
      decoder.destroy();
      decoder = null;
    }
  }

  private void sendErrorResponse(ChannelHandlerContext context, HttpResponseStatus status, String message) {
    FullHttpResponse response;
    if (message != null) {
      ByteBuf content = ByteBufUtil.encodeString(ByteBufAllocator.DEFAULT, CharBuffer.wrap(message), StandardCharsets.UTF_8);
      response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
    } else {
      response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
    }

    ChannelFuture future = context.writeAndFlush(response);
    future.addListener(ChannelFutureListener.CLOSE);
    reset();
  }
}
