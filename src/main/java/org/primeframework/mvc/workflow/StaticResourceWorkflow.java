/*
 * Copyright (c) 2012-2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;

import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.http.HTTPContext;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.http.HTTPStrings.Headers;
import org.primeframework.mvc.http.HTTPTools;
import org.primeframework.mvc.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles static resources via the Prime workflow chain. Static resources can either be files in the
 * `static` directory of the web application or they can be located inside JAR files in the classpath. This workflow
 * will attempt to load the files from both locations and will properly handle the expiration, last modified instant,
 * and other components of the HTTP request to try and ensure that browsers cache as much as possible to reduce load on
 * the server.
 *
 * @author Brian Pontarelli
 */
public class StaticResourceWorkflow implements Workflow {
  private static final Logger logger = LoggerFactory.getLogger(StaticResourceWorkflow.class);

  private final Set<ClassLoader> additionalClassLoaders;

  private final MVCConfiguration configuration;

  private final HTTPContext context;

  private final HTTPRequest request;

  private final HTTPResponse response;

  @Inject
  public StaticResourceWorkflow(HTTPContext context, HTTPRequest request, HTTPResponse response,
                                Set<ClassLoader> additionalClassLoaders, MVCConfiguration configuration) {
    this.context = context;
    this.request = request;
    this.response = response;
    this.additionalClassLoaders = additionalClassLoaders;
    this.configuration = configuration;
  }

  /**
   * Checks for static resource request and if it is one, locates and sends back the static resource. If it isn't one,
   * it passes control down the chain.
   *
   * @param workflowChain The workflow chain to use if the request is not a static resource.
   * @throws IOException If the request is a static resource and sending it failed or if the chain throws an
   *                     IOException.
   */
  public void perform(WorkflowChain workflowChain) throws IOException {
    boolean handled = false;

    // Ensure that this is a request for a resource and not a class
    String uri = HTTPTools.getRequestURI(request);
    if (!uri.endsWith(".class")) {
      try {
        handled = findStaticResource(uri, request, response);
      } catch (RuntimeException | IOException e) {
        logger.error("Unable to load static resource at uri [{}]", uri);
        throw e;
      }
    }

    if (!handled) {
      workflowChain.continueWorkflow();
    }
  }

  /**
   * Locate a static resource and copy directly to the response, setting the appropriate caching headers.
   *
   * @param uri      The resource uri.
   * @param request  The request
   * @param response The response
   * @return True if the resource was found in the classpath and if it was successfully written back to the output
   *     stream. Otherwise, this returns false if the resource doesn't exist in the classpath.
   * @throws IOException If anything goes wrong
   */
  protected boolean findStaticResource(String uri, HTTPRequest request, HTTPResponse response) throws IOException {
    // Retrieve the modified header (defaults to null)
    Instant ifModifiedSince = null;
    try {
      ifModifiedSince = request.getDateHeader(Headers.IfModifiedSince);
    } catch (Exception e) {
      logger.warn("Invalid If-Modified-Since header value [{}], ignoring", request.getHeader(Headers.IfModifiedSince));
    }

    // See if a file exists in the static directory
    String staticDirectory = configuration.staticDirectory();
    Path file = context.resolve(staticDirectory + uri);
    if (Files.isRegularFile(file) && Files.isReadable(file)) {
      Instant modified = Files.getLastModifiedTime(file).toInstant();
      if (ifModifiedSince == null || modified.isAfter(ifModifiedSince)) {
        try (InputStream is = Files.newInputStream(file)) {
          writeResponse(response, is, modified);
        }

        String contentType = Files.probeContentType(file);
        response.setContentType(contentType);
      } else {
        addHeaders(response, modified);
        response.setStatus(Status.SC_NOT_MODIFIED);
      }

      return true;
    }

    // See if there is a classpath entry
    URL url = HTTPContext.class.getResource(uri);
    if (url == null) {
      for (ClassLoader classLoader : additionalClassLoaders) {
        url = classLoader.getResource(uri);
        if (url != null) {
          break;
        }
      }
    }

    if (url != null) {
      URLConnection connection = url.openConnection();
      Instant modified = Instant.ofEpochMilli(connection.getLastModified());
      if (ifModifiedSince == null || modified.isAfter(ifModifiedSince)) {
        try (InputStream is = connection.getInputStream()) {
          writeResponse(response, is, modified);
        }

        response.setContentType(connection.getContentType());
      } else {
        addHeaders(response, modified);
        response.setStatus(Status.SC_NOT_MODIFIED);
      }

      return true;
    }

    return false;
  }

  private void addHeaders(HTTPResponse response, Instant modified) {
    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    ZonedDateTime expiry = now.plusDays(7);
    response.setHeader(Headers.CacheControl, "public");
    response.setDateHeader(Headers.Date, now);
    response.setDateHeader(Headers.Expires, expiry); // 7 days
    response.setDateHeader(Headers.LastModified, ZonedDateTime.ofInstant(modified, ZoneOffset.UTC));
    response.setDateHeader(Headers.RetryAfter, expiry); // 7 days
  }

  private void writeResponse(HTTPResponse response, InputStream is, Instant modified) throws IOException {
    addHeaders(response, modified);
    response.setStatus(200);
    is.transferTo(response.getOutputStream());
  }
}