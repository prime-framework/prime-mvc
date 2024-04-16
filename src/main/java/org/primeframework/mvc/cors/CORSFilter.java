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
package org.primeframework.mvc.cors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.HTTPValues.Headers;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter that enables client-side cross-origin requests by implementing W3C's CORS (<b>C</b>ross-<b>O</b>rigin
 * <b>R</b>esource-<b>S</b>haring) specification for resources. Each {@link HTTPRequest}
 * request is inspected as per specification, and appropriate response headers are added to {@link HTTPResponse}.
 * <p>
 * By default, it also sets following request attributes, that helps to determine nature of request downstream.
 * <ul>
 * <li><b>cors.isCorsRequest:</b> Flag to determine if request is a CORS request. Set to <code>true</code> if CORS
 * request; <code>false</code> otherwise.</li>
 * <li><b>cors.request.origin:</b> The Origin URL.</li>
 * <li><b>cors.request.type:</b> Type of request. Values: <code>simple</code> or <code>preflight</code> or
 * <code>not_cors</code> or <code>invalid_cors</code></li>
 * <li><b>cors.request.headers:</b> Request headers sent as 'Access-Control-Request-Headers' header, for pre-flight
 * request.</li>
 * </ul>
 *
 * @author Mohit Soni
 * @author Trevor Smith
 * @author Daniel DeGroff
 * @see <a href="http://www.w3.org/TR/cors/">CORS specification</a>
 */
public final class CORSFilter {
  /**
   * {@link Collection} of non-simple HTTP methods. Case-sensitive.
   */
  private static final Collection<HTTPMethod> ComplexHTTPMethods = Set.of(HTTPMethod.PATCH, HTTPMethod.PUT, HTTPMethod.DELETE, HTTPMethod.TRACE, HTTPMethod.CONNECT);

  /**
   * {@link Collection} of Simple HTTP request headers. Case-insensitive.
   *
   * @see <a href="http://www.w3.org/TR/cors/#terminology"></a>
   */
  private static final Collection<String> SimpleHTTPRequestContentTypes = Set.of("application/x-www-form-urlencoded", "multipart/form-data", "text/plain");

  private static final Logger logger = LoggerFactory.getLogger(CORSFilter.class);

  /**
   * A {@link Collection} of headers consisting of zero or more header field names that are supported by the resource.
   */
  private final Collection<String> allowedHTTPHeaders = new LinkedHashSet<>();

  /**
   * A {@link Collection} of headers consisting of zero or more header field names that are supported by the resource,
   * preserved with original formatting.
   */
  private final Collection<String> allowedHTTPHeadersOriginal = new LinkedHashSet<>();

  /**
   * A {@link Collection} of methods consisting of zero or more methods that are supported by the resource.
   */
  private final Collection<HTTPMethod> allowedHTTPMethods = new LinkedHashSet<>();

  /**
   * A {@link Collection} of origins consisting of zero or more origins that are allowed access to the resource.
   */
  private final Collection<String> allowedOrigins = new LinkedHashSet<>();

  /**
   * A {@link Collection} of exposed headers consisting of zero or more header field names of headers other than the
   * simple response headers that the resource might use and can be exposed.
   */
  private final Collection<String> exposedHeaders = new LinkedHashSet<>();

  /**
   * Determines if any origin is allowed to make request.
   */
  private boolean anyOriginAllowed;

  /**
   * Set this to true in order to create event logs when requests are blocked due to CORS.
   */
  private boolean debug;

  /**
   * An optional debugger.
   */
  private CORSDebugger debugger;

  /**
   * Regex for excluding URI patterns from CORS filter processing
   */
  private Pattern excludedPathPattern;

  /**
   * Regex for including URI patterns from CORS filter processing
   */

  private Pattern includedPathPattern;

  /**
   * Allow a predicate to decide whether to include in CORS
   */
  private Predicate<String> includeUriChecker;

  /**
   * Indicates (in seconds) how long the results of a pre-flight request can be cached in a pre-flight result cache.
   */
  private long preflightMaxAge;

