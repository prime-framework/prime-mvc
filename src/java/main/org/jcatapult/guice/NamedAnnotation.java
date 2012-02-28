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
package org.jcatapult.guice;

import java.lang.annotation.Annotation;

import com.google.inject.name.Named;

/**
 * <p>
 * This class implements Named annotation.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class NamedAnnotation implements Named {
    private final String value;

    public NamedAnnotation(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return 127 * "value".hashCode() ^ value.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Named)) {
            return false;
        }

        Named other = (Named) o;
        return value.equals(other.value());
    }

    public String toString() {
        return "@" + Named.class.getName() + "(value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType() {
        return Named.class;
    }
}