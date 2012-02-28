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
package org.primeframework.mvc.action.result.freemarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.ext.beans.CollectionModel;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.primeframework.freemarker.FieldSupportBeansWrapper;
import org.primeframework.mvc.ObjectFactory;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ControlHashModel;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.result.control.Control;

import com.google.inject.Inject;

/**
 * <p>
 * This class is a FreeMarker model that provides access in the templates
 * to the request, session and contet attributes as well as values from the
 * action and the Control directives via the {@link org.primeframework.mvc.action.result.ControlHashModel} class.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class FreeMarkerMap implements TemplateHashModelEx {
    public static final String REQUEST_MODEL = "Request";
    public static final String REQUEST = "request";
    public static final String SESSION_MODEL = "Session";
    public static final String SESSION = "session";
    public static final String APPLICATION_MODEL = "Application";
    public static final String APPLICATION = "application";
    public static final String JCATAPULT_TAGS = "jc";
    public static final String JSP_TAGLIBS = "JspTaglibs";

    private static final Map<String, Class<? extends TemplateModel>> models = new HashMap<String, Class<? extends TemplateModel>>();
    private static final Map<String, Class<? extends Control>> controls = new HashMap<String, Class<? extends Control>>();
    private static TaglibFactory taglibFactory;
    private static ObjectFactory objectFactory;

    private static ServletContext context;
    private final Map<String, Object> objects = new HashMap<String, Object>();
    private final HttpServletRequest request;
    private final ExpressionEvaluator expressionEvaluator;
    private final ActionInvocationStore actionInvocationStore;

    /**
     * Initializes the ServletContext and the JSP taglib support for FreeMaker and also the TemplateModel
     * classes that are bound into the ObjectFactory.
     *
     * @param   context The context.
     * @param   objectFactory Used to get the template models.
     */
    @Inject
    public static void initialize(ServletContext context, ObjectFactory objectFactory) {
        FreeMarkerMap.taglibFactory = new TaglibFactory(context);
        FreeMarkerMap.context = context;

        List<Class<? extends TemplateModel>> types = objectFactory.getAllForType(TemplateModel.class);
        for (Class<? extends TemplateModel> type : types) {
            models.put(type.getSimpleName().toLowerCase(), type);
        }

        List<Class<? extends Control>> controlTypess = objectFactory.getAllForType(Control.class);
        for (Class<? extends Control> controlType : controlTypess) {
            controls.put(controlType.getSimpleName().toLowerCase(), controlType);
        }

        FreeMarkerMap.objectFactory = objectFactory;
    }

    public FreeMarkerMap(HttpServletRequest request, HttpServletResponse response,
            ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore, Map<String, Object> additionalValues) {
        objects.put(REQUEST_MODEL, new HttpRequestHashModel(request, response, FieldSupportBeansWrapper.INSTANCE));
        objects.put(REQUEST, request);
        HttpSession session = request.getSession(false);
        if (session != null) {
            objects.put(SESSION_MODEL, new HttpSessionHashModel(session, FieldSupportBeansWrapper.INSTANCE));
            objects.put(SESSION, session);
        }
        
        objects.put(APPLICATION_MODEL, new ServletContextHashModel(new GenericServlet() {
            public void service(ServletRequest servletRequest, ServletResponse servletResponse) {
            }

            @Override
            public ServletConfig getServletConfig() {
                return this;
            }

            @Override
            public ServletContext getServletContext() {
                return context;
            }


        }, FieldSupportBeansWrapper.INSTANCE));
        objects.put(APPLICATION, context);
        objects.put(JSP_TAGLIBS, taglibFactory);
        objects.put(JCATAPULT_TAGS, new ControlHashModel(objectFactory, controls));

        objects.putAll(additionalValues);

        // Add any additional FreeMarker models that are registered
        for (String key : models.keySet()) {
            if (!objects.containsKey(key)) {
                objects.put(key, objectFactory.create(models.get(key)));
            }
        }

        this.request = request;
        this.expressionEvaluator = expressionEvaluator;
        this.actionInvocationStore = actionInvocationStore;
    }

    public int size() {
        int size = objects.size() + count(request.getAttributeNames());

        HttpSession session = request.getSession(false);
        if (session != null) {
            size += count(session.getAttributeNames());
        }

        size += count(context.getAttributeNames());

        Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
        if (actionInvocations != null) {
            for (ActionInvocation actionInvocation : actionInvocations) {
                if (actionInvocation.action() != null) {
                    size += expressionEvaluator.getAllMembers(actionInvocation.action().getClass()).size();
                }
            }
        }

        return size;
    }

    public boolean isEmpty() {
        return size() > 0;
    }

    public TemplateModel get(String key) {
        // First check the action
        Object value = null;

        Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
        if (actionInvocations != null) {
            for (ActionInvocation actionInvocation : actionInvocations) {
                if (actionInvocation.action() != null) {
                    try {
                        value = expressionEvaluator.getValue(key, actionInvocation.action());
                        if (value != null) {
                            break;
                        }
                    } catch (ExpressionException e) {
                        // Smother because the value is probably somewhere else
                    }
                }
            }
        }

        // Next, check the objects
        if (value == null && objects.containsKey(key)) {
            value = objects.get(key);
        }

        // Next, check the request
        if (value == null) {
            value = request.getAttribute(key);
        }

        // Next, check the session
        if (value == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                value = session.getAttribute(key);
            }
        }

        // Next, check the context
        if (value == null) {
            value = context.getAttribute(key);
        }

        try {
            return FieldSupportBeansWrapper.INSTANCE.wrap(value);
        } catch (TemplateModelException e) {
            throw new RuntimeException(e);
        }
    }

    public TemplateCollectionModel keys() {
        Set<String> keys = append(objects.keySet(), iterable(request.getAttributeNames()));

        HttpSession session = request.getSession(false);
        if (session != null) {
            keys.addAll(append(iterable(session.getAttributeNames())));
        }

        keys.addAll(append(iterable(context.getAttributeNames())));

        Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
        if (actionInvocations != null) {
            for (ActionInvocation actionInvocation : actionInvocations) {
                if (actionInvocation.action() != null) {
                    keys.addAll(expressionEvaluator.getAllMembers(actionInvocation.action().getClass()));
                }
            }
        }

        keys.add(JSP_TAGLIBS);

        return new CollectionModel(keys, FieldSupportBeansWrapper.INSTANCE);
    }

    public TemplateCollectionModel values() {
        Collection<Object> values = new ArrayList<Object>(objects.values());
        Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
        if (actionInvocations != null) {
            for (ActionInvocation actionInvocation : actionInvocations) {
                if (actionInvocation.action() != null) {
                    values.addAll(expressionEvaluator.getAllMemberValues(actionInvocation.action()));
                }
            }
        }

        Enumeration en = request.getAttributeNames();
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            values.add(request.getAttribute(name));
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            en = session.getAttributeNames();
            while (en.hasMoreElements()) {
                String name = (String) en.nextElement();
                values.add(session.getAttribute(name));
            }
        }

        en = context.getAttributeNames();
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            values.add(context.getAttribute(name));
        }

        values.add(taglibFactory);

        return new CollectionModel(values, FieldSupportBeansWrapper.INSTANCE);
    }

    private int count(Enumeration enumeration) {
        int count = 0;
        while (enumeration.hasMoreElements()) {
            count++;
            enumeration.nextElement();
        }

        return count;
    }

    private <T> Set<T> append(Iterable<T>... iterables) {
        Set<T> set = new HashSet<T>();
        for (Iterable<T> iterable : iterables) {
            for (T key : iterable) {
                set.add(key);
            }
        }

        return set;
    }

    private Iterable<String> iterable(Enumeration enumeration) {
        List<String> list = new ArrayList<String>();
        while (enumeration.hasMoreElements()) {
            list.add((String) enumeration.nextElement());
        }

        return list;
    }
}