  // ----------------------------------------------------- Request attributes

  /**
   * A supports credentials flag that indicates whether the resource supports user credentials in the request. It is
   * true when the resource does and false otherwise.
   */
  private boolean supportsCredentials;

  /**
   * Filter the request and update response based on CORS rules. Continue processing the workflow chain if request
   * passes filter.
   *
   * @param request       The {@link HTTPRequest}
   * @param response      The {@link HTTPResponse}
   * @param workflowChain The {@link WorkflowChain} to continue workflow processing
   */
  public void doFilter(HTTPRequest request, final HTTPResponse response, WorkflowChain workflowChain)
      throws IOException {
    // Allow a same site request with an origin header. This means that FusionAuth can make requests to itself w/out going through the CORS filter.
    // - For example, regardless of the CORS configuration we want to be able to POST to the /oauth2/authorize endpoint
    String origin = request.getHeader(Headers.Origin);
    if (origin != null && isSameOrigin(origin, request)) {
      workflowChain.continueWorkflow();
      return;
    }

    // Determines the CORS request type, Skip the CORS filter if this is an excluded URI
    CORSFilter.CORSRequestType requestType = checkRequestType(request, origin);

    // If this URI should be excluded from the CORS filter and this is a pre-flight check, return 403, else mark as Not CORS.
    String requestURI = request.getPath();
    if (excludedRequestURI(requestURI)) {
      if (requestType == CORSRequestType.PRE_FLIGHT) {
        handleInvalidCORS(request, response, InvalidCORSReason.PreFlightUnexpected, requestURI);
        return;
      } else {
        requestType = CORSRequestType.NOT_CORS;
      }
    }

    // Handles Simple and Actual CORS requests.
    // Handles a Pre-flight CORS request.
    // Handles a Normal request that is not a cross-origin request.
    // Handles a CORS request that violates specification.
    switch (requestType) {
      case SIMPLE, ACTUAL -> handleSimpleCORS(request, response, workflowChain);
      case PRE_FLIGHT -> handlePreflightCORS(request, response);
      case NOT_CORS -> workflowChain.continueWorkflow();
      default -> handleInvalidCORS(request, response, InvalidCORSReason.UnhandledCORSRequestType, requestType);
    }
  }

  public CORSFilter withAllowCredentials(boolean allow) {
    this.supportsCredentials = allow;
    return this;
  }

  public CORSFilter withAllowedHTTPHeaders(List<String> headers) {
    if (headers != null) {
      this.allowedHTTPHeaders.clear();
      this.allowedHTTPHeadersOriginal.clear();
      for (String header : headers) {
        this.allowedHTTPHeaders.add(header.toLowerCase());
        this.allowedHTTPHeadersOriginal.add(header);
      }
    }
    return this;
  }

  public CORSFilter withAllowedHTTPMethods(List<HTTPMethod> methods) {
    if (methods != null) {
      this.allowedHTTPMethods.clear();
      this.allowedHTTPMethods.addAll(methods);
    }
    return this;
  }

  public CORSFilter withAllowedOrigins(List<URI> origins) {
    if (origins != null) {
      if (origins.contains(URI.create("*"))) {
        this.anyOriginAllowed = true;
      } else {
        this.anyOriginAllowed = false;
        this.allowedOrigins.clear();
        origins.forEach(o -> this.allowedOrigins.add(o.toString()));
      }
    }
    return this;
  }

  public CORSFilter withDebugEnabled(boolean debug) {
    this.debug = debug;
    return this;
  }

  public CORSFilter withDebugger(CORSDebugger debugger) {
    this.debugger = debugger;
    return this;
  }

  /**
   * Specifies a regex that matches paths that should be ignored for CORS (meaning they will always fail CORS and the
   * browser will not load them). This effectively protects these resources.
   *
   * @param pattern The pattern.
   * @return This.
   */
  public CORSFilter withExcludedPathPattern(Pattern pattern) {
    this.excludedPathPattern = pattern;
    return this;
  }

