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

import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.container.ServletContainerResolver;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.message.l10n.ResourceBundleMessageProvider;
import org.primeframework.mvc.message.l10n.WebControl;
import org.primeframework.mvc.util.MapBuilder;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the resource bundle message provider.
 *
 * @author Brian Pontarelli
 */
public class ResourceBundleMessageProviderTest {
  @Test
  public void search() {
    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.l10nReloadSeconds()).andReturn(1).times(2);
    replay(config);

    MockServletContext context = new MockServletContext(new File("src/java/test/unit"));

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/NonExistent", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/badPackage/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    replay(store);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals("American English Message", provider.getMessage("key"));
    assertEquals("Package Message", provider.getMessage("key"));
    assertEquals("Super Package Message", provider.getMessage("key"));

    provider = new ResourceBundleMessageProvider(Locale.GERMAN, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals("Default Message", provider.getMessage("key"));
  }

  @Test
  public void format() {
    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.l10nReloadSeconds()).andReturn(1).times(2);
    replay(config);

    MockServletContext context = new MockServletContext(new File("src/java/test/unit"));

    Map<String, String> attributes = MapBuilder.asMap("c", "c", "a", "a");

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/NonExistent", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/badPackage/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    replay(store);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals("American English Message b a c", provider.getMessage("format_key", attributes, "b"));
    assertEquals("Package Message b a c", provider.getMessage("format_key", attributes, "b"));
    assertEquals("Super Package Message b a c", provider.getMessage("format_key", attributes, "b"));

    provider = new ResourceBundleMessageProvider(Locale.GERMAN, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals("Default Message b a c", provider.getMessage("format_key", attributes, "b"));
  }

  @Test
  public void missing() {
    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.l10nReloadSeconds()).andReturn(1);
    replay(config);

    MockServletContext context = new MockServletContext(new File("src/java/test/unit"));

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    replay(store);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config), store);
    try {
      provider.getMessage("bad_key");
      fail("Should have failed");
    } catch (MissingMessageException e) {
      // Expected
    }
  }
}