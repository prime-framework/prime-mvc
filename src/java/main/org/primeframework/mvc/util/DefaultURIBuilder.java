/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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

/**
 * <p>
 * This converts the class name into a URI using this method:
 * </p>
 *
 * <ul>
 * <li>Find the first instance of <em>action</em></li>
 * <li>Trim everything before that</li>
 * <li>Replace periods (.) with forward slashes (/)</li>
 * <li>Break on camel case and join back with dashes (-)</li>
 * <li>Lower case the entire thing</li>
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
        String fullName = type.getName();
        int index = fullName.indexOf("action");
        String lessPackage = fullName.substring(index + 6).replace('.', '/');

        // Convert to underscores
        char[] ca = lessPackage.toCharArray();
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
}