  public CORSFilter withIncludedPathPattern(Pattern pattern) {
    this.includedPathPattern = pattern;
    return this;
  }

  public CORSFilter withIncludedUriChecker(Predicate<String> includeFunction) {
    this.includeUriChecker = includeFunction;
    return this;
  }

  public CORSFilter withExposedHeaders(List<String> headers) {
    if (headers != null) {
      this.exposedHeaders.clear();
      this.exposedHeaders.addAll(headers);
    }
    return this;
  }

  public CORSFilter withPreflightMaxAge(int maxAge) {
    this.preflightMaxAge = maxAge;
    return this;
  }

  /**
   * Determines the request type.
   *
   * @param request The HTTP request.
   * @param origin  The origin header.
   * @return The CORS type.
   */
  private CORSRequestType checkRequestType(HTTPRequest request, String origin) {
    if (request == null) {
      throw new IllegalArgumentException("HttpServletRequest object is null");
    }

    // Section 6.1.1 and Section 6.2.1
    if (origin == null) {
      return CORSRequestType.NOT_CORS;
    }

    if (origin.isBlank() || !isValidOrigin(origin)) {
      return CORSRequestType.INVALID_CORS;
    }

    CORSRequestType requestType = CORSRequestType.INVALID_CORS;
    HTTPMethod method = request.getMethod();
    if (method != null) {
      if (HTTPMethod.OPTIONS.is(method)) {
        String accessControlRequestMethodHeader = request.getHeader(Headers.AccessControlRequestMethod);
        if (accessControlRequestMethodHeader != null && !accessControlRequestMethodHeader.isBlank()) {
          requestType = CORSRequestType.PRE_FLIGHT;
        } else if (accessControlRequestMethodHeader == null) {
          requestType = CORSRequestType.ACTUAL;
        }
      } else if (HTTPMethod.GET.is(method) || HTTPMethod.HEAD.is(method)) {
        requestType = CORSRequestType.SIMPLE;
      } else if (HTTPMethod.POST.is(method)) {
        String contentType = request.getContentType();
        if (contentType != null) {
          contentType = contentType.toLowerCase().trim();
          if (SimpleHTTPRequestContentTypes.contains(contentType)) {
            requestType = CORSRequestType.SIMPLE;
          } else {
            requestType = CORSRequestType.ACTUAL;
          }
        }
      } else if (ComplexHTTPMethods.contains(method)) {
        requestType = CORSRequestType.ACTUAL;
      }
    }

    return requestType;
  }

  /**
   * Match request URI to exclude URI pattern
   *
   * @param requestURI the HTTP servlet request URI
   * @return true if this request should be excluded from CORS
   */
  private boolean excludedRequestURI(final String requestURI) {
    if (excludedPathPattern != null) {
      return excludedPathPattern.matcher(requestURI).find();
    }
    else if (includedPathPattern != null) {
      return !includedPathPattern.matcher(requestURI).find();
    }
    else if (includeUriChecker != null) {
      return !includeUriChecker.test(requestURI);
    }
    // we're not using any of the functionality
    return false;
  }

  /**
   * Handles a CORS request that violates specification.
   *
   * @param request     The {@link HTTPRequest} object.
   * @param response    The {@link HTTPResponse} object.
   * @param reason      The reason code.
   * @param reasonValue The value that was the cause of the failure.
   */
  private void handleInvalidCORS(HTTPRequest request, HTTPResponse response, InvalidCORSReason reason,
                                 Object reasonValue) {
    if (logger.isDebugEnabled() || debug) {
      logRequest(request, reason, reasonValue);
    }

    response.setContentType("text/plain");
    response.setStatus(403);
  }

