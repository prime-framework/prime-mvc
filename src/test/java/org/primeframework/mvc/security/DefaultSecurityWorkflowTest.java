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

import javax.servlet.http.HttpServletRequest;
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
import org.primeframework.mvc.config.MVCConfiguration;
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

  @Inject public MVCConfiguration mvcConfiguration;

  @Test
  public void performAuthenticationNotRequired() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(Post.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Post(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, request, response, mvcConfiguration);
    workflow.setSecurityContext(new TestSecurityContext(request, emptySet()));

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

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, request, response, mvcConfiguration);
    workflow.setSecurityContext(new TestSecurityContext(request, new HashSet<>(singletonList("bad"))));
    request.getSession().setAttribute(BaseHttpSessionSecurityContext.USER_SESSION_KEY, "user"); // Log in the user

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

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, request, response, mvcConfiguration);
    workflow.setSecurityContext(new TestSecurityContext(request, emptySet()));

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);

    assertEquals(response.getRedirect(), "/login");
    assertEquals(session.getAttribute(SavedHttpRequest.INITIAL_SESSION_KEY), new SavedHttpRequest("/secure?test=value&test2=value2", null));
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

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, request, response, mvcConfiguration);
    workflow.setSecurityContext(new TestSecurityContext(request, emptySet()));

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);

    assertEquals(response.getRedirect(), "/login");
    assertEquals(session.getAttribute(SavedHttpRequest.INITIAL_SESSION_KEY), new SavedHttpRequest("/secure", request.getParameterMap()));
  }

  @Test
  public void performAuthenticationRequiredSuccess() throws Exception {
    ActionConfiguration configuration = actionConfigurationBuilder.build(Secure.class);
    ActionInvocation actionInvocation = new ActionInvocation(new Secure(), null, null, null, configuration);
    DefaultActionInvocationStore store = new DefaultActionInvocationStore(request);
    store.setCurrent(actionInvocation);

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, request, response, mvcConfiguration);
    workflow.setSecurityContext(new TestSecurityContext(request, new HashSet<>(singletonList("admin"))));
    request.getSession().setAttribute(BaseHttpSessionSecurityContext.USER_SESSION_KEY, "user"); // Log in the user

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

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, request, response, mvcConfiguration);
    workflow.setSecurityContext(new TestSecurityContext(request, new HashSet<>(singletonList("bad"))));
    request.getSession().setAttribute(BaseHttpSessionSecurityContext.USER_SESSION_KEY, "user"); // Log in the user

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

    DefaultSecurityWorkflow workflow = new DefaultSecurityWorkflow(store, request, response, mvcConfiguration);

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  public static class TestSecurityContext extends BaseHttpSessionSecurityContext {
    private final Set<String> roles;

    public TestSecurityContext(HttpServletRequest request, Set<String> roles) {
      super(request);
      this.roles = roles;
    }

    @Override
    public Set<String> getCurrentUsersRoles() {
      return roles;
    }
  }
}
