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
package org.jcatapult.mvc.validation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import static java.util.Arrays.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static net.java.lang.ObjectTools.*;
import org.jcatapult.mvc.action.ActionInvocation;
import org.jcatapult.mvc.action.ActionInvocationStore;
import org.jcatapult.mvc.action.DefaultActionInvocation;
import org.jcatapult.mvc.message.MessageStore;
import org.jcatapult.mvc.message.scope.MessageScope;
import org.jcatapult.mvc.message.scope.MessageType;
import org.jcatapult.mvc.parameter.DefaultParameterWorkflow;
import org.jcatapult.mvc.parameter.InternalParameters;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;
import org.jcatapult.mvc.parameter.el.TypeTools;
import org.jcatapult.mvc.validation.annotation.Valid;
import org.jcatapult.mvc.validation.annotation.ValidCollection;
import org.jcatapult.mvc.validation.annotation.ValidMap;
import org.jcatapult.mvc.validation.annotation.ValidateMethod;
import org.jcatapult.mvc.validation.annotation.ValidatorAnnotation;
import org.jcatapult.servlet.WorkflowChain;

import com.google.inject.Inject;

/**
 * <p>
 * This workflow performs all the validation on the current action.
 * </p>
 *
 * <p>
 * Validation can be turned off by setting a form field by the name 'jcatapultExecuteValidation'
 * to false. This is best accomplished by setting a hidden input field as follows:
 * </p>
 *
 * <pre>
 * &lt;input type="hidden" name="jcatapultExecuteValidation" value="false"/>
 * </pre>
 *
 * @author Brian Pontarelli
 */
public class DefaultValidationWorkflow implements ValidationWorkflow {
    private final ActionInvocationStore actionInvocationStore;
    private final ExpressionEvaluator expressionEvaluator;
    private final ValidatorProvider validatorProvider;
    private final MessageStore messageStore;
    private final HttpServletRequest request;

    @Inject
    public DefaultValidationWorkflow(HttpServletRequest request, ActionInvocationStore actionInvocationStore,
                                     ExpressionEvaluator expressionEvaluator, ValidatorProvider validatorProvider,
                                     MessageStore messageStore) {
        this.request = request;
        this.actionInvocationStore = actionInvocationStore;
        this.expressionEvaluator = expressionEvaluator;
        this.validatorProvider = validatorProvider;
        this.messageStore = messageStore;
    }

    /**
     * Performs the validation on the action using annotations and validate methods. If validation
     * fails, this swaps out the ActionInvocation with a dummied version that always returns the
     * <code>input</code> result code and then proceeds down the chain.
     *
     * @param chain The chain.
     * @throws IOException      If the chain throws.
     * @throws ServletException If the chain throws.
     */
    public void perform(WorkflowChain chain) throws IOException, ServletException {
        if (request.getMethod().equals("POST") ||containsSubmitButton(request)) {
            ActionInvocation invocation = actionInvocationStore.getCurrent();
            Object action = invocation.action();
            boolean executeValidation = InternalParameters.is(request, InternalParameters.JCATAPULT_EXECUTE_VALIDATION);
            if (action != null && executeValidation) {
                validate(action);
                if (messageStore.contains(MessageType.ERROR)) {
                    actionInvocationStore.setCurrent(new DefaultActionInvocation(action, invocation.actionURI(),
                            invocation.extension(), null, invocation.configuration(), true, false, "input"));
                }
            }
        }

        chain.continueWorkflow();
    }

    /**
     * Determines if the request contains a submit button, which means a form was subnitted using a
     * GET.
     *
     * @param   request The request.
     * @return  True if the form contains a submit button, false otherwise.
     */
    protected boolean containsSubmitButton(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        for (String key :params.keySet()){
            if (key.startsWith(DefaultParameterWorkflow.ACTION_PREFIX)) {
                return true;
            }
        }
        
        return false;
    }

