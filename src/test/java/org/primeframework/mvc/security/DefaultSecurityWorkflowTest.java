/*
 * Copyright (c) 2015-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import com.google.inject.Inject;
import org.example.action.PostAction;
import org.example.action.SecureAction;
import org.example.action.SecureNoRolesAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.DefaultActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.http.HTTPContext;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.security.guice.SecuritySchemeFactory;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.fail;

/**
 * @author Brian Pontarelli
 */
public class DefaultSecurityWorkflowTest extends PrimeBaseTest {
  @Inject public DefaultActionConfigurationBuilder actionConfigurationBuilder;

  @Inject public HTTPContext context;

  @Test
  public void performAuthenticationNotRequired() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(PostAction.class);
    ExecuteMethodConfiguration methodConfiguration = new ExecuteMethodConfiguration(HTTPMethod.GET, null, null);
    ActionInvocation actionInvocation = new ActionInvocation(new PostAction(), methodConfiguration, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext securityContext = new MockUserLoginSecurityContext(request, response);
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(PrimeBaseTest.configuration, securityContext, request, csrfProvider));
    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredNoRolesRequired() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(SecureNoRolesAction.class);
    ExecuteMethodConfiguration methodConfiguration = new ExecuteMethodConfiguration(HTTPMethod.GET, null, null);
    ActionInvocation actionInvocation = new ActionInvocation(new SecureNoRolesAction(), methodConfiguration, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext.roles.add("bad");
    MockUserLoginSecurityContext securityContext = new MockUserLoginSecurityContext(request, response);
    securityContext.login("user");

    // Copy the cookies back from the response to the request because the request cookies are used below
    request.addCookies(response.getCookies());

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    // Setup CSRF Origin and Referer
    request.setHost("example.com");
    request.addHeader("Origin", "http://example.com:10000");

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(PrimeBaseTest.configuration, securityContext, request, csrfProvider));
    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredNotLoggedInGET() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(SecureAction.class);
    ExecuteMethodConfiguration methodConfiguration = new ExecuteMethodConfiguration(HTTPMethod.GET, null, null);
    ActionInvocation actionInvocation = new ActionInvocation(new SecureAction(), methodConfiguration, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    request.setMethod(HTTPMethod.GET);
    request.setPath("/secure");
    request.addParameter("test", "value");
    request.addParameter("test2", "value2");

    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext securityContext = new MockUserLoginSecurityContext(request, response);
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(PrimeBaseTest.configuration, securityContext, request, csrfProvider));
    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    replay(workflowChain);

    try {
      workflow.perform(workflowChain);
      fail("Should have failed");
    } catch (UnauthenticatedException e) {
      // Expected
    }

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredNotLoggedInPOST() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(SecureAction.class);
    ExecuteMethodConfiguration methodConfiguration = new ExecuteMethodConfiguration(HTTPMethod.POST, null, null);
    ActionInvocation actionInvocation = new ActionInvocation(new SecureAction(), methodConfiguration, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    request.setMethod(HTTPMethod.POST);
    request.setPath("/secure");
    request.addParameter("test", "value");
    request.addParameter("test2", "value2");

    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext securityContext = new MockUserLoginSecurityContext(request, response);
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(PrimeBaseTest.configuration, securityContext, request, csrfProvider));
    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    replay(workflowChain);

    try {
      workflow.perform(workflowChain);
      fail("Should have failed");
    } catch (UnauthenticatedException e) {
      // Expected
    }

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredSuccess() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(SecureAction.class);
    ExecuteMethodConfiguration methodConfiguration = new ExecuteMethodConfiguration(HTTPMethod.GET, null, null);
    ActionInvocation actionInvocation = new ActionInvocation(new SecureAction(), methodConfiguration, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext.roles.add("admin");
    MockUserLoginSecurityContext securityContext = new MockUserLoginSecurityContext(request, response);
    securityContext.login("user");

    // Copy the cookies back from the response to the request because the request cookies are used below
    request.addCookies(response.getCookies());

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(PrimeBaseTest.configuration, securityContext, request, csrfProvider));
    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    // Setup CSRF Origin and Referer
    request.setHost("example.com");
    request.addHeader("Origin", "http://example.com:10000");

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredWrongRoles() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(SecureAction.class);
    ExecuteMethodConfiguration methodConfiguration = new ExecuteMethodConfiguration(HTTPMethod.GET, null, null);
    ActionInvocation actionInvocation = new ActionInvocation(new SecureAction(), methodConfiguration, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext.roles.add("bad");
    MockUserLoginSecurityContext securityContext = new MockUserLoginSecurityContext(request, response);
    securityContext.login("user");

    // Copy the cookies back from the response to the request because the request cookies are used below
    request.addCookies(response.getCookies());

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(PrimeBaseTest.configuration, securityContext, request, csrfProvider));
    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    replay(workflowChain);

    try {
      workflow.perform(workflowChain);
      fail("Should have thrown");
    } catch (UnauthorizedException e) {
      // Expected
    }

    verify(workflowChain);
  }

  @Test
  public void performNotConfigured() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(PostAction.class);
    ExecuteMethodConfiguration methodConfiguration = new ExecuteMethodConfiguration(HTTPMethod.GET, null, null);
    ActionInvocation actionInvocation = new ActionInvocation(new PostAction(), methodConfiguration, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(PrimeBaseTest.configuration, null, request, csrfProvider));

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  public static class TestSecuritySchemeFactory extends SecuritySchemeFactory {
    private final MVCConfiguration configuration;

    private final CSRFProvider csrfProvider;

    private final HTTPRequest request;

    private final MockUserLoginSecurityContext securityContext;

    public TestSecuritySchemeFactory(MVCConfiguration configuration, MockUserLoginSecurityContext securityContext,
                                     HTTPRequest request, CSRFProvider csrfProvider) {
      super(PrimeBaseTest.injector);
      this.configuration = configuration;
      this.securityContext = securityContext;
      this.csrfProvider = csrfProvider;
      this.request = request;
    }

    @Override
    public SecurityScheme build(String scheme) {
      DefaultUserLoginConstraintValidator constraintsValidator = new DefaultUserLoginConstraintValidator();
      constraintsValidator.setUserLoginSecurityContext(securityContext);

      UserLoginSecurityScheme s = new UserLoginSecurityScheme(configuration, constraintsValidator, csrfProvider, request, request.getMethod());
      s.setUserLoginSecurityContext(securityContext);
      return s;
    }
  }
}
