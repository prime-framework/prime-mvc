/*
 * Copyright 2007 (c) by Texture Media, Inc.
 *
 * This software is confidential and proprietary to
 * Texture Media, Inc. It may not be reproduced,
 * published or disclosed to others without company
 * authorization.
 */
package org.primeframework.mvc.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.primeframework.mvc.container.ContainerResolver;

import com.google.inject.Inject;
import freemarker.cache.TemplateLoader;

/**
 * This class is a free marker template loader that uses the {@link ContainerResolver} interface and the current context
 * ClassLoader to find the templates. This provides override support by looking in the container first and then the
 * ClassLoader.
 *
 * @author Brian Pontarelli
 */
public class OverridingTemplateLoader implements TemplateLoader {
  private final ContainerResolver containerResolver;

  /**
   * Creates a resource template loader that will use the specified container resolver to load the resources as well as
   * the current threads context class loader.
   *
   * @param containerResolver The container resolver to use to find the files.
   */
  @Inject
  public OverridingTemplateLoader(ContainerResolver containerResolver) {
    this.containerResolver = containerResolver;
  }

  /**
   * First looks in the container using the {@link ContainerResolver} to get the real path to a File on the file system.
   * If that works, it creates a URL out of the File and returns a new URLTemplateSource from that URL.
   * <p/>
   * If that fails, it tries getting the resource URL frm the {@link ContainerResolver}. If that works, it creates a
   * URLTemplateSource from that URL.
   * <p/>
   * If that fails, it tries to get the resource from the current threads context classloader. If that works, it creates
   * a URLTemplateSource from that URL.
   *
   * @param name The name of the template.
   * @return The template or null if it doesn't exist.
   * @throws IOException If the template could not be resolved.
   */
  public Object findTemplateSource(String name) throws IOException {
    // First try to open as plain file.
    try {
      String realPath = containerResolver.getRealPath(name);
      if (realPath != null) {
        File file = new File(realPath);
        if (file.isFile() && file.canRead()) {
          return new URLTemplateSource(file.toURI().toURL());
        }
      }
    } catch (SecurityException e) {
      // This means we couldn't access to the file according to the security manager (not the file system). In this case
      // we skip this lookup and move to the classpath and other lookups.
    }

    // If it fails, try to open it with context.getResource.
    URL url = containerResolver.getResource(name);
    if (url == null) {
      // If that fails, finally try looking it up in the class path
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      url = cl.getResource(name);
    }

    return url == null ? null : new URLTemplateSource(url);
  }

  /**
   * Gets the last modified from the URLTemplateSource.
   *
   * @param templateSource The URLTemplateSource.
   * @return The last modified from the URLConnection inside the URLTemplateSource.
   */
  public long getLastModified(Object templateSource) {
    return ((URLTemplateSource) templateSource).lastModified();
  }

  /**
   * Returns the InputStream from the URLTemplateSource wrapped in an InputStreamReader.
   *
   * @param templateSource The URLTemplateSource.
   * @param encoding       Used to construct the InputStreamReader.
   * @return The reader.
   * @throws IOException if the Reader couldn't be created.
   */
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    return new InputStreamReader(((URLTemplateSource) templateSource).getInputStream(), encoding);
  }

  /**
   * Calls close on the URLTemplateSource.
   *
   * @param templateSource The URLTemplateSource.
   * @throws IOException If the close fails.
   */
  public void closeTemplateSource(Object templateSource) throws IOException {
    ((URLTemplateSource) templateSource).close();
  }
}