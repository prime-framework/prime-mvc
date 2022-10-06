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
 */
package org.primeframework.mvc.message.l10n;

import java.nio.file.Path;
import java.util.Locale;

import io.fusionauth.http.server.HTTPContext;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.container.ServletContainerResolver;
import org.primeframework.mvc.locale.LocaleProvider;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * This class tests the resource bundle message provider.
 *
 * @author Brian Pontarelli
 */
public class ResourceBundleMessageProviderTest extends PrimeBaseTest {
  @Test
  public void defaultMessages() {
    HTTPContext context = new HTTPContext(Path.of("src/test/java"));
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/", null, null)).times(4);
    replay(store);

    LocaleProvider localeProvider = createStrictMock(LocaleProvider.class);
    expect(localeProvider.get()).andReturn(Locale.US).times(15);
    replay(localeProvider);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(localeProvider, new WebControl(new ServletContainerResolver(context), configuration), store);
    assertEquals(provider.getMessage("[blank]foo.bar"), "Required (foo.bar)");
    assertEquals(provider.getMessage("[blank]baz"), "Required (default)");

    // Really missing
    try {
      provider.getMessage("[not_found]bar");
      fail("Should have failed");
    } catch (MissingMessageException e) {
      // Expected
    }

    verify(store);
  }

  @Test
  public void format() {
    HTTPContext context = new HTTPContext(Path.of("src/test/java"));
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/Test", null, null));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/NonExistent", null, null));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/badPackage/Test", null, null));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/Test", null, null));
    replay(store);

    LocaleProvider localeProvider = createStrictMock(LocaleProvider.class);
    expect(localeProvider.get()).andReturn(Locale.US).times(9);
    expect(localeProvider.get()).andReturn(Locale.GERMAN).times(3);
    replay(localeProvider);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(localeProvider, new WebControl(new ServletContainerResolver(context), configuration), store);
    assertEquals(provider.getMessage("format_key", "b", "a", "c"), "American English Message b a c");
    assertEquals(provider.getMessage("format_key", "b", "a", "c"), "Package Message b a c");
    assertEquals(provider.getMessage("format_key", "b", "a", "c"), "Super Package Message b a c");

    provider = new ResourceBundleMessageProvider(localeProvider, new WebControl(new ServletContainerResolver(context), configuration), store);
    assertEquals(provider.getMessage("format_key", "b", "a", "c"), "Default Message b a c");

    verify(store);
  }

  @Test
  public void missing() {
    HTTPContext context = new HTTPContext(Path.of("src/test/java"));
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/Test", null, null)).times(2);
    replay(store);

    LocaleProvider localeProvider = createStrictMock(LocaleProvider.class);
    expect(localeProvider.get()).andReturn(Locale.US).times(3);
    replay(localeProvider);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(localeProvider, new WebControl(new ServletContainerResolver(context), configuration), store);
    try {
      provider.getMessage("bad_key");
      fail("Should have failed");
    } catch (MissingMessageException e) {
      // Expected
    }

    verify(store);
  }

  @Test
  public void search() {
    HTTPContext context = new HTTPContext(Path.of("src/test/java"));
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/Test", null, null));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/NonExistent", null, null));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/badPackage/Test", null, null));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/l10n/Test", null, null));
    replay(store);

    LocaleProvider localeProvider = createStrictMock(LocaleProvider.class);
    expect(localeProvider.get()).andReturn(Locale.US).times(9);
    expect(localeProvider.get()).andReturn(Locale.GERMAN).times(3);
    replay(localeProvider);

    ResourceBundleMessageProvider provider = new ResourceBundleMessageProvider(localeProvider, new WebControl(new ServletContainerResolver(context), configuration), store);
    assertEquals(provider.getMessage("key"), "American English Message");
    assertEquals(provider.getMessage("key"), "Package Message");
    assertEquals(provider.getMessage("key"), "Super Package Message");

    provider = new ResourceBundleMessageProvider(localeProvider, new WebControl(new ServletContainerResolver(context), configuration), store);
    assertEquals(provider.getMessage("key"), "Default Message");

    verify(store);
  }
}