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
package org.jcatapult.mvc.scope;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.google.inject.Inject;
import org.jcatapult.mvc.action.ActionInvocation;
import org.jcatapult.mvc.action.ActionInvocationStore;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;
import org.jcatapult.mvc.scope.annotation.ScopeAnnotation;
import org.jcatapult.servlet.WorkflowChain;

/**
 * <p>
 * This class implements the storage.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultScopeStorageWorkflow implements ScopeStorageWorkflow {
    private final ActionInvocationStore actionInvocationStore;
    private final ExpressionEvaluator expressionEvaluator;
    private final ScopeProvider scopeProvider;

    @Inject
    public DefaultScopeStorageWorkflow(ActionInvocationStore actionInvocationStore,
                                       ExpressionEvaluator expressionEvaluator,
                                       ScopeProvider scopeProvider) {
        this.actionInvocationStore = actionInvocationStore;
        this.expressionEvaluator = expressionEvaluator;
        this.scopeProvider = scopeProvider;
    }

    /**
     * Handles the incoming HTTP request for scope values.
     *
     * @param   chain The workflow chain.
     */
    public void perform(WorkflowChain chain) throws IOException, ServletException {
        ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
        Object action = actionInvocation.action();

        // Handle storing scoped members from the action
        if (action != null) {
            storeScopedMembers(action);
        }

        chain.continueWorkflow();
    }

    /**
     * Stores all of the values from the action from into the scopes.
     *
     * @param   action The action to get the values from.
     */
    @SuppressWarnings("unchecked")
    protected void storeScopedMembers(Object action) {
        Class<?> klass = action.getClass();
        while (klass != Object.class) {
            Field[] fields = klass.getDeclaredFields();
            for (Field field : fields) {
                Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    Class<? extends Annotation> type = annotation.annotationType();
                    String fieldName = field.getName();

                    if (type.isAnnotationPresent(ScopeAnnotation.class)) {
                        Scope scope = scopeProvider.lookup(type);
                        Object value = expressionEvaluator.getValue(fieldName, action);
                        scope.set(fieldName, value, annotation);
                    }
                }
            }

            klass = klass.getSuperclass();
        }
    }
}
