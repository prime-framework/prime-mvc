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
package org.primeframework.freemarker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.primeframework.config.PrimeMVCConfiguration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * This is a simple FreeMarkerService implementation. It uses two configuration parameters and the {@link
 * OverridingTemplateLoader} to provide FreeMarker rendering capabiilities.
 * <p/>
 * <h3>Caching</h3> By default, FreeMarker provides caching for all templates. This class uses that caching by default
 * as well. However, you can also change this class to not cache at all by setting the check seconds to a negative
 * number.
 * <p/>
 * <strong>NOTE</strong> If you set the check seconds to a negative number, the templates are still cached but just
 * reloaded each time. This will incur memory a penalty.
 * <p/>
 * The configuration parameters are:
 * <dl>
 *   <dt>jcatapult.freemarker-service.check-seconds</dt>
 *   <dd> An integer that controls the number of seconds that the templates are checked for new versions. If you don't
 *   specify this configuration parameter a default is determined. The way that this is determine is based on the
 *   environment. If the environment is <strong> production</strong>, this returns Integer.MAX_VALUE. Otherwise it
 *   returns <strong>2</strong>. Therefore, the templates are reloaded quite frequently in development but never
 *   reloaded in production. </dd>
 * </dl>
 *
 * @author Brian Pontarelli
 */
@Singleton
public class DefaultFreeMarkerService implements FreeMarkerService {
  private final freemarker.template.Configuration freeMarkerConfiguration = new freemarker.template.Configuration();

  @Inject
  public DefaultFreeMarkerService(PrimeMVCConfiguration configuration, OverridingTemplateLoader loader) {
    int checkSeconds = configuration.freemarkerCheckSeconds();
    this.freeMarkerConfiguration.setTemplateUpdateDelay(checkSeconds);
    this.freeMarkerConfiguration.setTemplateLoader(loader);
    this.freeMarkerConfiguration.setDefaultEncoding("UTF-8");
  }

  /**
   * {@inheritDoc}
   */
  public String render(String templateName, Object root, Locale locale)
    throws FreeMarkerRenderException, MissingTemplateException {
    StringWriter writer = new StringWriter();
    render(writer, templateName, root, locale, null);
    return writer.toString();
  }

  /**
   * {@inheritDoc}
   */
  public void render(Writer writer, String templateName, Object root, Locale locale)
    throws FreeMarkerRenderException, MissingTemplateException {
    render(writer, templateName, root, locale, null);
  }

  /**
   * {@inheritDoc}
   */
  public String render(String templateName, Object root, Locale locale, ObjectWrapper objectWrapper)
    throws FreeMarkerRenderException, MissingTemplateException {
    StringWriter writer = new StringWriter();
    render(writer, templateName, root, locale, objectWrapper);
    return writer.toString();
  }

  public void render(Writer writer, String templateName, Object root, Locale locale, ObjectWrapper objectWrapper)
    throws FreeMarkerRenderException, MissingTemplateException {
    try {
      Template template = freeMarkerConfiguration.getTemplate(templateName, locale);
      if (objectWrapper != null) {
        template.setObjectWrapper(objectWrapper);
      } else {
        template.setObjectWrapper(FieldSupportBeansWrapper.INSTANCE);
      }

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
