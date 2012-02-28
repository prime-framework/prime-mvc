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
package org.primeframework.mvc.message;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.example.action.user.Edit;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.l10n.MessageProvider;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.message.scope.MessageType;
import org.primeframework.mvc.message.scope.Scope;
import org.primeframework.mvc.message.scope.ScopeProvider;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;

/**
 * <p> This tests the default message store. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageStoreTest {
  @Test
  public void testConversionError() {
    Map<String, String> attributes = new HashMap<String, String>();
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("bundle", "foo.bar.conversionError", attributes, values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addFieldMessage(MessageType.ERROR, "foo.bar", "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.REQUEST)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addConversionError("foo.bar", "bundle", attributes, (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddFieldMessage() {
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("bundle", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addFieldMessage(MessageType.PLAIN, "foo.bar", "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addFieldMessage(MessageScope.ACTION_SESSION, "foo.bar", "bundle", "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddFieldMessageAction() {
    Edit action = new Edit();
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar", null, null));
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("/foo/bar", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addFieldMessage(MessageType.PLAIN, "foo.bar", "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addFieldMessage(MessageScope.ACTION_SESSION, "foo.bar", "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddFieldError() {
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("bundle", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addFieldMessage(MessageType.ERROR, "foo.bar", "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addFieldError(MessageScope.ACTION_SESSION, "foo.bar", "bundle", "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddFieldErrorAction() {
    Edit action = new Edit();
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar", null, null));
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("/foo/bar", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addFieldMessage(MessageType.ERROR, "foo.bar", "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addFieldError(MessageScope.ACTION_SESSION, "foo.bar", "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddActionMessage() {
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("bundle", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addActionMessage(MessageType.PLAIN, "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addActionMessage(MessageScope.ACTION_SESSION, "bundle", "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddActionMessageAction() {
    Edit action = new Edit();
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar", null, null));
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("/foo/bar", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addActionMessage(MessageType.PLAIN, "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addActionMessage(MessageScope.ACTION_SESSION, "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddActionError() {
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("bundle", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addActionMessage(MessageType.ERROR, "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addActionError(MessageScope.ACTION_SESSION, "bundle", "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }

  @Test
  public void testAddActionErrorAction() {
    Edit action = new Edit();
    String[] values = array("value1", "value2");

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(action, "/foo/bar", null, null));
    EasyMock.replay(ais);

    MessageProvider mp = EasyMock.createStrictMock(MessageProvider.class);
    EasyMock.expect(mp.getMessage("/foo/bar", "key", values)).andReturn("message");
    EasyMock.replay(mp);

    Scope scope = EasyMock.createStrictMock(Scope.class);
    scope.addActionMessage(MessageType.ERROR, "message");
    EasyMock.replay(scope);

    ScopeProvider sp = EasyMock.createStrictMock(ScopeProvider.class);
    EasyMock.expect(sp.lookup(MessageScope.ACTION_SESSION)).andReturn(scope);
    EasyMock.replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(ais, mp, sp);
    store.addActionError(MessageScope.ACTION_SESSION, "key", (Object[]) values);

    EasyMock.verify(ais, mp, scope, sp);
  }
}