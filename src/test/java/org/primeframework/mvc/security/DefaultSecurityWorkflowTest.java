/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.example.action.Post;
import org.example.action.Secure;
import org.example.action.SecureNoRoles;
import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.DefaultActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.security.guice.SecuritySchemeFactory;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Brian Pontarelli
 */
public class DefaultSecurityWorkflowTest extends PrimeBaseTest {
  @Inject public DefaultActionConfigurationBuilder actionConfigurationBuilder;

  @Test
  public void performAuthenticationNotRequired() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(Post.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Post(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    TestUserLoginSecurityContext securityContext = new TestUserLoginSecurityContext(request, emptySet());
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(securityContext));

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredNoRolesRequired() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(SecureNoRoles.class);
    ActionInvocation actionInvocation = new ActionInvocation(new SecureNoRoles(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    TestUserLoginSecurityContext securityContext = new TestUserLoginSecurityContext(request, new HashSet<>(singletonList("bad")));
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(securityContext));
    request.getSession().setAttribute(BaseHttpSessionUserLoginSecurityContext.USER_SESSION_KEY, "user"); // Log in the user

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredNotLoggedInGET() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(Secure.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Secure(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    request.setMethod(Method.GET);
    request.setUri("/secure");
    request.getParameters().put("test", singletonList("value"));
    request.getParameters().put("test2", singletonList("value2"));

    TestUserLoginSecurityContext securityContext = new TestUserLoginSecurityContext(request, emptySet());
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(securityContext));

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
    ActionConfiguration configuration = actionConfigurationBuilder.build(Secure.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Secure(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    request.setMethod(Method.POST);
    request.setUri("/secure");
    request.getParameters().put("test", singletonList("value"));
    request.getParameters().put("test2", singletonList("value2"));

    TestUserLoginSecurityContext securityContext = new TestUserLoginSecurityContext(request, emptySet());
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(securityContext));

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
    ActionConfiguration configuration = actionConfigurationBuilder.build(Secure.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Secure(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    TestUserLoginSecurityContext securityContext = new TestUserLoginSecurityContext(request, new HashSet<>(singletonList("admin")));
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(securityContext));
    request.getSession().setAttribute(BaseHttpSessionUserLoginSecurityContext.USER_SESSION_KEY, "user"); // Log in the user

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  @Test
  public void performAuthenticationRequiredWrongRoles() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(Secure.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Secure(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    TestUserLoginSecurityContext securityContext = new TestUserLoginSecurityContext(request, new HashSet<>(singletonList("bad")));
    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(securityContext));
    request.getSession().setAttribute(BaseHttpSessionUserLoginSecurityContext.USER_SESSION_KEY, "user"); // Log in the user

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
    ActionConfiguration configuration = actionConfigurationBuilder.build(Post.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Post(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, new TestSecuritySchemeFactory(null));

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  public static class TestSecuritySchemeFactory extends SecuritySchemeFactory {
    private final TestUserLoginSecurityContext securityContext;

    public TestSecuritySchemeFactory(TestUserLoginSecurityContext securityContext) {
      super(PrimeBaseTest.injector);
      this.securityContext = securityContext;
    }

    @Override
    public SecurityScheme build(String scheme) {
      UserLoginSecurityScheme s = new UserLoginSecurityScheme();
      s.setUserLoginSecurityContext(securityContext);
      return s;
    }
  }

  public static class TestUserLoginSecurityContext extends BaseHttpSessionUserLoginSecurityContext {
    public final Set<String> roles = new HashSet<>();

    public TestUserLoginSecurityContext(HttpServletRequest request, Set<String> roles) {
      super(request);
      this.roles.addAll(roles);
    }

    @Override
    public Set<String> getCurrentUsersRoles() {
      return roles;
    }
  }
}
