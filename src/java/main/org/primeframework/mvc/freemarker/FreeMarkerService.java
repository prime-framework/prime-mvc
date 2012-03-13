/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.freemarker;

import java.io.Writer;
import java.util.Locale;

import com.google.inject.ImplementedBy;

/**
 * This service provides support for FreeMarker to Prime libraries and applications. In most cases callers need not
 * know anything about the implementation.
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultFreeMarkerService.class)
public interface FreeMarkerService {
  /**
   * Renders the given template. This method renders the template into a StringWriter and then returns the String.
   *
   * @param templateName The name of the template. Since this service is generic, this is the full path to the template.
   *                     For example, it would be <strong>/WEB-INF/emails/foo-html.ftl</strong>.
   * @param root         This can be a Map or a FreeMarker model type that provides the values to the template.
   * @param locale       The locale used to find the template.
   * @return The template.
   * @throws FreeMarkerRenderException If the render fails.
   * @throws MissingTemplateException  If the template is missing.
   */
  String render(String templateName, Object root, Locale locale)
    throws FreeMarkerRenderException, MissingTemplateException;

  /**
   * Renders the given template. This method renders the template into the given Writer.
   *
   * @param writer       The writer to output the render to.
   * @param templateName The name of the template. Since this service is generic, this is the full path to the template.
   *                     For example, it would be <strong>/WEB-INF/emails/foo-html.ftl</strong>.
   * @param root         This can be a Map or a FreeMarker model type that provides the values to the template.
   * @param locale       The locale used to find the template.
   * @throws FreeMarkerRenderException If the render fails.
   * @throws MissingTemplateException  If the template is missing.
   */
  void render(Writer writer, String templateName, Object root, Locale locale)
    throws FreeMarkerRenderException, MissingTemplateException;
}