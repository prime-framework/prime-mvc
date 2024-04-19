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

import java.net.URISyntaxException;
import java.nio.file.Paths;
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
public class URLPrinter {
  @Inject
  private ActionConfigurationProvider provider;

  /**
   * Dumps a list of URLs in the application based on discovered actions.
   *
   * @param dumpMethods should HTTP methods declared on the action be printed
   * @param includeTestClasses should action classes in the test path (build/classes/test) be included
   */
  public void dump(boolean dumpMethods, boolean includeTestClasses) {
    provider.getActionConfigurations()
            .stream()
            .filter(ac -> {
              try {
                return includeTestClasses || !isTestClass(ac);
              } catch (URISyntaxException e) {
                throw new RuntimeException(e);
              }
            })
            .sorted(Comparator.comparing(ac -> ac.uri))
            .forEach(actionConfig -> {
              var methods = actionConfig.executeMethods.entrySet().stream()
                                                       .map(this::formatMethod)
                                                       .collect(Collectors.joining(", "));
              String uri = actionConfig.uri;
              if (!actionConfig.pattern.isEmpty()) {
                uri = uri + "/" + actionConfig.pattern;
              }
              if (dumpMethods) {
                System.out.printf("%-60s%s\n",
                                  uri,
                                  methods);
              } else {
                System.out.println(uri);
              }
            });
  }

  private boolean isTestClass(ActionConfiguration actionConfiguration) throws URISyntaxException {
    return Paths.get(actionConfiguration.actionClass.getProtectionDomain().getCodeSource().getLocation().toURI()).endsWith(Paths.get("build/classes/test"));
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
