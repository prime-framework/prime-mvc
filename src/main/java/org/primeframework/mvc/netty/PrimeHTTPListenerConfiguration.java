/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import org.primeframework.mvc.util.Buildable;

/**
 * An HTTP listener configuration that is used to create a Netty ServerBootstrap and Channel. Technically, Netty is
 * running multiple servers inside a single Prime MVC server, but we call them listeners.
 *
 * @author Daniel DeGroff
 */
public class PrimeHTTPListenerConfiguration implements Buildable<PrimeHTTPListenerConfiguration> {
  /**
   * An optional description of this listener configuration.
   */
  public String description;

  /**
   * The ports to bind to the HTTP server.
   */
  public int httpPort = -1;

  /**
   * The port to bind for HTTPs server.
   */
  public int httpsPort = -1;

  /**
   * The max size of any HTTP body (JSON, Octet-stream, etc) in bytes.
   */
  public long maxBodySize = 32 * 1024 * 1024;

  /**
   * The max chunk size in bytes.
   */
  public int maxChunkSize = 64 * 1024;

  /**
   * The max header size in bytes.
   */
  public int maxHeaderSize = 64 * 1024;

  /**
   * The max initial line length in bytes.
   */
  public int maxInitialLineLength = 64 * 1024;

  /**
   * Optional private key used for TLS.
   */
  public PrivateKey privateKey;

  /**
   * HTTP connection read timeout in seconds.
   */
  public long readTimeout = 0;

  /**
   * Optional X.509 certificate chain used for TLS.
   */
  public List<X509Certificate> x509Certificates;

  public PrimeHTTPListenerConfiguration() {
  }

  public PrimeHTTPListenerConfiguration(int httpPort) {
    this.httpPort = httpPort;
  }

  public PrimeHTTPListenerConfiguration(int httpPort, int httpsPort) {
    this.httpPort = httpPort;
    this.httpsPort = httpsPort;
  }

  /**
   * The scheme used by the channel handler.
   */
  public String getScheme(int port) {
    return port == httpPort ? "http" : "https";
  }
}