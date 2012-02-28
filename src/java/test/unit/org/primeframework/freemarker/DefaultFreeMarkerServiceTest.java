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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import freemarker.ext.beans.BeansWrapper;
import org.easymock.EasyMock;
import org.primeframework.config.PrimeMVCConfiguration;
import org.primeframework.container.ContainerResolver;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * <p>
 * This class test the default free marker service.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultFreeMarkerServiceTest {
    @Test
    public void testUse() {
        PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
        EasyMock.expect(config.freemarkerCheckSeconds()).andReturn(2);
        EasyMock.replay(config);

        ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
        EasyMock.expect(containerResolver.getRealPath("src/java/test/unit/org/jcatapult/freemarker/test_en_US.ftl")).andReturn("src/java/test/unit/org/jcatapult/freemarker/test.ftl");
        EasyMock.replay(containerResolver);

        DefaultFreeMarkerService service = new DefaultFreeMarkerService(config, new OverridingTemplateLoader(containerResolver));
        String result = service.render("src/java/test/unit/org/jcatapult/freemarker/test.ftl", new HashMap<String, Object>(), Locale.US);
        assertEquals("It worked!", result);
    }

    @Test
    public void objectWrapper() {
        PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
        EasyMock.expect(config.freemarkerCheckSeconds()).andReturn(2);
        EasyMock.replay(config);

        ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
        EasyMock.expect(containerResolver.getRealPath("src/java/test/unit/org/jcatapult/freemarker/test-with-bean_en_US.ftl")).andReturn("src/java/test/unit/org/jcatapult/freemarker/test-with-bean.ftl");
        EasyMock.replay(containerResolver);

        DefaultFreeMarkerService service = new DefaultFreeMarkerService(config,  new OverridingTemplateLoader(containerResolver));

        BeansWrapper ow = new BeansWrapper();
        ow.setExposeFields(true);
        ow.setSimpleMapWrapper(true);

        Bean bean = new Bean();
        bean.coolMap.put(1, "test");
        bean.setAge(42);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("bean", bean);
        String result = service.render("src/java/test/unit/org/jcatapult/freemarker/test-with-bean.ftl", context, Locale.US, ow);
        assertEquals("Bean 1 test test 42", result);
    }

    @Test
    public void defaultObjectWrapper() {
        PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
        EasyMock.expect(config.freemarkerCheckSeconds()).andReturn(2);
        EasyMock.replay(config);

        ContainerResolver containerResolver = EasyMock.createStrictMock(ContainerResolver.class);
        EasyMock.expect(containerResolver.getRealPath("src/java/test/unit/org/jcatapult/freemarker/test-with-bean_en_US.ftl")).andReturn("src/java/test/unit/org/jcatapult/freemarker/test-with-bean.ftl");
        EasyMock.replay(containerResolver);

        DefaultFreeMarkerService service = new DefaultFreeMarkerService(config, new OverridingTemplateLoader(containerResolver));

        Bean bean = new Bean();
        bean.coolMap.put(1, "test");
        bean.setAge(42);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("bean", bean);
        String result = service.render("src/java/test/unit/org/jcatapult/freemarker/test-with-bean.ftl", context, Locale.US);
        assertEquals("Bean 1 test test 42", result);
    }
}