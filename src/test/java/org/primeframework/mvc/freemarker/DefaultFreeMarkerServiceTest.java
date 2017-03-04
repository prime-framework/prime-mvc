/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.freemarker.guice.FreeMarkerConfigurationProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * This class test the default free marker service.
 *
 * @author Brian Pontarelli
 */
public class DefaultFreeMarkerServiceTest {
  @Test
  public void locale() {
    MVCConfiguration config = EasyMock.createStrictMock(MVCConfiguration.class);
    EasyMock.expect(config.templateCheckSeconds()).andReturn(2);
    EasyMock.replay(config);

    ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(containerResolver.getRealPath("src/test/java/org/primeframework/mvc/freemarker/test-locale_fr.ftl")).andReturn("src/test/java/org/primeframework/mvc/freemarker/test-locale.ftl");
    EasyMock.replay(containerResolver);

    DefaultFreeMarkerService service = new DefaultFreeMarkerService(new FreeMarkerConfigurationProvider(config, new OverridingTemplateLoader(containerResolver)).get(), Locale.FRENCH);

    Map<String, Object> context = new HashMap<String, Object>();
    StringWriter writer = new StringWriter();
    service.render(writer, "src/test/java/org/primeframework/mvc/freemarker/test-locale.ftl", context);
    assertEquals(writer.toString(), "3,14");
  }

  @Test
  public void objectWrapper() {
    MVCConfiguration config = EasyMock.createStrictMock(MVCConfiguration.class);
    EasyMock.expect(config.templateCheckSeconds()).andReturn(2);
    EasyMock.replay(config);

    ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(containerResolver.getRealPath("src/test/java/org/primeframework/mvc/freemarker/test-with-bean_en_US.ftl")).andReturn("src/test/java/org/primeframework/mvc/freemarker/test-with-bean.ftl");
    EasyMock.replay(containerResolver);

    DefaultFreeMarkerService service = new DefaultFreeMarkerService(new FreeMarkerConfigurationProvider(config, new OverridingTemplateLoader(containerResolver)).get(), Locale.US);

    Bean bean = new Bean();
    bean.coolMap.put(1, "test");
    bean.setAge(42);

    Map<String, Object> context = new HashMap<>();
    context.put("bean", bean);
    StringWriter writer = new StringWriter();
    service.render(writer, "src/test/java/org/primeframework/mvc/freemarker/test-with-bean.ftl", context);
    assertEquals(writer.toString(), "Bean 1 test test 42");
  }

  @Test
  public void use() {
    MVCConfiguration config = EasyMock.createStrictMock(MVCConfiguration.class);
    EasyMock.expect(config.templateCheckSeconds()).andReturn(2);
    EasyMock.replay(config);

    ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(containerResolver.getRealPath("src/test/java/org/primeframework/mvc/freemarker/test_en_US.ftl")).andReturn("src/test/java/org/primeframework/mvc/freemarker/test.ftl");
    EasyMock.replay(containerResolver);

    DefaultFreeMarkerService service = new DefaultFreeMarkerService(new FreeMarkerConfigurationProvider(config, new OverridingTemplateLoader(containerResolver)).get(), Locale.US);
    StringWriter writer = new StringWriter();
    service.render(writer, "src/test/java/org/primeframework/mvc/freemarker/test.ftl", new HashMap<String, Object>());
    assertEquals(writer.toString(), "It worked!");
  }
}