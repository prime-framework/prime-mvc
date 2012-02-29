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
 */
package org.primeframework.mvc.l10n;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.container.ServletContainerResolver;
import org.primeframework.mock.servlet.MockServletContext;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;
import static org.testng.Assert.*;

/**
 * <p> This class tests the resource bundle message provider. </p>
 *
 * @author Brian Pontarelli
 */
public class ResourceBundleMessageProviderTest {
  @Test
  public void testSearch() {
    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.l10nReloadSeconds()).andReturn(1).times(2);
    EasyMock.replay(config);

    MockServletContext context = new MockServletContext(new File("src/java/test/unit"));

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config));
    assertEquals("American English Message", provider.getMessage("/l10n/Test", "key"));
    assertEquals("Package Message", provider.getMessage("/l10n/NonExistent", "key"));
    assertEquals("Super Package Message", provider.getMessage("/badPackage/Test", "key"));

    provider = new ResourceBundleMessageProvider(Locale.GERMAN, new WebControl(new ServletContainerResolver(context), config));
    assertEquals("Default Message", provider.getMessage("/l10n/Test", "key"));
  }

  @Test
  public void testFormat() {
    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.l10nReloadSeconds()).andReturn(1).times(2);
    EasyMock.replay(config);

    MockServletContext context = new MockServletContext(new File("src/java/test/unit"));

    Map<String, String> attributes = map("c", "c", "a", "a");
    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config));
    assertEquals("American English Message b a c", provider.getMessage("/l10n/Test", "format_key", attributes, "b"));
    assertEquals("Package Message b a c", provider.getMessage("/l10n/NonExistent", "format_key", attributes, "b"));
    assertEquals("Super Package Message b a c", provider.getMessage("/badPackage/Test", "format_key", attributes, "b"));

    provider = new ResourceBundleMessageProvider(Locale.GERMAN, new WebControl(new ServletContainerResolver(context), config));
    assertEquals("Default Message b a c", provider.getMessage("/l10n/Test", "format_key", attributes, "b"));
  }

  @Test
  public void testMissing() {
    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.l10nReloadSeconds()).andReturn(1);
    EasyMock.replay(config);

    MockServletContext context = new MockServletContext(new File("src/java/test/unit"));

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config));
    try {
      provider.getMessage("/l10n/Test", "bad_key");
      fail("Should have failed");
    } catch (MissingMessageException e) {
      // Expected
    }
  }
}