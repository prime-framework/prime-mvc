/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.util.URITools;

/**
 * Provides a String that is the extension of the request URI.
 *
 * @author Brian Pontarelli
 */
public class ExtensionProvider implements Provider<String> {
  private final HTTPRequest request;

  @Inject
  public ExtensionProvider(HTTPRequest request) {
    this.request = request;
  }

  @Override
  public String get() {
    String uri = request.getPath();
    String extension = URITools.determineExtension(uri);
    if (extension == null) {
      return "";
    }

    return extension;
  }
}
