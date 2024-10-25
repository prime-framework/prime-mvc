/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.locale.LocaleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the MessageProvider using ResourceBundles inside the web context. It also adds the additional step of
 * looking for multiple bundles if the message isn't initially found. The search method is:
 * <p>
 * <pre>
 * uri = /foo/bar
 * locale = en_US
 *
 * ${configuration.resourceDirectory}/messages/foo/bar_en_US
 * ${configuration.resourceDirectory}/messages/foo/bar_en
 * ${configuration.resourceDirectory}/messages/foo/bar
 * ${configuration.resourceDirectory}/messages/foo/package_en_US
 * ${configuration.resourceDirectory}/messages/foo/package_en
 * ${configuration.resourceDirectory}/messages/foo/package
 * ${configuration.resourceDirectory}/messages/package_en_US
 * ${configuration.resourceDirectory}/messages/package_en
 * ${configuration.resourceDirectory}/messages/package
 * </pre>
 * <p>
 * This stops when ${configuration.resourceDirectory}/messages is hit.
 * <p>
 * Once the message is found, it is formatted using the {@link Formatter} class. The values are passed in order.
 *
 * @author Brian Pontarelli
 */
public class ResourceBundleMessageProvider implements MessageProvider {
  private final static Logger logger = LoggerFactory.getLogger(ResourceBundleMessageProvider.class);

  private final ResourceBundle.Control control;

  private final ActionInvocationStore invocationStore;

  private final LocaleProvider localeProvider;

  @Inject
  public ResourceBundleMessageProvider(LocaleProvider localeProvider, Control control,
                                       ActionInvocationStore invocationStore) {
    this.localeProvider = localeProvider;
    this.control = control;
    this.invocationStore = invocationStore;
  }

  @Override
  public String getMessage(String key, Object... values) throws MissingMessageException {
    String message = getOptionalMessage(key, values);
    if (message == null) {
      ActionInvocation actionInvocation = invocationStore.getCurrent();
      throw new MissingMessageException("Message could not be found for the URI [" + actionInvocation.actionURI + "] and key [" + key + "]");
    }

    return message;
  }

  @Override
  public String getOptionalMessage(String key, Object... values) {
    ActionInvocation actionInvocation = invocationStore.getCurrent();
    String template = findMessage(actionInvocation, key);
    // See if a default message exists for this key
    if (template == null) {
      template = findDefaultMessage(actionInvocation, key);
    }

    if (template == null) {
      if (!"[ValidationException]".equals(key)) {
        logger.debug("Message could not be found for the URI [{}] and key [{}]", actionInvocation.actionURI, key);
      }

      return null;
    }

    Formatter f = new Formatter();
    f.format(localeProvider.get(), template, values);
    return f.out().toString();
  }

  protected Queue<String> determineBundles(String bundle) {
    Queue<String> names = new LinkedList<>();
    names.offer(bundle);

    int index = bundle.lastIndexOf('/');
    while (index != -1) {
      bundle = bundle.substring(0, index);
      names.offer(bundle + "/package");
      index = bundle.lastIndexOf('/');
    }

    return names;
  }

  /**
   * Finds the message in a resource bundle using the search method described in the class comment.
   * If the action was annotated with a {@link org.primeframework.mvc.action.annotation.MessageResources} annotation
   * and the message was not found with the request's action, that action will be searched as well.
   *
   * @param actionInvocation The action invocation.
   * @param key              The key of the message.
   * @return The message or null if it doesn't exist.
   */
  protected String findMessage(ActionInvocation actionInvocation, String key) {
    String message = findMessage(actionInvocation.actionURI, key);
    if (message != null) {
      return message;
    }

    ActionConfiguration config = actionInvocation.configuration;
    if (config.fallbackMessageResourcesPath != null) {
      return findMessage(config.fallbackMessageResourcesPath, key);
    }

    return null;
  }

  /**
   * For "bracketed" style messages such as [blank]foo.bar, fall back to [blank] as a default.
   *
   * @param actionInvocation The action invocation.
   * @param key              The key of the message.
   * @return The message or null if it doesn't exist.
   */
  private String findDefaultMessage(ActionInvocation actionInvocation, String key) {
    if (key.indexOf('[') == 0) {
      int index = key.indexOf(']', 1);
      if (index != -1) {
        return findMessage(actionInvocation, key.substring(0, index + 1));
      }
    }

    return null;
  }

  /**
   * Finds the message in a resource bundle using the search method described in the class comment.
   *
   * @param uri The action URI to use for searching.
   * @param key The key of the message.
   * @return The message or null if it doesn't exist.
   */
  private String findMessage(String uri, String key) {
    Queue<String> names = determineBundles(uri);
    for (String name : names) {
      try {
        ResourceBundle rb = ResourceBundle.getBundle(name, localeProvider.get(), control);
        return rb.getString(key);
      } catch (MissingResourceException ignore) {
        // Ignore and check the next bundle
      }
    }

    return null;
  }
}