    protected void validate(Object action) {
        Class<?> type = action.getClass();
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ValidateMethod.class)) {
                try {
                    method.invoke(action);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to invoke @ValidateMethod named [" +
                            method.getName() + "] on class [" + action.getClass() + "]", e);
                }
            }
        }

        handleAnnotations(action.getClass(), action, "");
    }

    /**
     * Handles looping over all of the fields on the given Class and looking for validation annotations.
     *
     * @param type   The Class to validate.
     * @param object (Optional) The instance of the Class.
     * @param path   The current validation path to the object.
     */
    protected void handleAnnotations(Class<?> type, Object object, String path) {
        List<Field> fields = allFields(type);
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().isAnnotationPresent(ValidatorAnnotation.class)) {
                    Object value = (object != null) ? expressionEvaluator.getValue(field.getName(), object) : null;
                    String newPath = path(path, field);
                    validate(annotation, object, value, newPath);
                } else if (annotation.annotationType() == Valid.class) {
                    handleObject(object, path, field);
                } else if (annotation.annotationType() == ValidMap.class) {
                    handleMap(object, path, field, annotation);
                } else if (annotation.annotationType() == ValidCollection.class) {
                    handleCollection(object, path, field, annotation);
                }
            }
        }
    }

    /**
     * Handles plain objects.
     *
     * @param container The object to get the value from for the given field.
     * @param path      The path up to the object, not including the field.
     * @param field     The field to validate.
     */
    protected void handleObject(Object container, String path, Field field) {
        // First, validate that this field isn't a collection, array or map
        String newPath = path(path, field);
        Class<?> type = field.getType();
        if (isCollection(type) || type.isArray()) {
            throw new RuntimeException("Invalid use of @Valid on the field [" + newPath +
                    "]. This annotation should only be used on fields that are NOT Collections, arrays or Maps.");
        }

        // Next, grab the value and handle it
        Object value = (container != null) ? expressionEvaluator.getValue(field.getName(), container) : null;
        if (value != null) {
            handleAnnotations(value.getClass(), value, newPath);
        } else {
            handleAnnotations(type, null, newPath);
        }
    }

    /**
     * Handles Maps.
     *
     * @param container  The object to get the value from for the given field.
     * @param path       The path up to the object, not including the field.
     * @param field      The field to validate.
     * @param annotation The ValidMap annotation.
     */
    protected void handleMap(Object container, String path, Field field, Annotation annotation) {
        // First verify that it is a collection
        String newPath = path(path, field);
        if (!Map.class.isAssignableFrom(field.getType())) {
            throw new RuntimeException("Invalid use of @ValidMap on the field [" + newPath +
                    "]. This annotation can only be used on fields that are Maps.");
        }

        // Next, grab the value and handle it as a Map.
        Object value = (container != null) ? expressionEvaluator.getValue(field.getName(), container) : null;
        if (value != null) {
            Map map = (Map) value;
            for (Object key : map.keySet()) {
                Object mapObj = map.get(key);
                String keyedPath = newPath + "['" + key.toString() + "']";
                if (mapObj != null) {
                    handleAnnotations(mapObj.getClass(), mapObj, keyedPath);
                } else {
                    Type fieldType = field.getGenericType();
                    Class<?> componentType = TypeTools.componentFinalType(fieldType, keyedPath);
                    handleAnnotations(componentType, null, keyedPath);
                }
            }
        } else {
            ValidMap validMap = (ValidMap) annotation;
            String[] keys = validMap.keys();
            Type fieldType = field.getGenericType();
            Class<?> componentType = TypeTools.componentFinalType(fieldType, path + "." + field.getName());
            for (String key : keys) {
                String keyedPath = newPath + "['" + key + "']";
                handleAnnotations(componentType, null, keyedPath);
            }
        }
    }

    /**
     * Handles Collections and Arrays.
     *
     * @param container  The object to get the value from for the given field.
     * @param path       The path up to the object, not including the field.
     * @param field      The field to validate.
     * @param annotation The ValidCollection annotation.
     */
    protected void handleCollection(Object container, String path, Field field, Annotation annotation) {
        // First verify that it is a collection
        String newPath = path(path, field);
        if (!Collection.class.isAssignableFrom(field.getType()) && !field.getType().isArray()) {
            throw new RuntimeException("Invalid use of @ValidCollection on the field [" + newPath +
                    "]. This annotation can only be used on fields that are Collections or arrays.");
        }

        // Next grab the value and handle it as a collection
        Object value = (container != null) ? expressionEvaluator.getValue(field.getName(), container) : null;
        if (value != null) {
            Collection collection = (Collection) value;
            int i = 0;
            for (Object o : collection) {
                String indexedPath = newPath + "[" + i++ + "]";
                if (o != null) {
                    handleAnnotations(o.getClass(), o, indexedPath);
                } else {
                    Type fieldType = field.getGenericType();
                    Class<?> componentType = TypeTools.componentFinalType(fieldType, indexedPath);
                    handleAnnotations(componentType, null, indexedPath);
                }
            }
        } else {
            ValidCollection validCollection = (ValidCollection) annotation;
            int[] indexes = validCollection.indexes();
            Type fieldType = field.getGenericType();
            Class<?> componentType = TypeTools.componentFinalType(fieldType, newPath);
            for (int index : indexes) {
                String indexedPath = newPath + "[" + index + "]";
                handleAnnotations(componentType, null, indexedPath);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void validate(Annotation annotation, Object container, Object value, String path) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Validator validator = validatorProvider.lookup(annotationType);
        if (!validator.validate(annotation, container, value)) {
            String key;
            try {
                key = (String) annotation.annotationType().getMethod("key").invoke(annotation);
                if (key.equals("")) {
                    key = path + "." + annotationType.getSimpleName().toLowerCase();
                }
            } catch (Exception e) {
                throw new RuntimeException("Invalid validator annotation [" + annotation.annotationType() +
                        "] it is missing the required key() method.");
            }

            messageStore.addFieldError(MessageScope.REQUEST, path, key, value);
        }
    }

    private List<Field> allFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        while (type != Object.class) {
            fields.addAll(asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }

        return fields;
    }

    private String path(String path, Field field) {
        if (path.equals("")) {
            return field.getName();
        }

        return path + "." + field.getName();
    }
}