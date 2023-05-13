/*
 * Copyright (c) 2001-2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.util.ClassClasspathResolver;
import org.primeframework.mvc.util.URITools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loads the configuration by scanning the classpath for packages and action classes.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class DefaultActionConfigurationProvider implements ActionConfigurationProvider {
  private static final Logger logger = LoggerFactory.getLogger(DefaultActionConfigurationProvider.class);

  private final List<ActionConfiguration> actionConfigurations = new ArrayList<>();

  private final Node root = new Node();

  @Inject
  public DefaultActionConfigurationProvider(ActionConfigurationBuilder builder) {
    ClassClasspathResolver<?> resolver = new ClassClasspathResolver<>();
    Set<? extends Class<?>> actionClasses;
    try {
      actionClasses = resolver.findByLocators(new ClassClasspathResolver.AnnotatedWith(Action.class), true, null, "action");
    } catch (IOException e) {
      throw new PrimeException("Error discovering action classes", e);
    }

    for (Class<?> actionClass : actionClasses) {
      // Only accept classes loaded by the ClassLoader for Prime. This prevents classes loaded by parent loader from
      // being included as available Actions. One situation that this can occur: A jar with Actions (Prime) is in the classpath
      // of a Java program, and that program starts up an embedded web server that includes prime-mvc. When the embedded web server
      // initializes prime-mvc it will locate the actions in the jar outside the war file.
      if (!inClassLoaderOrParentClassLoader(Action.class.getClassLoader(), actionClass)) {
        continue;
      }

      ActionConfiguration actionConfiguration = builder.build(actionClass);
      actionConfigurations.add(actionConfiguration);
      String uri = actionConfiguration.uri;

      Node current = root;
      String[] uriParts = uri.substring(1).split("/");

      for (int i = 0; i < uriParts.length; i++) {
        if (i == uriParts.length - 1) {
          current = processPrefixParameters(actionConfiguration, current);
          current = current.actions.compute(uriParts[i], (k, v) -> new Node(actionConfiguration));
        } else {
          current = current.packages.computeIfAbsent(uriParts[i], k -> new Node());
        }
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Added action configuration for [{}] and the uri [{}]", actionClass, actionConfiguration.uri);
      }
    }
  }

  @Override
  public List<ActionConfiguration> getActionConfigurations() {
    return new ArrayList<>(actionConfigurations);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ActionInvocation lookup(String uri) {
    // Handle extensions
    String extension = URITools.determineExtension(uri);
    if (extension != null) {
      uri = uri.substring(0, uri.length() - extension.length() - 1);
    }

    // When ending in a slash, add 'index' to see if that helps us match an Index action.
    boolean addIndexURIPart = uri.endsWith("/");
    if (addIndexURIPart) {
      uri = uri + "index";
    }

    ActionInvocation invocation = new ActionInvocation(null, null, uri, extension, null);
    String[] uriParts = uri.substring(1).split("/");
    TraversalState state = new TraversalState();
    boolean result = traverse(root, uriParts, 0, state, addIndexURIPart);
    if (result) {
      invocation.actionURI = state.actionConfiguration.uri;
      invocation.configuration = state.actionConfiguration;
      invocation.uriParameters.putAll(state.uriParameters.entrySet()
                                                         .stream()
                                                         .filter(e -> e.getValue().size() > 0)
                                                         .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }

    return invocation;
  }

  /**
   * Determines if the action configuration can handle the given URI parts. MVCConfiguration objects provide additional
   * handling for URI parameters and other cases and this method uses the full incoming URI to determine if the
   * configuration can handle it.
   *
   * @param actionConfiguration The action configuration to check.
   * @param remainingURIParts   The remaining URI parts after the traversal in the lookup method.
   * @return True if this configuration can handle the URI, false if not.
   */
  protected boolean canHandle(ActionConfiguration actionConfiguration, String[] remainingURIParts,
                              TraversalState state) {
    Map<String, List<String>> uriParameters = new HashMap<>();
    for (int i = 0; i < remainingURIParts.length; i++) {
      String uriPart = remainingURIParts[i];

      // If there are no more pattern parts, bail
      if (i >= actionConfiguration.patternParts.length) {
        break;
      }

      if (actionConfiguration.patternParts[i].startsWith("{*")) {
        // Bad pattern
        if (!actionConfiguration.patternParts[i].endsWith("}")) {
          throw new PrimeException("Action annotation in class [" + actionConfiguration.actionClass +
              "] contains an invalid URI parameter pattern [" + actionConfiguration.pattern + "]. A curly " +
              "bracket is unclosed. If you want to include a curly bracket that is not " +
              "a URI parameter capture, you need to escape it like \\{");
        }

        // Can't have wildcard capture in the middle
        if (i != actionConfiguration.patternParts.length - 1) {
          throw new PrimeException("Action annotation in class [" + actionConfiguration.actionClass +
              "] contains an invalid URI parameter pattern [" + actionConfiguration.pattern + "]. You cannot " +
              "have a wildcard capture (i.e. {*foo}) in the middle of the pattern. It must " +
              "be on the end of the pattern.");
        }

        // Store the wildcard matched URI parameter
        String name = actionConfiguration.patternParts[i].substring(2, actionConfiguration.patternParts[i].length() - 1);
        String[] parts = Arrays.copyOfRange(remainingURIParts, i, remainingURIParts.length);
        List<String> params = uriParameters.computeIfAbsent(name, k -> new ArrayList<>());
        for (String part : parts) {
          params.add(URITools.decodeURIPathSegment(part));
        }

        break;
      } else if (actionConfiguration.patternParts[i].startsWith("{")) {
        if (!actionConfiguration.patternParts[i].endsWith("}")) {
          throw new PrimeException("Action annotation in class [" + actionConfiguration.actionClass +
              "] contains an invalid URI parameter pattern [" + actionConfiguration.pattern + "]. A curly " +
              "bracket is unclosed. If you want to include a curly bracket that is not " +
              "a URI parameter capture, you need to escape it like \\{");
        }

        // Store the singular matched URI parameter
        String name = actionConfiguration.patternParts[i].substring(1, actionConfiguration.patternParts[i].length() - 1);
        uriParameters.computeIfAbsent(name, k -> new ArrayList<>()).add(URITools.decodeURIPathSegment(uriPart));
      } else {
        // Ensure that the URI matches the literal from the annotation (i.e. {foo}/bar/{baz})
        String patternPart = normalize(actionConfiguration.patternParts[i]);
        if (!uriPart.equals(patternPart)) {
          return false;
        }
      }
    }

    for (String key : uriParameters.keySet()) {
      state.uriParameters.computeIfAbsent(key, k -> new ArrayList<>())
                         .addAll(uriParameters.get(key));
    }

    return true;
  }

  /**
   * Replaces \{ with { and \} with }.
   *
   * @param pattern The pattern to normalize.
   * @return The normalized pattern.
   */
  protected String normalize(String pattern) {
    return pattern.replace("\\{", "{").replace("\\}", "}");
  }

  /**
   * Traverses the Tree and attempts to locate an Action node.
   *
   * @param node              The current node.
   * @param uriParts          The URI parts (all of them)
   * @param currentIndex      The current node index (in the URI parts)
   * @param state             The state of the traversal.
   * @param addedIndexURIPart True if 'index' was added as an additional segment to the URI parts
   * @return True if we found an action node, false if not.
   */
  protected boolean traverse(Node node, String[] uriParts, int currentIndex, TraversalState state, boolean addedIndexURIPart) {
    // Exit condition
    if (currentIndex == uriParts.length) {
      return false;
    }

    // First, recurse on package names
    String uriPart = uriParts[currentIndex];
    if (node.packages.containsKey(uriPart)) {
      if (traverse(node.packages.get(uriPart), uriParts, currentIndex + 1, state, addedIndexURIPart)) {
        return true;
      }
    }

    // Second, recurse on parameters
    if (node.parameters.size() > 0) {
      for (String parameterName : node.parameters.keySet()) {
        state.uriParameters.computeIfAbsent(parameterName, k -> new ArrayList<>()).add(uriPart);
        if (traverse(node.parameters.get(parameterName), uriParts, currentIndex + 1, state, addedIndexURIPart)) {
          return true;
        }
        state.uriParameters.get(parameterName).remove(uriPart);
      }
    }

    // Last, check for actions
    if (node.actions.containsKey(uriPart)) {
      // If addedIndexURIPart is true then remove the last URI part for this part of the resolution
      int endIndex = addedIndexURIPart ? uriParts.length - 1 : uriParts.length;
      String[] remainingURIParts = endIndex < (currentIndex + 1) ? new String[]{} : Arrays.copyOfRange(uriParts, currentIndex + 1, endIndex);
      Node actionNode = node.actions.get(uriPart);
      if (canHandle(actionNode.actionConfiguration, remainingURIParts, state)) {
        state.actionConfiguration = actionNode.actionConfiguration;
        return true;
      }
    }

    return false;
  }

  /**
   * Return true if the {@code actionClass} is loaded by {@code classLoader} or one of it's descendant
   * {@link ClassLoader}
   *
   * @param classLoader the ClassLoader
   * @param actionClass the Class to test
   * @return true if actionClass was loaded by classLoader or one of its children
   */
  private boolean inClassLoaderOrParentClassLoader(ClassLoader classLoader, Class<?> actionClass) {
    ClassLoader actionClassClassLoader = actionClass.getClassLoader();
    while (actionClassClassLoader != null) {
      if (classLoader.equals(actionClassClassLoader)) {
        return true;
      }
      actionClassClassLoader = actionClassClassLoader.getParent();
    }
    return false;
  }

  /**
   * Process prefix parameters.
   * <p>
   * Process an action that has defined a prefix parameter. Example : /api/application/{id}/oauth-configuration
   * <p>
   * The {id} parameter will be set on the package node to identify the parameter as coming before the action name on
   * the URI when the URI is processed later by {@link #lookup(String)}.
   *
   * @param actionConfiguration The action configuration.
   * @param current             The current node.
   * @return The new current node.
   */
  private Node processPrefixParameters(ActionConfiguration actionConfiguration, Node current) {
    // Process prefix parameters adding them to the current package node
    if (!actionConfiguration.annotation.prefixParameters().equals("")) {
      String[] prefixParameters = actionConfiguration.annotation.prefixParameters().split("/");
      for (String prefix : prefixParameters) {
        String prefixName = prefix.substring(1, prefix.length() - 1);
        current = current.parameters.computeIfAbsent(prefixName, k -> new Node(prefixName));
      }
    }

    return current;
  }

  private static class Node {
    public ActionConfiguration actionConfiguration;

    public Map<String, Node> actions = new TreeMap<>();

    public Map<String, Node> packages = new TreeMap<>();

    public String parameterName;

    public Map<String, Node> parameters = new TreeMap<>();

    public Node() {
    }

    public Node(String parameterName) {
      this.parameterName = parameterName;
    }

    public Node(ActionConfiguration actionConfiguration) {
      this.actionConfiguration = actionConfiguration;
    }
  }

  private static class TraversalState {
    public ActionConfiguration actionConfiguration;

    public Map<String, List<String>> uriParameters = new TreeMap<>();
  }
}