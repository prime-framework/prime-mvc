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
package org.primeframework.mvc.action.config;

import org.primeframework.mvc.action.annotation.Action;

/**
 * <p>
 * This class is the default implementation of the action configuration.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultActionConfiguration implements ActionConfiguration {
    private final Class<?> actionClass;
    private final String uri;
    private final Action annotation;

    public DefaultActionConfiguration(Class<?> actionClass, String uri) {
        this.actionClass = actionClass;
        this.uri = uri;
        this.annotation = actionClass.getAnnotation(Action.class);
    }

    public Class<?> actionClass() {
        return actionClass;
    }

    public String uri() {
        return uri;
    }

    public boolean canHandle(String uri) {
        // Check if the URIs are equal
        if (this.uri.equals(uri)) {
            return true;
        }

        // Verify that the full URI starts with this URI
        if (!uri.startsWith(this.uri)) {
            return false;
        }

        // Finally, use the pattern from the action annotation value
        String pattern = annotation.value();
        if (pattern.equals("")) {
            return false;
        }

        String[] uriParts = uri.substring(this.uri.length() + 1).split("/");
        String[] patternParts = pattern.split("/");
        for (int i = 0; i < uriParts.length; i++) {
            String uriPart = uriParts[i];

            // If there are no more pattern parts, bail
            if (i >= patternParts.length) {
                break;
            }

            if (patternParts[i].startsWith("{*")) {
                // Bad pattern
                if (!patternParts[i].endsWith("}")) {
                    throw new IllegalArgumentException("Action annotation in class [" + actionClass +
                        "] contains an invalid URI parameter pattern [" + pattern + "]. A curly " +
                        "bracket is unclosed. If you want to include a curly brakcet that is not " +
                        "a URI parameter capture, you need to escape it like \\{");
                }

                // Can't have wildcard capture in the middle
                if (i != patternParts.length - 1) {
                    throw new IllegalArgumentException("Action annotation in class [" + actionClass +
                        "] contains an invalid URI parameter pattern [" + pattern + "]. You cannot " +
                        "have a wildcard capture (i.e. {*foo}) in the middle of the pattern. It must " +
                        "be on the end of the pattern.");
                }

                break;
            } else if (patternParts[i].startsWith("{")) {
                if (!patternParts[i].endsWith("}")) {
                    throw new IllegalArgumentException("Action annotation in class [" + actionClass +
                        "] contains an invalid URI parameter pattern [" + pattern + "]. A curly " +
                        "bracket is unclosed. If you want to include a curly brakcet that is not " +
                        "a URI parameter capture, you need to escape it like \\{");
                }
            } else {
                String patternPart = normalize(patternParts[i]);
                if (!uriPart.equals(patternPart)) {
                    return false;
                }
            }
        }

        return true;
    }

    public String uriParameterPattern() {
        return annotation.value();
    }

    /**
     * Replaces \{ with { and \} with }.
     *
     * @param   pattern The pattern to normalize.
     * @return  The normalized pattern.
     */
    private String normalize(String pattern) {
        return pattern.replace("\\{", "{").replace("\\}", "}");
    }
}