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
package org.primeframework.mvc.action.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freemarker.ext.beans.CollectionModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.primeframework.freemarker.FieldSupportBeansWrapper;
import org.primeframework.mvc.ObjectFactory;
import org.primeframework.mvc.result.control.Control;
import org.primeframework.mvc.result.control.FreeMarkerControlProxy;

/**
 * <p>
 * This class is a hash that stores the {@link Control} classes so that they
 * can be used from the FreeMarker templates.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ControlHashModel implements TemplateHashModelEx {
    private final ObjectFactory objectFactory;
    private final Map<String, Class<? extends Control>> controls;

    public ControlHashModel(ObjectFactory objectFactory, Map<String, Class<? extends Control>> controls) {
        this.objectFactory = objectFactory;
        this.controls = controls;
    }

    public TemplateCollectionModel keys() throws TemplateModelException {
        return new CollectionModel(keySet(), FieldSupportBeansWrapper.INSTANCE);
    }

    public int size() {
        return controls.size();
    }

    public boolean isEmpty() {
        return controls.isEmpty();
    }

    public TemplateModel get(String key) {
        Class<? extends Control> type = controls.get(key);
        if (type != null) {
            return new FreeMarkerControlProxy(objectFactory.create(type));
        }

        return null;
    }

    public TemplateCollectionModel values() {
        return new CollectionModel(valueCollection(), FieldSupportBeansWrapper.INSTANCE);
    }

    public Set<String> keySet() {
        return new HashSet<String>(controls.keySet());
    }

    public Collection<?> valueCollection() {
        List<TemplateModel> all = new ArrayList<TemplateModel>();
        for (String name : controls.keySet()) {
            all.add(new FreeMarkerControlProxy(objectFactory.create(controls.get(name))));
        }

        return all;
    }
}