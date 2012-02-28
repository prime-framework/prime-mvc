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
package org.primeframework.mvc.result.form;

import java.lang.reflect.Method;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.result.form.annotation.FormPrepareMethod;

import com.google.inject.Inject;

/**
 * <p>
 * This is the default implementation of the FormPreparer interface.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultFormPreparer implements FormPreparer {
    private final ActionInvocationStore actionInvocationStore;

    @Inject
    public DefaultFormPreparer(ActionInvocationStore actionInvocationStore) {
        this.actionInvocationStore = actionInvocationStore;
    }

    /**
     * {@inheritDoc}
     */
    public void prepare() {
        // Get the action object
        ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
        Object action = actionInvocation.action();
        if (action == null) {
            return;
        }

        Class<?> actionClass = action.getClass();
        Method[] methods = actionClass.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(FormPrepareMethod.class) != null) {
                try {
                    method.invoke(action);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to call FormPrepareMethod method [" + method + "]", e);
                }
            }
        }
    }
}