  /**
   * Handles CORS pre-flight request.
   *
   * @param request  The {@link HTTPRequest} object.
   * @param response The {@link HTTPResponse} object.
   */
  private void handlePreflightCORS(HTTPRequest request, HTTPResponse response) {
    final String origin = request.getHeader(Headers.Origin);

    // Section 6.2.2
    if (!isOriginAllowed(origin)) {
      handleInvalidCORS(request, response, InvalidCORSReason.PreFlightOriginNotAllowed, origin);
      return;
    }

    // Section 6.2.3
    String accessControlRequestMethodValue = request.getHeader(Headers.AccessControlRequestMethod);
    HTTPMethod accessControlRequestMethod = accessControlRequestMethodValue != null ? HTTPMethod.of(accessControlRequestMethodValue.trim()) : null;
    if (accessControlRequestMethod == null) {
      handleInvalidCORS(request, response, InvalidCORSReason.PreFlightMethodNotRecognized, null);
      return;
    }

    // Section 6.2.4
    String accessControlRequestHeadersHeader = request.getHeader(Headers.AccessControlRequestHeaders);
    List<String> accessControlRequestHeaders = new LinkedList<>();
    if (accessControlRequestHeadersHeader != null && !accessControlRequestHeadersHeader.trim().isEmpty()) {
      String[] headers = accessControlRequestHeadersHeader.trim().split(",");
      for (String header : headers) {
        accessControlRequestHeaders.add(header.trim().toLowerCase());
      }
    }

    // Section 6.2.5
    if (!allowedHTTPMethods.contains(accessControlRequestMethod)) {
      handleInvalidCORS(request, response, InvalidCORSReason.PreFlightMethodNotAllowed, accessControlRequestMethod);
      return;
    }

    // Section 6.2.6
    if (!accessControlRequestHeaders.isEmpty()) {
      for (String header : accessControlRequestHeaders) {
        if (!allowedHTTPHeaders.contains(header)) {
          handleInvalidCORS(request, response, InvalidCORSReason.PreFlightHeaderNotAllowed, header);
          return;
        }
      }
    }

    // Section 6.2.7
    if (supportsCredentials) {
      response.addHeader(Headers.AccessControlAllowOrigin, origin);
      response.addHeader(Headers.AccessControlAllowCredentials, "true");
      // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin
      // - Indicate to the browser that the server response can differ based on the value of the Origin request header.
      response.addHeader("Vary", "Origin");
    } else {
      if (anyOriginAllowed) {
        response.addHeader(Headers.AccessControlAllowOrigin, "*");
      } else {
        response.addHeader(Headers.AccessControlAllowOrigin, origin);
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin
        // - Indicate to the browser that the server response can differ based on the value of the Origin request header.
        response.addHeader("Vary", "Origin");
      }
    }

    // Section 6.2.8
    if (preflightMaxAge > 0) {
      response.addHeader(Headers.AccessControlMaxAge, String.valueOf(preflightMaxAge));
    }

    // Section 6.2.9
    response.addHeader(Headers.AccessControlAllowMethods, accessControlRequestMethod.toString());

    // Section 6.2.10
    if (!allowedHTTPHeaders.isEmpty()) {
      response.addHeader(Headers.AccessControlAllowHeaders, String.join(",", allowedHTTPHeadersOriginal));
    }

    response.setStatus(204);
    // Do not forward the request down the filter chain.
  }

