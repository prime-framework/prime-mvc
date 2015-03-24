/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
import java.util.HashMap;
import java.util.Map;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Daniel DeGroff
 */
public final class BodyTools {

  static final Configuration configuration;

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

  public static String processTemplate(Path path, Object... values) throws IOException {
    StringWriter writer = new StringWriter();
    Template template = configuration.getTemplate(path.toAbsolutePath().toString());
    Map<String, Object> map = new HashMap<>(values.length / 2);
    if (values.length % 2 != 0) {
      String key = values[values.length - 1].toString();
      throw new RuntimeException("Missing value for key [" + key + "]");
    }
    for (int i = 0; i < values.length; i += 2) {
      map.put(values[i].toString(), values[i + 1]);
    }
    try {
      template.process(map, writer);
      return writer.toString();
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    }
  }

}
