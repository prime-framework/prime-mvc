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
package org.jcatapult.mvc.parameter.el;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * This class is a small helper class that is used to store the read and write methods of a bean
 * property as well as a flag that determines if it is indexed.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class PropertyInfo {
    private String name;
    private final Map<String, Method> methods = new HashMap<String, Method>();
    private Class<?> klass;
    private Class<?> type;
    private boolean indexed;
    private Type genericType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getKlass() {
        return klass;
    }

    public void setKlass(Class<?> klass) {
        this.klass = klass;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public Type getGenericType() {
        return genericType;
    }

    public void setGenericType(Type genericType) {
        this.genericType = genericType;
    }

    public Map<String, Method> getMethods() {
        return methods;
    }

    public String toString() {
        return "Property named [" + name + "] in class [" + klass + "]";
    }
}