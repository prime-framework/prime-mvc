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
package org.jcatapult.mvc.message.scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class stores field messages using a multi-map methodology.
 * This is NOT thread safe by any means. If you want to use this
 * implementation for anything outside of a request, you must
 * synchronize correctly.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class FieldMessages implements Map<String, List<String>> {
    private Map<String, List<String>> messages = new LinkedHashMap<String, List<String>>();

    /**
     * Adds the given field message to the list of messages for that field.
     *
     * @param   field The name of the field.
     * @param   message The message to append to the list of messages for that field.
     */
    public void addMessage(String field, String message) {
        List<String> list = messages.get(field);
        if (list == null) {
            list = new ArrayList<String>();
            messages.put(field, list);
        }

        list.add(message);
    }

    /**
     * @return  The live field messages.
     */
    public Map<String, List<String>> getFieldMessages() {
        return messages;
    }

    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public boolean containsKey(Object key) {
        return messages.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return messages.containsValue(value);
    }

    public List<String> get(Object key) {
        return messages.get(key);
    }

    public List<String> put(String key, List<String> value) {
        throw new UnsupportedOperationException("Put is not allowed on FieldMessages. Use addMessage instead.");
    }

    public List<String> remove(Object key) {
        return messages.remove(key);
    }

    public void putAll(Map<? extends String, ? extends List<String>> m) {
        throw new UnsupportedOperationException("Put is not allowed on FieldMessages. Use addMessage instead.");
    }

    public void clear() {
        messages.clear();
    }

    public Set<String> keySet() {
        return messages.keySet();
    }

    public Collection<List<String>> values() {
        return messages.values();
    }

    public Set<Entry<String, List<String>>> entrySet() {
        return messages.entrySet();
    }

    public boolean equals(Object o) {
        return messages.equals(o);
    }
}
