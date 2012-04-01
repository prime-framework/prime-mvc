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

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.RedirectResult.RedirectImpl;
import org.primeframework.mvc.action.result.annotation.ResultAnnotation;
import org.primeframework.mvc.action.result.annotation.ResultContainerAnnotation;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * This class is the default implementation of the result provider.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationProvider implements ResultInvocationProvider {
  private final ActionInvocationStore actionInvocationStore;
  private final ForwardResult forwardResult;

  @Inject
  public DefaultResultInvocationProvider(ActionInvocationStore actionInvocationStore, ForwardResult forwardResult) {
    this.actionInvocationStore = actionInvocationStore;
    this.forwardResult = forwardResult;
  }

  /**
   * Delegates to the {@link ForwardResult#defaultResult(ActionInvocation, String)} method.
   *
   * @return The result invocation that is a forward or redirect, depending on the situation. Or null if there isn't a
   *         forwardable resource in the web application for the given URI.
   */
  public ResultInvocation lookup() {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
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
   * Checks for results using this search order:
   * <p/>
   * <ol>
   *   <li>Action annotations that are {@link ResultAnnotation}s, have a code method whose return value matches the
   * result code</li>
   *   <li>Delegates to the {@link ForwardResult#defaultResult(ActionInvocation, String)} method.</li>
   * </ol>
   *
   * @param resultCode The result code from the action invocation.
   * @return The result invocation from the annotation or a forward based on any pages that were found.
   */
  public ResultInvocation lookup(String resultCode) {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
    String uri = invocation.actionURI();
    Object action = invocation.action();
    List<Annotation> annotations = getAllAnnotations(action.getClass());
    for (Annotation annotation : annotations) {
      Class<? extends Annotation> type = annotation.annotationType();
      if (type.isAnnotationPresent(ResultAnnotation.class)) {
        if (matchesCode(resultCode, annotation)) {
          return new DefaultResultInvocation(annotation, uri, resultCode);
        }
      } else if (type.isAnnotationPresent(ResultContainerAnnotation.class)) {
        // There are multiple annotations inside the value
        try {
          Annotation[] results = (Annotation[]) annotation.getClass().getMethod("value").invoke(annotation);
          for (Annotation result : results) {
            if (matchesCode(resultCode, result)) {
              return new DefaultResultInvocation(result, uri, resultCode);
            }
          }
        } catch (Exception e) {
          throw new PrimeException("Custom result annotation containers must have a method " +
            "named [value] that is an array of result annotations.");
        }
      }
    }

    Annotation annotation = forwardResult.defaultResult(invocation, resultCode);
    if (annotation == null) {
      throw new PrimeException("Unable to locate result for URI [" + invocation.uri() + "] and result code [" + resultCode + "]");
    }

    return new DefaultResultInvocation(annotation, uri, resultCode);
  }

  /**
   * Finds all of the annotations for the class, including those on parent classes.
   *
   * @param type The type to start from.
   * @return The list of annotations.
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
      String code = (String) annotation.getClass().getMethod("code").invoke(annotation);
      if (code.equals(resultCode)) {
        return true;
      }
    } catch (Exception e) {
      throw new PrimeException("Custom result annotations must have a method named [code] that contains the result code they are associated with.");
    }

    return false;
  }
}
