/*
 * Copyright (c) 2001-2025, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ForwardResult.ForwardImpl;
import org.primeframework.mvc.action.result.RedirectResult.RedirectImpl;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.util.ReflectionUtils;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles invoking the result.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationWorkflow implements ResultInvocationWorkflow {
  private static final Logger logger = LoggerFactory.getLogger(DefaultResultInvocationWorkflow.class);

  private final ActionInvocationStore actionInvocationStore;

  private final MVCConfiguration configuration;

  private final Map<String, ActionResultDefinition> defaultResultMappings;

  private final ResultFactory factory;

  private final ResourceLocator resourceLocator;

  private final ResultStore resultStore;

  @Inject
  public DefaultResultInvocationWorkflow(ActionInvocationStore actionInvocationStore, MVCConfiguration configuration,
                                         Map<String, ActionResultDefinition> defaultResults,
                                         ResultStore resultStore, ResourceLocator resourceLocator,
                                         ResultFactory factory) {
    this.actionInvocationStore = actionInvocationStore;
    this.configuration = configuration;
    this.defaultResultMappings = defaultResults;
    this.resultStore = resultStore;
    this.resourceLocator = resourceLocator;
    this.factory = factory;
  }

  /**
   * Performs the action invocation using this process.
   * <p>
   * <h3>Action-less request</h3>
   * <p>
   * <ul> <li>Lookup an action-less result invocation</li> <li>If it doesn't exist, continue down the chain</li> <li>If
   * it does exist, call the ResultRegistry to find the Result</li> <li>Invoke the Result</li> </ul>
   * <p>
   * <h3>Action request</h3>
   * <p>
   * <ul> <li>Lookup an result invocation using the action invocation, action URI and result code from the action</li>
   * <li>If it doesn't exist, error out</li> <li>If it does exist, call the ResultRegistry to find the Result</li>
   * <li>Invoke the Result</li> </ul>
   *
   * @param chain The chain.
   * @throws IOException If the chain throws an IOException.
   */
  @SuppressWarnings("unchecked")
  public void perform(WorkflowChain chain) throws IOException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    if (actionInvocation.executeResult) {
      Annotation annotation = null;
      String resultCode = "success";
      if (actionInvocation.action != null) {
        resultCode = resultStore.get();
        annotation = actionInvocation.configuration.resultConfigurations.get(resultCode);
      }

      if (annotation == null) {
        var defaultMapping = defaultResultMappings.get(resultCode);
        if (defaultMapping != null) {
          annotation = defaultMapping.getAnnotation(resultCode);
        }

        if (annotation == null) {
          if (actionInvocation.action != null) {
            // Note that if an action exists, a configuration exists
            annotation = actionInvocation.configuration.resultConfigurations.get("*");
            if (annotation != null) {
              annotation = proxyAnnotation(annotation, resultCode);
            }
          }

          // We don't always have an action invocation.
          if (annotation == null) {
            annotation = new ForwardImpl("", resultCode);
          }
        }
      }

      // Call pre-render methods registered for this result
      if (actionInvocation.action != null && actionInvocation.configuration.preRenderMethods != null) {
        List<Method> preRenderMethods = actionInvocation.configuration.preRenderMethods.get(annotation.annotationType());
        if (preRenderMethods != null) {
          ReflectionUtils.invokeAll(actionInvocation.action, preRenderMethods);
        }
      }

      long start = System.currentTimeMillis();
      Result result = factory.build(annotation.annotationType());
      boolean handled = result.execute(annotation);

      if (logger.isDebugEnabled()) {
        logger.debug("Result execute took [{}]", (System.currentTimeMillis() - start));
      }

      if (!handled) {
        handleContinueOrRedirect(chain);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void handleContinueOrRedirect(WorkflowChain chain) throws IOException {
    String uri = resourceLocator.locateIndex(configuration.templateDirectory());
    if (uri != null) {
      Annotation annotation = new RedirectImpl(uri, "success", true, false);
      Result redirectResult = factory.build(annotation.annotationType());
      redirectResult.execute(annotation);
      return;
    }

    // The action either didn't have a result and the Forward template was missing, or there was no action and Forward template for the URI.
    // In either case, this should be a 404 so we will let the Prime MVC workflow at the top level handle that
    chain.continueWorkflow();
  }

  private Annotation proxyAnnotation(Annotation annotation, String resultCode) {
    try {
      Class<?> annotationType = annotation.annotationType();
      return (Annotation) Proxy.newProxyInstance(
          annotationType.getClassLoader(),
          new Class[]{annotationType},
          new AnnotationInvocationHandler(annotation, resultCode));
    } catch (Exception e) {
      throw new PrimeException("Unable to proxy the default result code [*]. This is unexpected.", e);
    }
  }

  public static class AnnotationInvocationHandler implements InvocationHandler {
    private final Annotation annotation;

    private final String resultCode;

    public AnnotationInvocationHandler(Annotation annotation, String resultCode) {
      this.annotation = annotation;
      this.resultCode = resultCode;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // The purpose of this proxy handler is to be able to modify the return value for "code" which is the result code.
      if (method.getName().equals("code")) {
        return resultCode;
      }

      return method.invoke(annotation, args);
    }
  }
}
