/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.l10n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import static java.util.Arrays.*;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.jcatapult.config.Configuration;
import org.jcatapult.container.ContainerResolver;

import com.google.inject.Inject;

/**
 * <p>
 * This class handles loading of resource bundles using the servlet
 * context from the directory WEB-INF/messages.
 * </p>
 *
 * <p>
 * This reloads by default. The check interval is set to 1 second by default.
 * This interval can be controlled using the configuration property named:
 * </p>
 *
 * <pre>
 * jcatapult.l10n.reload-check-seconds
 * </pre>
 *
 * @author  Brian Pontarelli
 */
public class WebControl extends ResourceBundle.Control {
    private final ContainerResolver containerResolver;
    private final long reloadCheckSeconds;

    @Inject
    public WebControl(ContainerResolver containerResolver, Configuration configuration) {
        this.containerResolver = containerResolver;
        this.reloadCheckSeconds = configuration.getLong("jcatapult.l10n.reload-check-seconds", 1);
    }

    /**
     * Only properties.
     *
     * @param   uri Not used.
     * @return  An array containing only "java.properties".
     */
    @Override
    public List<String> getFormats(String uri) {
        return asList("java.properties");
    }

    /**
     * Always returns null because there are no fallback locales.
     *
     * @param   uri Not used.
     * @param   locale Not used.
     * @return  Always null.
     */
    @Override
    public Locale getFallbackLocale(String uri, Locale locale) {
        return null;
    }

    /**
     * First tries to load the bundle using the getRealPath method on the ServletContext. If that
     * doesn't work because the application is a WAR, this uses the getResourceAsStream method on
     * the ServletContext.
     *
     * @param   uri The current URI.
     * @param   locale Not used.
     * @param   format Not used.
     * @param   loader Not used.
     * @param   reload Not used.
     * @return  The property resource bundle and never null.
     * @throws  IOException If the file couldn't be read.
     * @throws  IllegalArgumentException If the bundle doesn't exist.
     */
    @Override
    public ResourceBundle newBundle(String uri, Locale locale, String format, ClassLoader loader, boolean reload)
    throws IOException {
        // Create the bundle from the WEB-INF/messages folder. basename is the uri
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
            return new PropertyResourceBundle(url.openStream());
        }

        throw new IllegalArgumentException("Invalid bundle [" + uri + "]");
    }

    /**
     * See class comment for reload information.
     *
     * @param   baseName Not used.
     * @param   locale Not used.
     * @return  The reload check seconds multiplied times 1000.
     */
    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        return reloadCheckSeconds * 1000;
    }

    /**
     * Returns true if the ServletContext getRealPath method returns non-null for the bundle name
     * and the file has been modified since the last load.
     *
     * @param   uri The current URI, used to construct the bundle name.
     * @param   locale Not used.
     * @param   format Not used.
     * @param   loader Not used.
     * @param   bundle Not used.
     * @param   loadTime Not used.
     * @return  True if the file needs a reload.
     */
    @Override
    public boolean needsReload(String uri, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
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
     * Makes the file name.
     *
     * @param   uri The current URI.
     * @param   locale The locale.
     * @return  The file name.
     */
    private String name(String uri, Locale locale) {
        // Normaly URIs like /foo/ to /foo/index
        if (uri.endsWith("/")) {
            uri = uri + "index";
        }

        return "/WEB-INF/message" + toBundleName(uri, locale) + ".properties";
    }
}