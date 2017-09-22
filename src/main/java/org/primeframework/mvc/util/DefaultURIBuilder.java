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
package org.primeframework.mvc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.primeframework.mvc.action.annotation.URIModifier;

/**
 * This converts the class name into a URI using this method:
 * <p/>
 * <ul>
 *   <li>Find the first instance of <em>action</em></li>
 *   <li>Trim everything before that</li>
 *   <li>Replace periods (.) with forward slashes (/)</li>
 *   <li>Break on camel case and join back with dashes (-)</li>
 *   <li>Lower case the entire thing</li>
 * </ul>
 *
 * @author Brian Pontarelli
 */
public class DefaultURIBuilder implements URIBuilder {
  /**
   * {@inheritDoc}
   */
  public String build(Class<?> type) {
    // Determine the URI
    String rawURI = getURIFromActionClass(type);

    // Convert to underscores
    char[] ca = rawURI.toCharArray();
    StringBuilder build = new StringBuilder("" + ca[0]);
    boolean lower = true;
    boolean previousWasCharacter = false;
    for (int i = 1; i < ca.length; i++) {
      char c = ca[i];
      if (Character.isUpperCase(c) && previousWasCharacter && lower) {
        build.append("-");
        lower = false;
      } else if (Character.isUpperCase(c) && previousWasCharacter) {
        if (i + 1 < ca.length && Character.isLowerCase(ca[i + 1])) {
          build.append("-");
        }
      } else if (!Character.isUpperCase(c)) {
        lower = true;
      }

      build.append(c);

      previousWasCharacter = Character.isJavaIdentifierPart(c);
    }

    return build.toString().toLowerCase();
  }

  private String getURIFromActionClass(Class<?> type) {
    String fullName = type.getName();
    if (fullName.endsWith("Action")) {
      fullName = fullName.substring(0, fullName.length() - 6);
    }

    int index = fullName.indexOf("action");
    String lessPackage = fullName.substring(index + 6).replace('.', '/');

    List<String> parts = new ArrayList<>(Arrays.asList(lessPackage.substring(1).split("/")));
    int startIndex = parts.size() - 2; // last part is the Action name, subtract 2 instead of 1

    for (int partIndex = startIndex; partIndex >= 0; partIndex--) {
      int packageIndex = startIndex - partIndex; // starts at 0 and increments each iteration
      String packageName = getPackageName(fullName, packageIndex);
      Package pkg = ReflectionUtils.findPackageWithAnnotation(packageName, URIModifier.class);
      if (pkg != null) {
        URIModifier packageModifier = pkg.getAnnotation(URIModifier.class);
        parts.set(partIndex, packageModifier.value());
      }
    }

    return "/" + String.join("/", parts);
  }

  /**
   * Return the package name given a fully qualified class name. The index is from right to left.
   * <p>
   * For example:
   * <pre>
   *     getPackageName("org.primeframework.awesome.product.ExampleAction", 0) # "org.primeframework.awesome.product"
   *     getPackageName("org.primeframework.awesome.product.ExampleAction", 1) # "org.primeframework.awesome"
   *     getPackageName("org.primeframework.awesome.product.ExampleAction", 2) # "org.primeframework"
   *     getPackageName("org.primeframework.awesome.product.ExampleAction", 3) # "org"
   *     getPackageName("org.primeframework.awesome.product.ExampleAction", 4) # null
   *   </pre>
   *
   * @param fqClassName the fully qualified class name
   * @param index       the index starting at the right and moving left.
   * @return a string name or null
   */
  private String getPackageName(String fqClassName, int index) {
    int lastDot = fqClassName.lastIndexOf(".");
    while (index > 0) {
      lastDot = fqClassName.lastIndexOf('.', lastDot - 1);
      index--;
    }

    return lastDot == -1 ? null : fqClassName.substring(0, lastDot);
  }
}