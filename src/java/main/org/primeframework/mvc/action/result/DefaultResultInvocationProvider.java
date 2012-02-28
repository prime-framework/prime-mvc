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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import net.java.lang.reflect.ReflectionException;
import static net.java.lang.reflect.ReflectionTools.*;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.result.RedirectResult.RedirectImpl;
import org.primeframework.mvc.action.result.annotation.ResultAnnotation;
import org.primeframework.mvc.action.result.annotation.ResultContainerAnnotation;

/**
 * <p>
 * This class is the default implementation of the result provider.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultResultInvocationProvider implements ResultInvocationProvider {
    private final ForwardResult forwardResult;

    @Inject
    public DefaultResultInvocationProvider(ForwardResult forwardResult) {
        this.forwardResult = forwardResult;
    }

    /**
     * <p>
     * Delegates to the {@link ForwardResult#defaultResult(ActionInvocation, String)} method.
     * </p>
     *
     * @param   invocation The current action invocation.
     * @return  The result invocation that is a forward or redirect, depending on the situation.
     *          Or null if there isn't a forwardable resource in the web application for the given URI.
     */
    public ResultInvocation lookup(final ActionInvocation invocation) {
        Annotation annotation = forwardResult.defaultResult(invocation, null);
        if (annotation == null) {
            // Determine if there is an index page that we can redirect to for this URI. This index page would result in
            // a forward, therefore we'll ask the forward result for it
            String redirectURI = forwardResult.redirectURI(invocation);
            if (redirectURI != null) {
                annotation = new RedirectImpl(redirectURI, null, false, false);
            } else {
                return null;
            }
        }

        return new DefaultResultInvocation(annotation, invocation.actionURI(), null);
    }

    /**
     * <p>
     * Checks for results using this search order:
     * </p>
     *
     * <ol>
     * <li>Action annotations that are {@link ResultAnnotation}s, have a code method whose return
     *  value matches the result code</li>
     * <li>Delegates to the {@link ForwardResult#defaultResult(ActionInvocation, String)} method. </li>
     * </ol>
     *
     * @param   invocation The action invocation used to look for annotations.
     * @param   resultCode The result code from the action invocation.
     * @return  The result invocation from the annotation or a forward based on any pages that
     *          were found.
     */
    public ResultInvocation lookup(ActionInvocation invocation, String resultCode) {
        String uri = invocation.actionURI();
        Object action = invocation.action();
        List<Annotation> annotations = getAllAnnotations(action.getClass());
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(ResultAnnotation.class)) {
                if (matchesCode(resultCode, annotation)) {
                    return new DefaultResultInvocation(annotation, uri, resultCode);
                }
            } else if (annotation.annotationType().isAnnotationPresent(ResultContainerAnnotation.class)) {
                // There are multiple annotations inside the value
                try {
                    Annotation[] results = (Annotation[]) invokeMethod("value", annotation);
                    for (Annotation result : results) {
                        if (matchesCode(resultCode, result)) {
                            return new DefaultResultInvocation(result, uri, resultCode);
                        }
                    }
                } catch (ReflectionException e) {
                    throw new RuntimeException("Custom result annotation containers must have a method " +
                        "named [value] that is an array of result annotations.");
                }
            }

        }

        Annotation annotation = forwardResult.defaultResult(invocation, resultCode);
        if (annotation == null) {
            throw new RuntimeException("Unable to locate result for URI [" + invocation.uri() +
                "] and result code [" + resultCode + "]");
        }

        return new DefaultResultInvocation(annotation, uri, resultCode);
    }

    /**
     * Finds all of the annotations for the class, including those on parent classes.
     *
     * @param   type The type to start from.
     * @return  The list of annotations.
     */
    private List<Annotation> getAllAnnotations(Class<?> type) {
        List<Annotation> annotations = new ArrayList<Annotation>();
        while (type != Object.class) {
            annotations.addAll(asList(type.getAnnotations()));
            type = type.getSuperclass();
        }

        return annotations;
    }

    private boolean matchesCode(String resultCode, Annotation annotation) {
        try {
            String code = (String) invokeMethod("code", annotation);
            if (code.equals(resultCode)) {
                return true;
            }
        } catch (ReflectionException e) {
            throw new RuntimeException("Custom result annotations must have a method named " +
                "[code] that contains the result code they are associated with.");
        }

        return false;
    }
}
