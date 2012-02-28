/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.easymock.EasyMock;
import org.example.action.ExtensionInheritance;
import org.example.action.Simple;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.servlet.WorkflowChain;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * <p>
 * This class tests the default action invocation workflow.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultResultInvocationWorkflowTest {
    @Test
    public void actionLessWithDefault() throws IOException, ServletException {
        HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
        EasyMock.replay(response);

        ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
        ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(ais.getCurrent()).andReturn(ai);
        EasyMock.replay(ais);

        Annotation annotation = new ForwardResult.ForwardImpl("/foo/bar", null);
        ResultInvocation ri = new DefaultResultInvocation(annotation, "/foo/bar", null);
        ResultInvocationProvider rip = EasyMock.createStrictMock(ResultInvocationProvider.class);
        EasyMock.expect(rip.lookup(ai)).andReturn(ri);
        EasyMock.replay(rip);

        Result result = EasyMock.createStrictMock(Result.class);
        result.execute(annotation, ai);
        EasyMock.replay(result);

        ResultProvider resultProvider = EasyMock.createStrictMock(ResultProvider.class);
        EasyMock.expect(resultProvider.lookup(Forward.class)).andReturn(result);
        EasyMock.replay(resultProvider);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        EasyMock.replay(chain);

        DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider);
        workflow.perform(chain);

        EasyMock.verify(response, ais, rip, result, resultProvider, chain);
    }

    @Test
    public void actionLessWithoutDefault() throws IOException, ServletException {
        HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
        EasyMock.replay(response);

        ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
        ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(ais.getCurrent()).andReturn(ai);
        EasyMock.replay(ais);

        ResultInvocationProvider rip = EasyMock.createStrictMock(ResultInvocationProvider.class);
        EasyMock.expect(rip.lookup(ai)).andReturn(null);
        EasyMock.replay(rip);

        Result result = EasyMock.createStrictMock(Result.class);
        EasyMock.replay(result);

        ResultProvider resultProvider = EasyMock.createStrictMock(ResultProvider.class);
        EasyMock.replay(resultProvider);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        chain.continueWorkflow();
        EasyMock.replay(chain);

        DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider);
        workflow.perform(chain);

        EasyMock.verify(response, ais, rip, result, resultProvider, chain);
    }

    @Test
    public void actionWithResult() throws IOException, ServletException {
        HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
        EasyMock.replay(response);

        Simple simple = new Simple();
        ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), true, true, "success");
        ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(ais.getCurrent()).andReturn(invocation);
        EasyMock.replay(ais);

        Annotation annotation = new ForwardResult.ForwardImpl("/foo/bar", "success");
        ResultInvocation ri = new DefaultResultInvocation(annotation, "/foo/bar", "success");
        ResultInvocationProvider rip = EasyMock.createStrictMock(ResultInvocationProvider.class);
        EasyMock.expect(rip.lookup(invocation, "success")).andReturn(ri);
        EasyMock.replay(rip);

        Result result = EasyMock.createStrictMock(Result.class);
        result.execute(annotation, invocation);
        EasyMock.replay(result);

        ResultProvider resultProvider = EasyMock.createStrictMock(ResultProvider.class);
        EasyMock.expect(resultProvider.lookup(annotation.annotationType())).andReturn(result);
        EasyMock.replay(resultProvider);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        EasyMock.replay(chain);

        DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider);
        workflow.perform(chain);

        EasyMock.verify(response, ais, rip, result, resultProvider, chain);
    }

    @Test
    public void actionSuppressResult() throws IOException, ServletException {
        HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
        EasyMock.replay(response);

        Simple simple = new Simple();
        ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), false, true, "success");
        ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(ais.getCurrent()).andReturn(invocation);
        EasyMock.replay(ais);

        ResultInvocationProvider rip = EasyMock.createStrictMock(ResultInvocationProvider.class);
        EasyMock.replay(rip);

        Result result = EasyMock.createStrictMock(Result.class);
        EasyMock.replay(result);

        ResultProvider resultProvider = EasyMock.createStrictMock(ResultProvider.class);
        EasyMock.replay(resultProvider);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        EasyMock.replay(chain);

        DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider);
        workflow.perform(chain);

        EasyMock.verify(response, ais, rip, result, resultProvider, chain);
    }

    @Test
    public void actionMissingResult() throws IOException, ServletException {
        HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
        response.setStatus(404);
        EasyMock.replay(response);

        Simple simple = new Simple();
        ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), true, true, "success");
        ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(ais.getCurrent()).andReturn(invocation);
        EasyMock.replay(ais);

        ResultInvocationProvider rip = EasyMock.createStrictMock(ResultInvocationProvider.class);
        EasyMock.expect(rip.lookup(invocation, "success")).andReturn(null);
        EasyMock.replay(rip);

        ResultProvider resultProvider = EasyMock.createStrictMock(ResultProvider.class);
        EasyMock.replay(resultProvider);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        EasyMock.replay(chain);

        DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider);
        try {
            workflow.perform(chain);
            fail("Should have failed with 404");
        } catch (ServletException e) {
            System.out.println(e);
            // Expected
        }

        EasyMock.verify(response, ais, rip, resultProvider, chain);
    }

    @Test
    public void actionMissingResultType() throws IOException {
        HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
        EasyMock.replay(response);

        Simple simple = new Simple();
        ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), true, true, "success");
        ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(ais.getCurrent()).andReturn(invocation);
        EasyMock.replay(ais);

        Annotation annotation = new ForwardResult.ForwardImpl("/foo/bar", "success");
        ResultInvocation ri = new DefaultResultInvocation(annotation, "/foo/bar", "success");
        ResultInvocationProvider rip = EasyMock.createStrictMock(ResultInvocationProvider.class);
        EasyMock.expect(rip.lookup(invocation, "success")).andReturn(ri);
        EasyMock.replay(rip);

        ResultProvider resultProvider = EasyMock.createStrictMock(ResultProvider.class);
        EasyMock.expect(resultProvider.lookup(annotation.annotationType())).andReturn(null);
        EasyMock.replay(resultProvider);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        EasyMock.replay(chain);

        DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider);
        try {
            workflow.perform(chain);
            fail("Should have failed");
        } catch (ServletException e) {
            System.out.println(e);
            // Expected
        }

        EasyMock.verify(response, ais, rip, resultProvider, chain);
    }

    @Test
    public void actionExtension() throws IOException, ServletException {
        HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
        EasyMock.replay(response);

        ExtensionInheritance action = new ExtensionInheritance();
        ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", "ajax", null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), true, true, "ajax");
        ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(ais.getCurrent()).andReturn(invocation);
        EasyMock.replay(ais);

        Annotation annotation = new ForwardResult.ForwardImpl("/foo/bar", "ajax");
        ResultInvocation ri = new DefaultResultInvocation(annotation, "/foo/bar", "ajax");
        ResultInvocationProvider rip = EasyMock.createStrictMock(ResultInvocationProvider.class);
        EasyMock.expect(rip.lookup(invocation, "ajax")).andReturn(ri);
        EasyMock.replay(rip);

        Result result = EasyMock.createStrictMock(Result.class);
        result.execute(annotation, invocation);
        EasyMock.replay(result);

        ResultProvider resultProvider = EasyMock.createStrictMock(ResultProvider.class);
        EasyMock.expect(resultProvider.lookup(annotation.annotationType())).andReturn(result);
        EasyMock.replay(resultProvider);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        EasyMock.replay(chain);

        DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider);
        workflow.perform(chain);

        EasyMock.verify(response, ais, rip, resultProvider, chain);
    }
}
