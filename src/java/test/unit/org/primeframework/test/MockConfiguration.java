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
package org.primeframework.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import org.primeframework.config.PrimeMVCConfiguration;

/**
 * <p>
 * This is a mock configuration object that delegates to another
 * PrimeMVCConfiguration instance, but also allows specific properties to
 * be mocked out. This can be used in a unit tests via a little glue like this:
 * </p>
 *
 * <pre>
 *
 * protected PrimeMVCConfiguration config;
 *
 * {@code @Inject}
 * public void setConfiguration(PrimeMVCConfiguration config) {
 *   this.config = config;
 * }
 *
 * {@code @Test}
 * protected void mock() {
 *   PrimeMVCConfiguration mock = new MockConfiguration(config);
 *   mock.addParameter("foo", "bar");
 *   ...
 * }
 * </pre>
 *
 * @author Brian Pontarelli
 */
public class MockConfiguration implements PrimeMVCConfiguration {
    private final Map<String, Object> parameters = new HashMap<String, Object>();
    private final PrimeMVCConfiguration configuration;

    public MockConfiguration(PrimeMVCConfiguration configuration) {
        this.configuration = configuration;
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public boolean isEmpty() {
        return configuration.isEmpty() && parameters.isEmpty();
    }

    public boolean containsKey(String key) {
        return configuration.containsKey(key) || parameters.containsKey(key);
    }

    public void clearProperty(String key) {
        // not implemented
    }

    public Object getProperty(String key) {
        if (parameters.containsKey(key)) {
            return parameters.get(key);
        }

        return configuration.getProperty(key);
    }

    public Iterator<String> getKeys(String prefix) {
        Set<String> keys = new HashSet<String>();
        Set<String> pks = parameters.keySet();
        for (String pk : pks) {
            if (pk.startsWith(prefix)) {
                keys.add(pk);
            }
        }

        Iterator<String> iter = configuration.getKeys();
        while (iter.hasNext()) {
            String s = iter.next();
            if (s.startsWith(prefix)) {
                keys.add(s);
            }
        }

        return keys.iterator();
    }

    public Iterator<String> getKeys() {
        Set<String> keys = new HashSet<String>(parameters.keySet());
        Iterator<String> iter = configuration.getKeys();
        while (iter.hasNext()) {
            String s = iter.next();
            keys.add(s);
        }
        return keys.iterator();
    }

    public Properties getProperties(String key) {
        return null;
    }

    public boolean getBoolean(String key) {
        if (parameters.containsKey(key)) {
            return (Boolean) parameters.get(key);
        }

        return configuration.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (parameters.containsKey(key)) {
            return (Boolean) parameters.get(key);
        }

        return configuration.getBoolean(key, defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        if (parameters.containsKey(key)) {
            return (Boolean) parameters.get(key);
        }

        return configuration.getBoolean(key, defaultValue);
    }

    public byte getByte(String key) {
        if (parameters.containsKey(key)) {
            return (Byte) parameters.get(key);
        }

        return configuration.getByte(key);
    }

    public byte getByte(String key, byte defaultValue) {
        if (parameters.containsKey(key)) {
            return (Byte) parameters.get(key);
        }

        return configuration.getByte(key, defaultValue);
    }

    public Byte getByte(String key, Byte defaultValue) {
        if (parameters.containsKey(key)) {
            return (Byte) parameters.get(key);
        }

        return configuration.getByte(key, defaultValue);
    }

    public double getDouble(String key) {
        if (parameters.containsKey(key)) {
            return (Double) parameters.get(key);
        }

        return configuration.getDouble(key);
    }

    public double getDouble(String key, double defaultValue) {
        if (parameters.containsKey(key)) {
            return (Double) parameters.get(key);
        }

        return configuration.getDouble(key, defaultValue);
    }

    public Double getDouble(String key, Double defaultValue) {
        if (parameters.containsKey(key)) {
            return (Double) parameters.get(key);
        }

        return configuration.getDouble(key, defaultValue);
    }

    public float getFloat(String key) {
        if (parameters.containsKey(key)) {
            return (Float) parameters.get(key);
        }

        return configuration.getFloat(key);
    }

    public float getFloat(String key, float defaultValue) {
        if (parameters.containsKey(key)) {
            return (Float) parameters.get(key);
        }

        return configuration.getFloat(key, defaultValue);
    }

    public Float getFloat(String key, Float defaultValue) {
        if (parameters.containsKey(key)) {
            return (Float) parameters.get(key);
        }

        return configuration.getFloat(key, defaultValue);
    }

    public int getInt(String key) {
        if (parameters.containsKey(key)) {
            return (Integer) parameters.get(key);
        }

        return configuration.getInt(key);
    }

    public int getInt(String key, int defaultValue) {
        if (parameters.containsKey(key)) {
            return (Integer) parameters.get(key);
        }

        return configuration.getInt(key, defaultValue);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        if (parameters.containsKey(key)) {
            return (Integer) parameters.get(key);
        }

        return configuration.getInteger(key, defaultValue);
    }

    public long getLong(String key) {
        if (parameters.containsKey(key)) {
            return (Long) parameters.get(key);
        }

        return configuration.getLong(key);
    }

    public long getLong(String key, long defaultValue) {
        if (parameters.containsKey(key)) {
            return (Long) parameters.get(key);
        }

        return configuration.getLong(key, defaultValue);
    }

    public Long getLong(String key, Long defaultValue) {
        if (parameters.containsKey(key)) {
            return (Long) parameters.get(key);
        }

        return configuration.getLong(key, defaultValue);
    }

    public short getShort(String key) {
        if (parameters.containsKey(key)) {
            return (Short) parameters.get(key);
        }

        return configuration.getShort(key);
    }

    public short getShort(String key, short defaultValue) {
        if (parameters.containsKey(key)) {
            return (Short) parameters.get(key);
        }

        return configuration.getShort(key, defaultValue);
    }

    public Short getShort(String key, Short defaultValue) {
        if (parameters.containsKey(key)) {
            return (Short) parameters.get(key);
        }

        return configuration.getShort(key, defaultValue);
    }

    public BigDecimal getBigDecimal(String key) {
        if (parameters.containsKey(key)) {
            return (BigDecimal) parameters.get(key);
        }

        return configuration.getBigDecimal(key);
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        if (parameters.containsKey(key)) {
            return (BigDecimal) parameters.get(key);
        }

        return configuration.getBigDecimal(key, defaultValue);
    }

    public BigInteger getBigInteger(String key) {
        if (parameters.containsKey(key)) {
            return (BigInteger) parameters.get(key);
        }

        return configuration.getBigInteger(key);
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        if (parameters.containsKey(key)) {
            return (BigInteger) parameters.get(key);
        }

        return configuration.getBigInteger(key, defaultValue);
    }

    public String getString(String key) {
        if (parameters.containsKey(key)) {
            return (String) parameters.get(key);
        }

        return configuration.getString(key);
    }

    public String getString(String key, String defaultValue) {
        if (parameters.containsKey(key)) {
            return (String) parameters.get(key);
        }

        return configuration.getString(key, defaultValue);
    }

    public String[] getStringArray(String key) {
        if (parameters.containsKey(key)) {
            return (String[]) parameters.get(key);
        }

        return configuration.getStringArray(key);
    }

    public List<String> getList(String key) {
        if (parameters.containsKey(key)) {
            return (List<String>) parameters.get(key);
        }

        return configuration.getList(key);
    }

    public List<String> getList(String key, List<String> defaultValue) {
        if (parameters.containsKey(key)) {
            return (List<String>) parameters.get(key);
        }

        return configuration.getList(key, defaultValue);
    }
}