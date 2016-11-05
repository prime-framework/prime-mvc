/*
 * Copyright (c) 2015-2016, Inversoft Inc., All Rights Reserved
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
import java.nio.file.Path;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Helper methods for processing JSON files that contain replacement variables.
 *
 * @author Daniel DeGroff
 */
public final class BodyTools {

  private static final Configuration configuration;

  /**
   * Process the FreeMarker template (JSON) and return the rendered string. <p>Example usage when a single replacement
   * value exists in the json named ${id} :</p>
   * <pre>
   *   BodyTools.processTemplate(Paths.get("/foo.json"), "id", "ffffffff-1e16-4b1d-88f3-ec68ad1200e2");
   * </pre>
   *
   * @param path   {@Path} to the FreeMarker template.
   * @param values Key value pairs of replacement values.
   * @return
   * @throws IOException
   */
  public static String processTemplate(Path path, Object... values) throws IOException {
    return processTemplateWithMap(path, new TestRootMap(values));
  }

  /**
   * Process the FreeMarker template (JSON) and return the rendered string. <p>Example usage when a single replacement
   * value exists in the json named ${id} :</p>
   * <pre>
   *   BodyTools.processTemplateWithMap(Paths.get("/foo.json"), MapBuilder.&lt;String, Object&gt;map()
   *     .put("id", "ffffffff-1e16-4b1d-88f3-ec68ad1200e2")
   *     .done());
   * </pre>
   *
   * @param path   {@Path} to the FreeMarker template.
   * @param values Map of key value pairs of replacement values.
   * @return
   * @throws IOException
   */
  public static String processTemplateWithMap(Path path, Object values) throws IOException {
    StringWriter writer = new StringWriter();
    Template template = configuration.getTemplate(path.toAbsolutePath().toString());
    try {
      template.process(values, writer);
      return writer.toString();
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    }
  }

  static {
    configuration = new Configuration();
    configuration.setDefaultEncoding("UTF-8");
    configuration.setNumberFormat("computer");
    configuration.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
    try {
      configuration.setTemplateLoader(new FileTemplateLoader(new File("/")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
