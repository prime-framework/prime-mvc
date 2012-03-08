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

import java.io.File;
import java.io.IOException;

import org.easymock.EasyMock;
import org.primeframework.mvc.container.ContainerResolver;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This tests the OverridingTemplateLoader class.
 *
 * @author Brian Pontarelli
 */
public class OverridingTemplateLoaderTest {
  @Test
  public void realPath() throws IOException {
    ContainerResolver resolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(resolver.getRealPath("project.xml")).andReturn("project.xml");
    EasyMock.replay(resolver);

    OverridingTemplateLoader loader = new OverridingTemplateLoader(resolver);
    URLTemplateSource source = (URLTemplateSource) loader.findTemplateSource("project.xml");
    File file = new File("project.xml");
    assertEquals(loader.getLastModified(source), file.lastModified());
    assertNotNull(loader.getReader(source, "UTF-8"));
    loader.closeTemplateSource(source);

    EasyMock.verify(resolver);
  }

  @Test
  public void resource() throws IOException {
    ContainerResolver resolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(resolver.getRealPath("project.xml")).andReturn(null);
    EasyMock.expect(resolver.getResource("project.xml")).andReturn(new File("project.xml").toURI().toURL());
    EasyMock.replay(resolver);

    OverridingTemplateLoader loader = new OverridingTemplateLoader(resolver);
    URLTemplateSource source = (URLTemplateSource) loader.findTemplateSource("project.xml");
    File file = new File("project.xml");
    assertEquals(loader.getLastModified(source), file.lastModified());
    assertNotNull(loader.getReader(source, "UTF-8"));
    loader.closeTemplateSource(source);

    EasyMock.verify(resolver);
  }

  @Test
  public void classPath() throws IOException {
    ContainerResolver resolver = EasyMock.createStrictMock(ContainerResolver.class);
    EasyMock.expect(resolver.getRealPath("logging.properties")).andReturn("target/classes/test/unit/logging.properties");
    EasyMock.replay(resolver);

    OverridingTemplateLoader loader = new OverridingTemplateLoader(resolver);
    URLTemplateSource source = (URLTemplateSource) loader.findTemplateSource("logging.properties");
    File file = new File("src/conf/test/unit/logging.properties");
    assertTrue(loader.getLastModified(source) > file.lastModified());
    assertEquals(loader.getLastModified(source), loader.getLastModified(source));
    assertNotNull(loader.getReader(source, "UTF-8"));
    loader.closeTemplateSource(source);

    EasyMock.verify(resolver);
  }
}