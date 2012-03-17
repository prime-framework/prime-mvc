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
package org.primeframework.mvc.message.l10n;

import java.io.File;
import java.util.Locale;

import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.container.ServletContainerResolver;
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
    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.l10nReloadSeconds()).andReturn(1).times(2);
    replay(config);

    MockServletContext context = new MockServletContext(new File("src/test/java"));

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/NonExistent", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/badPackage/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    replay(store);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals(provider.getMessage("key").toString(), "American English Message");
    assertEquals(provider.getMessage("key").toString(), "Package Message");
    assertEquals(provider.getMessage("key").toString(), "Super Package Message");

    provider = new ResourceBundleMessageProvider(Locale.GERMAN, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals(provider.getMessage("key").toString(), "Default Message");
  }

  @Test
  public void format() {
    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.l10nReloadSeconds()).andReturn(1).times(2);
    replay(config);

    MockServletContext context = new MockServletContext(new File("src/test/java"));

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/NonExistent", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/badPackage/Test", null, null));
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/l10n/Test", null, null));
    replay(store);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(Locale.US, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals(provider.getMessage("format_key", "b", "a", "c").toString(), "American English Message b a c");
    assertEquals(provider.getMessage("format_key", "b", "a", "c").toString(), "Package Message b a c");
    assertEquals(provider.getMessage("format_key", "b", "a", "c").toString(), "Super Package Message b a c");

    provider = new ResourceBundleMessageProvider(Locale.GERMAN, new WebControl(new ServletContainerResolver(context), config), store);
    assertEquals(provider.getMessage("format_key", "b", "a", "c").toString(), "Default Message b a c");
  }

  @Test
  public void missing() {
    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.l10nReloadSeconds()).andReturn(1);
    replay(config);

    MockServletContext context = new MockServletContext(new File("src/test/java"));

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