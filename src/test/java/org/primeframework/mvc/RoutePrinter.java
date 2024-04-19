/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;

/**
 * Prints out URLs/routes in the app based on Prime MVC actions. To use:
 * Then inject URLPrinter into your tests and call the dump method (see below)
 *
 * @author Brady Wied
 */
public class RoutePrinter {
  @Inject
  private ActionConfigurationProvider provider;

  /**
   * Dumps a list of URLs in the application based on discovered actions.
   *
   * @param showMethods       should HTTP methods declared on the action be printed
   * @param showActionClasses whether the classes that go with the URL should be printed
   * @param ignorePaths       paths to ignore when searching for actions
   */
  public void dump(boolean showMethods,
                   boolean showActionClasses,
                   String... ignorePaths) {
    var sortedList = provider.getActionConfigurations()
                             .stream()
                             .filter(ac -> !ignoreActionClass(ac, ignorePaths))
                             .sorted(Comparator.comparing(ac -> ac.uri))
                             .toList();
    // pad our output
    final int[] lengthMaximums = {0, 0};
    sortedList.forEach(actionConfig -> {
      lengthMaximums[0] = Math.max(lengthMaximums[0], actionConfig.uri.length() + 2);
      var actionClassName = actionConfig.actionClass.getName();
      lengthMaximums[1] = Math.max(lengthMaximums[1], actionClassName.length() + 2);
    });
    sortedList.forEach(actionConfig -> {
      var methods = actionConfig.executeMethods.entrySet().stream()
                                               .map(this::formatMethod)
                                               .collect(Collectors.joining(", "));
      String uri = actionConfig.uri;
      if (!actionConfig.pattern.isEmpty()) {
        uri = uri + "/" + actionConfig.pattern;
      }
      System.out.printf("%-" + lengthMaximums[0] + "s", uri);
      if (showActionClasses) {
        System.out.printf("%-" + lengthMaximums[1] + "s", actionConfig.actionClass.getName());
      }
      if (showMethods) {
        System.out.print(methods);
      }
      System.out.println();
    });
  }

  private boolean ignoreActionClass(ActionConfiguration actionConfiguration,
                                    String... pathsToIgnore) {
    var actionClassPath = actionConfiguration.actionClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    return Arrays.stream(pathsToIgnore)
                 .anyMatch(actionClassPath::contains);
  }

  private String formatMethod(Entry<HTTPMethod, ExecuteMethodConfiguration> entry) {
    var http = entry.getKey();
    var javaMethod = entry.getValue().method.getName();
    if (javaMethod.equals(http.name().toLowerCase())) {
      return http.name();
    }
    return String.format("%s->%s", http, javaMethod);
  }
}
