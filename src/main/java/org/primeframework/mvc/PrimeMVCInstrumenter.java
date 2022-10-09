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
package org.primeframework.mvc;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import io.fusionauth.http.server.Instrumenter;

/**
 * An instrumenter for Prime MVC that connects to the java-http server.
 *
 * @author Brian Pontarelli
 */
public class PrimeMVCInstrumenter implements Instrumenter {
  private volatile Counter acceptedConnections;

  private volatile Counter badRequests;

  private volatile Counter bytesRead;

  private volatile Counter bytesWritten;

  private volatile Counter chunkedRequests;

  private volatile Counter chunkedResponses;

  @Override
  public void acceptedConnection() {
    inc(acceptedConnections, 1);
  }

  @Override
  public void badRequest() {
    inc(badRequests, 1);
  }

  @Override
  public void chunkedRequest() {
    inc(chunkedRequests, 1);
  }

  @Override
  public void chunkedResponse() {
    inc(chunkedResponses, 1);
  }

  @Override
  public void readFromClient(long bytes) {
    inc(bytesRead, bytes);
  }

  @Override
  public void serverStarted() {
    // Ignored
  }

  public void updateInjector(Injector injector) {
    // If MetricRegistry is not bound to a singleton, this will return a new instance. But that's fine because we will
    // just store Counters from it and use those. Meaning, this won't thrash or eat performance because the Counters
    // will always point to the same objects until the server is `hup-ed`
    var metricRegistry = injector.getInstance(MetricRegistry.class);
    acceptedConnections = metricRegistry.counter("java-http.accepted-connections");
    badRequests = metricRegistry.counter("java-http.bad-requests");
    chunkedRequests = metricRegistry.counter("java-http.chunked-requests");
    chunkedResponses = metricRegistry.counter("java-http.chunked-responses");
    bytesRead = metricRegistry.counter("java-http.bytes-read");
    bytesWritten = metricRegistry.counter("java-http.bytes-written");
  }

  @Override
  public void wroteToClient(long bytes) {
    inc(bytesWritten, bytes);
  }

  private void inc(Counter counter, long number) {
    if (counter != null) {
      counter.inc(number);
    }
  }
}
