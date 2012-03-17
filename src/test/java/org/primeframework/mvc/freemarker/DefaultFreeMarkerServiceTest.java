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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.freemarker.guice.FreeMarkerConfigurationProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This class test the default free marker service.
 *
 * @author Brian Pontarelli
 */
public class DefaultFreeMarkerServiceTest {
  @Test
  public void use() {
    MVCConfiguration config = EasyMock.createStrictMock(MVCConfiguration.class);
    EasyMock.expect(config.templateCheckSeconds()).andReturn(2);
    EasyMock.replay(config);

    ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(containerResolver.getRealPath("src/test/java/org/primeframework/mvc/freemarker/test_en_US.ftl")).andReturn("src/test/java/org/primeframework/mvc/freemarker/test.ftl");
    EasyMock.replay(containerResolver);

    DefaultFreeMarkerService service = new DefaultFreeMarkerService(new FreeMarkerConfigurationProvider(config, new OverridingTemplateLoader(containerResolver)).get());
    String result = service.render("src/test/java/org/primeframework/mvc/freemarker/test.ftl", new HashMap<String, Object>(), Locale.US);
    assertEquals(result, "It worked!");
  }

  @Test
  public void objectWrapper() {
    MVCConfiguration config = EasyMock.createStrictMock(MVCConfiguration.class);
    EasyMock.expect(config.templateCheckSeconds()).andReturn(2);
    EasyMock.replay(config);

    ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(containerResolver.getRealPath("src/test/java/org/primeframework/mvc/freemarker/test-with-bean_en_US.ftl")).andReturn("src/test/java/org/primeframework/mvc/freemarker/test-with-bean.ftl");
    EasyMock.replay(containerResolver);

    DefaultFreeMarkerService service = new DefaultFreeMarkerService(new FreeMarkerConfigurationProvider(config, new OverridingTemplateLoader(containerResolver)).get());

    Bean bean = new Bean();
    bean.coolMap.put(1, "test");
    bean.setAge(42);

    Map<String, Object> context = new HashMap<String, Object>();
    context.put("bean", bean);
    String result = service.render("src/test/java/org/primeframework/mvc/freemarker/test-with-bean.ftl", context, Locale.US);
    assertEquals(result, "Bean 1 test test 42");
  }

  @Test
  public void defaultObjectWrapper() {
    MVCConfiguration config = EasyMock.createStrictMock(MVCConfiguration.class);
    EasyMock.expect(config.templateCheckSeconds()).andReturn(2);
    EasyMock.replay(config);

    ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(containerResolver.getRealPath("src/test/java/org/primeframework/mvc/freemarker/test-with-bean_en_US.ftl")).andReturn("src/test/java/org/primeframework/mvc/freemarker/test-with-bean.ftl");
    EasyMock.replay(containerResolver);

    DefaultFreeMarkerService service = new DefaultFreeMarkerService(new FreeMarkerConfigurationProvider(config, new OverridingTemplateLoader(containerResolver)).get());

    Bean bean = new Bean();
    bean.coolMap.put(1, "test");
    bean.setAge(42);

    Map<String, Object> context = new HashMap<String, Object>();
    context.put("bean", bean);
    String result = service.render("src/test/java/org/primeframework/mvc/freemarker/test-with-bean.ftl", context, Locale.US);
    assertEquals(result, "Bean 1 test test 42");
  }
}