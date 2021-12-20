/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
 *
 */
package org.primeframework.mvc.message.l10n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.container.ContainerResolver;
import static java.util.Collections.singletonList;

/**
 * This class handles loading of resource bundles using the servlet context from the directory WEB-INF/messages.
 * <p>
 * This reloads based on the setting in the {@link MVCConfiguration} interface.
 *
 * @author Brian Pontarelli
 */
public class WebControl extends ResourceBundle.Control {
  private final MVCConfiguration configuration;

  private final ContainerResolver containerResolver;

  private final long reloadCheckSeconds;

  @Inject
  public WebControl(ContainerResolver containerResolver, MVCConfiguration configuration) {
    this.configuration = configuration;
    this.containerResolver = containerResolver;
    this.reloadCheckSeconds = configuration.l10nReloadSeconds();
  }

  /**
   * Always returns null because there are no fallback locales.
   *
   * @param uri    Not used.
   * @param locale Not used.
   * @return Always null.
   */
  @Override
  public Locale getFallbackLocale(String uri, Locale locale) {
    return null;
  }

  /**
   * Only properties.
   *
   * @param uri Not used.
   * @return An array containing only "java.properties".
   */
  @Override
  public List<String> getFormats(String uri) {
    return singletonList("java.properties");
  }

  /**
   * See class comment for reload information.
   *
   * @param baseName Not used.
   * @param locale   Not used.
   * @return The reload check seconds multiplied times 1000.
   */
  @Override
  public long getTimeToLive(String baseName, Locale locale) {
    return reloadCheckSeconds * 1000;
  }

  /**
   * Returns true if the ServletContext getRealPath method returns non-null for the bundle name and the file has been
   * modified since the last load.
   *
   * @param uri      The current URI, used to construct the bundle name.
   * @param locale   The locale used to find the properties file.
   * @param format   Not used.
   * @param loader   Not used.
   * @param bundle   Not used.
   * @param loadTime Not used.
   * @return True if the file needs a reload.
   */
  @Override
  public boolean needsReload(String uri, Locale locale, String format, ClassLoader loader, ResourceBundle bundle,
                             long loadTime) {
    // Create the bundle from the WEB-INF/messages folder. basename is the uri
    String name = name(uri, locale);
    String realPath = containerResolver.getRealPath(name);
    if (realPath != null) {
      File file = new File(realPath);
      long time = file.lastModified();
      return time > loadTime;
    }

    return false;
  }

  /**
   * First tries to load the bundle using the getRealPath method on the Context If that doesn't work because the
   * application is a WAR, this uses the getResourceAsStream method on the Context
   *
   * @param uri    The current URI.
   * @param locale The locale used to find the properties file.
   * @param format Not used.
   * @param loader Used to lookup the resource if the ContainerResolver can't find it.
   * @param reload Not used.
   * @return The property resource bundle and never null.
   * @throws IOException              If the file couldn't be read.
   * @throws IllegalArgumentException If the bundle doesn't exist.
   */
  @Override
  public ResourceBundle newBundle(String uri, Locale locale, String format, ClassLoader loader, boolean reload)
      throws IOException {
    // Create the bundle from the ${configuration.resourceDirectory}/messages folder. basename is the uri
    String name = name(uri, locale);
    String realPath = containerResolver.getRealPath(name);
    if (realPath != null) {
      File file = new File(realPath);
      if (file.isFile()) {
        return new PropertyResourceBundle(new FileInputStream(file));
      }
    }

    URL url = containerResolver.getResource(name);
    if (url == null) {
      // Otherwise, check the classpath
      url = loader.getResource(name.substring(1));
    }

    if (url != null) {
      return new PropertyResourceBundle(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
    }

    throw new PrimeException("Invalid bundle [" + uri + "]");
  }

  /**
   * Makes the file name.
   *
   * @param uri    The current URI.
   * @param locale The locale.
   * @return The file name.
   */
  private String name(String uri, Locale locale) {
    // Normaly URIs like /foo/ to /foo/index
    if (uri.endsWith("/")) {
      uri = uri + "index";
    }

    return configuration.messageDirectory() + toBundleName(uri, locale) + ".properties";
  }
}