  /**
   * Handles a CORS request of type {@link CORSRequestType}.SIMPLE.
   *
   * @param request       The {@link HTTPRequest} object.
   * @param response      The {@link HTTPResponse} object.
   * @param workflowChain The {@link WorkflowChain} object.
   * @see <a href="http://www.w3.org/TR/cors/#resource-requests">Simple Cross-Origin Request, Actual Request, and
   *     Redirects</a>
   */
  private void handleSimpleCORS(HTTPRequest request, HTTPResponse response, WorkflowChain workflowChain)
      throws IOException {
    String origin = request.getHeader(Headers.Origin);
    HTTPMethod method = request.getMethod();

    // Section 6.1.2
    if (!isOriginAllowed(origin)) {
      handleInvalidCORS(request, response, InvalidCORSReason.SimpleOriginNotAllowed, origin);
      return;
    }

    if (!allowedHTTPMethods.contains(method)) {
      handleInvalidCORS(request, response, InvalidCORSReason.SimpleMethodNotAllowed, method);
      return;
    }

    // Section 6.1.3
    // Add a single Access-Control-Allow-Origin header.
    if (anyOriginAllowed && !supportsCredentials) {
      // If resource doesn't support credentials and if any origin is allowed to make CORS request, return header with '*'.
      response.addHeader(Headers.AccessControlAllowOrigin, "*");
    } else {
      // If the resource supports credentials add a single Access-Control-Allow-Origin header, with the value of the Origin header as value.
      response.addHeader(Headers.AccessControlAllowOrigin, origin);
      // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin
      // - Indicate to the browser that the server response can differ based on the value of the Origin request header.
      response.addHeader("Vary", "Origin");
    }

    // Section 6.1.3
    // If the resource supports credentials, add a single Access-Control-Allow-Credentials header with the case-sensitive string "true" as value.
    if (supportsCredentials) {
      response.addHeader(Headers.AccessControlAllowCredentials, "true");
    }

    // Section 6.1.4
    // If the list of exposed headers is not empty add one or more Access-Control-Expose-Headers headers, with as values the header field names given in the list of exposed headers.
    if (exposedHeaders.size() > 0) {
      String exposedHeadersString = String.join(",", exposedHeaders);
      response.addHeader(Headers.AccessControlExposeHeaders, exposedHeadersString);
    }

    // Forward the request down the chain.
    workflowChain.continueWorkflow();
  }

  /**
   * Checks if the Origin is allowed to make a CORS request.
   *
   * @param origin The Origin.
   * @return <code>true</code> if origin is allowed; <code>false</code>
   *     otherwise.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean isOriginAllowed(final String origin) {
    if (anyOriginAllowed) {
      return true;
    }

    // If 'Origin' header is a case-sensitive match of any of allowed
    // origins, then return true, else return false.
    return allowedOrigins.contains(origin);
  }

  /**
   * Determine if request was made from the same origin as the server.
   *
   * @param origin  The value from the Origin HTTP header
   * @param request The {@link HTTPRequest} object.
   * @return True if the request is from the same origin.
   */
  private boolean isSameOrigin(String origin, HTTPRequest request) {
    // "null" is a valid origin, but it isn't "us"
    if ("null".equals(origin)) {
      return false;
    }

    // RFC6454, section 4. "If uri-scheme is file, the implementation MAY return an implementation-defined value.". No limits are placed on
    // that value so treat all file URIs as valid origins. FusionAuth will never have a file:// origin, not "us"
    if (origin.startsWith("file://")) {
      return false;
    }

    // Be a little defensive, if we throw an exception this is not "us"
    try {
      URI uri = URI.create(request.getBaseURL());
      URI originURI = URI.create(origin);
      return uri.getScheme().equalsIgnoreCase(originURI.getScheme()) & uri.getPort() == originURI.getPort() && uri.getHost().equalsIgnoreCase(originURI.getHost());
    } catch (Exception ignore) {
    }

    return false;
  }

  /**
   * Checks if a given origin is valid or not. Criteria:
   * <ul>
   * <li>If an encoded character is present in origin, it's not valid.</li>
   * <li>Origin should be a valid {@link URI}</li>
   * </ul>
   *
   * @param origin The origin header.
   * @return True if the origin is valid.
   * @see <a href="http://tools.ietf.org/html/rfc952">RFC952</a>
   */
  private boolean isValidOrigin(String origin) {
    // Checks for encoded characters. Helps prevent CRLF injection.
    if (origin.contains("%")) {
      return false;
    }

    // "null" is a valid origin
    if ("null".equals(origin)) {
      return true;
    }

    // RFC6454, section 4. "If uri-scheme is file, the implementation MAY return an implementation-defined value.". No limits are placed on
    // that value so treat all file URIs as valid origins.
    if (origin.startsWith("file://")) {
      return true;
    }

    URI originURI;
    try {
      originURI = new URI(origin);
    } catch (URISyntaxException e) {
      return false;
    }
    // If scheme for URI is null, return false. Return true otherwise.
    return originURI.getScheme() != null;
  }

