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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import org.primeframework.mvc.config.MVCConfiguration;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * This is a simple FreeMarkerService implementation. It uses two configuration parameters and the {@link
 * OverridingTemplateLoader} to provide FreeMarker rendering capabilities.
 * <p/>
 * <h3>Caching</h3> By default, FreeMarker provides caching for all templates. This class uses that caching by default
 * as well. However, you can also change this class to not cache at all by setting the check seconds to a negative
 * number.
 * <p/>
 * <strong>NOTE</strong> If you set the check seconds to a negative number, the templates are still cached but just
 * reloaded each time. This will incur memory a penalty.
 * <p/>
 * The configuration interface defines the caching and reloading strategy for this service via the method {@link
 * MVCConfiguration#templateCheckSeconds}.
 *
 * @author Brian Pontarelli
 */
public class DefaultFreeMarkerService implements FreeMarkerService {
  private final Configuration configuration;
  private final Locale locale;

  @Inject
  public DefaultFreeMarkerService(Configuration configuration, Locale locale) {
    this.configuration = configuration;
    this.locale = locale;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(Writer writer, String templateName, Object root) throws FreeMarkerRenderException, MissingTemplateException {
    try {
      Template template = configuration.getTemplate(templateName, locale);
      template.process(root, writer);
    } catch (FileNotFoundException fnfe) {
      throw new MissingTemplateException(fnfe);
    } catch (IOException e) {
      throw new FreeMarkerRenderException(e);
    } catch (TemplateException e) {
      throw new FreeMarkerRenderException(e);
    }
  }
}
