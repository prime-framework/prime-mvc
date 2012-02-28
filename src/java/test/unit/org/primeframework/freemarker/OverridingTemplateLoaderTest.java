/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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

import java.io.File;
import java.io.IOException;

import org.easymock.EasyMock;
import org.primeframework.container.ContainerResolver;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * <p>
 * This tests the OverridingTemplateLoader class.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class OverridingTemplateLoaderTest {
    @Test
    public void testRealPath() throws IOException {
        ContainerResolver resolver = EasyMock.createStrictMock(ContainerResolver.class);
        EasyMock.expect(resolver.getRealPath("project.xml")).andReturn("project.xml");
        EasyMock.replay(resolver);

        OverridingTemplateLoader loader = new OverridingTemplateLoader(resolver);
        URLTemplateSource source = (URLTemplateSource) loader.findTemplateSource("project.xml");
        File file = new File("project.xml");
        assertEquals(file.lastModified(), loader.getLastModified(source));
        assertNotNull(loader.getReader(source, "UTF-8"));
        loader.closeTemplateSource(source);

        EasyMock.verify(resolver);
    }

    @Test
    public void testResource() throws IOException {
        ContainerResolver resolver = EasyMock.createStrictMock(ContainerResolver.class);
        EasyMock.expect(resolver.getRealPath("project.xml")).andReturn(null);
        EasyMock.expect(resolver.getResource("project.xml")).andReturn(new File("project.xml").toURI().toURL());
        EasyMock.replay(resolver);

        OverridingTemplateLoader loader = new OverridingTemplateLoader(resolver);
        URLTemplateSource source = (URLTemplateSource) loader.findTemplateSource("project.xml");
        File file = new File("project.xml");
        assertEquals(file.lastModified(), loader.getLastModified(source));
        assertNotNull(loader.getReader(source, "UTF-8"));
        loader.closeTemplateSource(source);

        EasyMock.verify(resolver);
    }

    @Test
    public void testClassPath() throws IOException {
        ContainerResolver resolver = EasyMock.createStrictMock(ContainerResolver.class);
        EasyMock.expect(resolver.getRealPath("jcatapult-default.properties")).andReturn(null);
        EasyMock.expect(resolver.getResource("jcatapult-default.properties")).andReturn(null);
        EasyMock.replay(resolver);

        OverridingTemplateLoader loader = new OverridingTemplateLoader(resolver);
        URLTemplateSource source = (URLTemplateSource) loader.findTemplateSource("jcatapult-default.properties");
        File file = new File("target/classes/main/jcatapult-default.properties");
        assertTrue(loader.getLastModified(source) > file.lastModified());
        assertEquals(loader.getLastModified(source), loader.getLastModified(source));
        assertNotNull(loader.getReader(source, "UTF-8"));
        loader.closeTemplateSource(source);

        EasyMock.verify(resolver);
    }
}