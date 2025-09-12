/*
 * Copyright (c) 2015-2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import freemarker.cache.FileTemplateLoader;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * Helper methods for processing JSON files that contain replacement variables.
 *
 * @author Daniel DeGroff
 */
public final class BodyTools {
  private static final Configuration config;

  /**
   * Process the FreeMarker template (JSON) and return the rendered string. <p>Example usage when a single replacement value exists in the json named
   * ${id} :</p>
   * <pre>
   *   BodyTools.processTemplate(Path.of("/foo.json"), "id", "ffffffff-1e16-4b1d-88f3-ec68ad1200e2");
   * </pre>
   *
   * @param path   Path to the FreeMarker template.
   * @param values Key value pairs of replacement values.
   * @return The result of executing the template.
   * @throws IOException If the template could not be loaded, parsed or executed.
   */
  public static String processTemplate(Path path, Object... values) throws IOException {
    return processTemplateWithMap(path, toMap(values), false);
  }

  /**
   * Process the FreeMarker template (JSON) and return the rendered string. <p>Example usage when a single replacement value exists in the json named
   * ${id} :</p>
   * <pre>
   *   BodyTools.processTemplate(Path.of("/foo.json"), "id", "ffffffff-1e16-4b1d-88f3-ec68ad1200e2");
   * </pre>
   *
   * @param path   Path to the FreeMarker template.
   * @param values Key value pairs of replacement values.
   * @return The result of executing the template.
   * @throws IOException If the template could not be loaded, parsed or executed.
   */
  public static String processTemplateForAssertion(Path path, Object... values) throws IOException {
    return processTemplateWithMap(path, toMap(values), true);
  }

  /**
   * Process the FreeMarker template (JSON) and return the rendered string. <p>Example usage when a single replacement value exists in the json named
   * ${id} :</p>
   * <pre>
   *   BodyTools.processTemplateWithMap(Path.of("/foo.json"), MapBuilder.&lt;String, Object&gt;map()
   *     .put("id", "ffffffff-1e16-4b1d-88f3-ec68ad1200e2")
   *     .done());
   * </pre>
   *
   * @param path                   Path to the FreeMarker template.
   * @param values                 Map of key value pairs of replacement values.
   * @param createMissingTemplates set true to create templates when they do not exists
   * @return The result of executing the template.
   * @throws IOException If the template could not be loaded, parsed or executed.
   */
  public static String processTemplateWithMap(Path path, DetectionMap values, boolean createMissingTemplates)
      throws IOException {
    StringWriter writer = new StringWriter();
    Template template = null;
    try {
      template = config.getTemplate(path.toAbsolutePath().toString());
    } catch (TemplateNotFoundException e) {
      if (Files.notExists(path.toAbsolutePath())) {
        Files.writeString(path.toAbsolutePath(), "{\"prime-mvc-auto-generated\": true}", StandardOpenOption.SYNC, StandardOpenOption.DSYNC, StandardOpenOption.CREATE_NEW);
        config.clearTemplateCache();
        template = config.getTemplate(path.toAbsolutePath().toString());
      }
    }

    try {
      template.process(values, writer);
      // used in some classes like RequestResult, and should be ignored
      Set<Object> unusedVariables = values.getUnusedVariables("_to_milli");
      if (!unusedVariables.isEmpty()) {
        throw new IllegalArgumentException("The following variables are not used in the [%s] template: %s".formatted(path,
                                                                                                                     unusedVariables));
      }
      return writer.toString();
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Take array of objects assumed to be in pairs of String, Object and build and return a map.
   *
   * @param values The array of values.
   * @return The Map.
   */
  private static DetectionMap toMap(Object... values) {
    if (values.length % 2 != 0) {
      String key = values[values.length - 1].toString();
      throw new IllegalArgumentException("Invalid mapping values. Must have a multiple of 2. Missing value for key [" + key + "]");
    }

    DetectionMap map = new DetectionMap();
    for (int i = 0; i < values.length; i = i + 2) {
      map.put(values[i].toString(), values[i + 1]);
    }

    return map;
  }

  static {
    BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_32);
    builder.setExposeFields(true);
    builder.setSimpleMapWrapper(true);

    config = new Configuration(Configuration.VERSION_2_3_32);
    config.setDefaultEncoding("UTF-8");
    config.setNumberFormat("computer");
    config.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
    config.setObjectWrapper(builder.build());
    config.setNumberFormat("computer");
    try {
      config.setTemplateLoader(new FileTemplateLoader(new File("/")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