  private void logRequest(HTTPRequest request, final InvalidCORSReason reason, final Object reasonValue) {
    if (debugger == null) {
      return;
    }

    // FusionAuth System Log debug
    String message = switch (reason) {
      case PreFlightUnexpected -> "Invalid request. Not expecting a preflight request from URI [" + reasonValue + "].";
      case SimpleMethodNotAllowed -> "Invalid Simple CORS request. HTTP method not allowed. [" + reasonValue + "]";
      case SimpleOriginNotAllowed -> "Invalid Simple CORS request. Origin not allowed. [" + reasonValue + "]";
      case PreFlightHeaderNotAllowed ->
          "Invalid CORS pre-flight request. HTTP header not allowed. [" + reasonValue + "]";
      case PreFlightMethodNotAllowed ->
          "Invalid CORS pre-flight request. HTTP method not allowed. [" + reasonValue + "]";
      case PreFlightMethodNotRecognized ->
          "Invalid CORS pre-flight request. HTTP method not recognized. [" + reasonValue + "]";
      case PreFlightOriginNotAllowed -> "Invalid CORS pre-flight request. Origin not allowed. [" + reasonValue + "]";
      case UnhandledCORSRequestType -> "Invalid request. Unhandled CORS request type [" + reasonValue + "].";
    };

    // Using defaultIfNull to handle the difference between null and "null". "null" is a valid origin, but I want to be able to tell the difference.
    debugger.disableTimestamp()
            .log(message)
            .log("")
            .log("Base URI: %s", URI.create(request.getBaseURL()))
            .log("HTTP Method: %s", request.getMethod())
            .log("URI: %s", request.getPath())
            .log("")
            .log("Content-Type header: %s", request.getHeader("Content-Type"))
            .log("Host header: %s", request.getHeader("Host"))
            .log("Origin header: %s", request.getHeader("Origin"))
            .log("Referer header: %s", request.getHeader("Referer"))
            .log("")
            .log("Remote host: %s", request.getHost())
            .log("IP address: %s", request.getIPAddress())
            .log("")
            .log("Header names: %s", String.join(",", request.getHeaders().keySet()));

    // One edge case we've seen in the field. If using POST w/out a Content-Type header, this will be considered an Invalid CORS request.
    // - This will show up as an UnhandledCORSRequestType. We could optionally re-factor a few things and then pass that all the way here, but
    //   this is likely adequate.
    if (request.getHeader("Content-Type") == null && reason == InvalidCORSReason.UnhandledCORSRequestType) {
      debugger.log("")
              .log("You are missing the Content-Type header during a POST request. This is an invalid CORS request and is the likely root cause of this failure.");
    }

    // Close it out.
    debugger.log("")
            .log("Return HTTP Status code 403.");

    if (logger.isDebugEnabled()) {
      //noinspection scwowasp-top-10_SLF4JLoggingenforceusageofplaceholdersinthemessages
      logger.debug(debugger.toString());
    }

    if (debug) {
      debugger.done();
    }
  }

  /**
   * Enumerates varies types of CORS requests. Also, provides utility methods to determine the request type.
   */
  public enum CORSRequestType {
    /**
     * A simple HTTP request, i.e. it shouldn't be pre-flighted.
     */
    SIMPLE,
    /**
     * A HTTP request that needs to be pre-flighted.
     */
    ACTUAL,
    /**
     * A pre-flight CORS request, to get meta information, before a non-simple HTTP request is sent.
     */
    PRE_FLIGHT,
    /**
     * Not a CORS request, but a normal request.
     */
    NOT_CORS,
    /**
     * An invalid CORS request, i.e. it qualifies to be a CORS request, but fails to be a valid one.
     */
    INVALID_CORS
  }

  enum InvalidCORSReason {
    PreFlightOriginNotAllowed,
    PreFlightUnexpected,
    PreFlightHeaderNotAllowed,
    PreFlightMethodNotAllowed,
    PreFlightMethodNotRecognized,
    SimpleOriginNotAllowed,
    SimpleMethodNotAllowed,
    UnhandledCORSRequestType
  }
}
