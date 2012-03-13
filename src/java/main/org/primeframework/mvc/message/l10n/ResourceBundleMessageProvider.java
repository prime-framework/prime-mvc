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

import java.util.Formatter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.Message;

import com.google.inject.Inject;

/**
 * This implements the MessageProvider using ResourceBundles inside the web context. It also adds the additional step of
 * looking for multiple bundles if the message isn't initially found. The search method is:
 * <p/>
 * <pre>
 * uri = /foo/bar
 * locale = en_US
 *
 * /WEB-INF/messages/foo/bar_en_US
 * /WEB-INF/messages/foo/bar_en
 * /WEB-INF/messages/foo/bar
 * /WEB-INF/messages/foo/package_en_US
 * /WEB-INF/messages/foo/package_en
 * /WEB-INF/messages/foo/package
 * /WEB-INF/messages/package_en_US
 * /WEB-INF/messages/package_en
 * /WEB-INF/messages/package
 * </pre>
 * <p/>
 * This stops when /WEB-INF/messages is hit.
 * <p/>
 * Once the message is found, it is formatted using the {@link Formatter} class. The values are passed in order.
 *
 * @author Brian Pontarelli
 */
public class ResourceBundleMessageProvider implements MessageProvider {
  private final Locale locale;
  private final ResourceBundle.Control control;
  private final ActionInvocationStore invocationStore;

  @Inject
  public ResourceBundleMessageProvider(Locale locale, Control control, ActionInvocationStore invocationStore) {
    this.locale = locale;
    this.control = control;
    this.invocationStore = invocationStore;
  }

  @Override
  public Message getMessage(String key, Object... values) throws MissingMessageException {
    ActionInvocation actionInvocation = invocationStore.getCurrent();
    String template = findMessage(actionInvocation.actionURI(), key);
    Formatter f = new Formatter();
    f.format(locale, template, values);
    final String message = f.out().toString();
    return new Message() {
      @Override
      public String toString() {
        return message;
      }
    };
  }

  @Override
  public FieldMessage getFieldMessage(final String field, String key, Object... values) throws MissingMessageException {
    ActionInvocation actionInvocation = invocationStore.getCurrent();
    String template = findMessage(actionInvocation.actionURI(), key);
    Formatter f = new Formatter();
    f.format(locale, template, values);
    final String message = f.out().toString();
    return new FieldMessage() {
      @Override
      public String getField() {
        return field;
      }

      @Override
      public String toString() {
        return message;
      }
    };
  }

  /**
   * Finds the message in a resource bundle using the search method described in the class comment.
   *
   * @param bundle The bundle to start the search with.
   * @param key    The key of the message.
   * @return The message or null if it doesn't exist.
   */
  protected String findMessage(String bundle, String key) {
    Queue<String> names = determineBundles(bundle);
    for (String name : names) {
      try {
        ResourceBundle rb = ResourceBundle.getBundle(name, locale, control);
        return rb.getString(key);
      } catch (MissingResourceException e) {
      }
    }

    throw new MissingMessageException("Message could not be found for bundle name [" + bundle + "] and key [" + key + "]");
  }

  protected Queue<String> determineBundles(String bundle) {
    Queue<String> names = new LinkedList<String>();
    names.offer(bundle);

    int index = bundle.lastIndexOf('/');
    while (index != -1) {
      bundle = bundle.substring(0, index);
      names.offer(bundle + "/package");
      index = bundle.lastIndexOf('/');
    }

    return names;
  }
}