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
import javax.servlet.ServletException;

import org.easymock.EasyMock;
import org.example.action.user.Edit;
import org.example.domain.Address;
import org.example.domain.User;
import org.jcatapult.mvc.action.ActionInvocationStore;
import org.jcatapult.mvc.action.DefaultActionInvocation;
import org.jcatapult.mvc.message.MessageStore;
import org.jcatapult.mvc.message.scope.MessageType;
import org.jcatapult.mvc.parameter.InternalParameters;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;
import org.jcatapult.servlet.WorkflowChain;
import org.jcatapult.test.JCatapultBaseTest;
import static org.junit.Assert.*;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * <p>
 * This class tests the DefaultValidationWorkflow.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultValidationWorkflowTest extends JCatapultBaseTest {
    protected ActionInvocationStore actionInvocationStore;
    protected ExpressionEvaluator expressionEvaluator;
    protected ValidatorProvider validatorProvider;
    protected MessageStore messageStore;

    @Inject
    public void setServices(ActionInvocationStore actionInvocationStore,
                            ExpressionEvaluator expressionEvaluator, ValidatorProvider validatorProvider,
                            MessageStore messageStore) {
        this.actionInvocationStore = actionInvocationStore;
        this.expressionEvaluator = expressionEvaluator;
        this.validatorProvider = validatorProvider;
        this.messageStore = messageStore;
    }

    @Test
    public void testValidationTurnedOff() throws IOException, ServletException {
        request.setPost(true);
        request.setParameter(InternalParameters.JCATAPULT_EXECUTE_VALIDATION, "false");
        
        Edit action = new Edit();
        action.user = new User();
        action.user.setName("Fred");
        action.user.setAge(12);
        action.user.setSecurityQuestions(new String[]{"What is your name?"});
        action.user.setAddress("home", new Address());
        action.user.setAddress("work", new Address());
        action.user.getAddress("home").setCity("Boulder");
        action.user.getAddress("home").setStreet("Main");
        action.user.getAddress("home").setCountry("US");
        action.user.getAddress("work").setCity("Boulder");
        action.user.getAddress("work").setStreet("Main");
        action.user.getAddress("work").setCountry("US");
        actionInvocationStore.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        DefaultValidationWorkflow workflow = new DefaultValidationWorkflow(request, actionInvocationStore,
                expressionEvaluator, validatorProvider, messageStore);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        chain.continueWorkflow();
        EasyMock.replay(chain);

        workflow.perform(chain);

        assertFalse(messageStore.contains(MessageType.ERROR));

        EasyMock.verify(chain);
    }

    @Test
    public void testValidationOnForGet() throws IOException, ServletException {
        request.setPost(false);
        request.setParameter("__jc_a_foo", "");

        Edit action = new Edit();
        action.user = new User();
        action.user.setName("Fred");
        action.user.setAge(12);
        action.user.setSecurityQuestions(new String[]{"What is your name?"});
        action.user.setAddress("home", new Address());
        action.user.setAddress("work", new Address());
        action.user.getAddress("home").setCity("Boulder");
        action.user.getAddress("home").setStreet("Main");
        action.user.getAddress("home").setCountry("US");
        action.user.getAddress("work").setCity("Boulder");
        action.user.getAddress("work").setStreet("Main");
        action.user.getAddress("work").setCountry("US");
        actionInvocationStore.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        DefaultValidationWorkflow workflow = new DefaultValidationWorkflow(request, actionInvocationStore,
                expressionEvaluator, validatorProvider, messageStore);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        chain.continueWorkflow();
        EasyMock.replay(chain);

        workflow.perform(chain);

        assertTrue(messageStore.contains(MessageType.ERROR));

        EasyMock.verify(chain);
    }

    @Test
    public void testAllNull() throws IOException, ServletException {
        Edit action = new Edit();
        request.setPost(true);
        actionInvocationStore.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        DefaultValidationWorkflow workflow = new DefaultValidationWorkflow(request, actionInvocationStore,
                expressionEvaluator, validatorProvider, messageStore);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        chain.continueWorkflow();
        EasyMock.replay(chain);

        workflow.perform(chain);

        assertTrue(messageStore.contains(MessageType.ERROR));
        assertEquals(9, messageStore.getFieldMessages(MessageType.ERROR).size());

        EasyMock.verify(chain);
    }

    @Test
    public void testClassLevel() throws IOException, ServletException {
        Edit action = new Edit();
        request.setPost(true);
        action.user = new User();
        action.user.setName("Fred");
        action.user.setAge(12);
        action.user.setSecurityQuestions(new String[]{"What is your name?"});
        action.user.setAddress("home", new Address());
        action.user.setAddress("work", new Address());
        action.user.getAddress("home").setCity("Boulder");
        action.user.getAddress("home").setStreet("Main");
        action.user.getAddress("home").setCountry("US");
        action.user.getAddress("work").setCity("Boulder");
        action.user.getAddress("work").setStreet("Main");
        action.user.getAddress("work").setCountry("US");
        actionInvocationStore.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        DefaultValidationWorkflow workflow = new DefaultValidationWorkflow(request, actionInvocationStore,
                expressionEvaluator, validatorProvider, messageStore);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        chain.continueWorkflow();
        EasyMock.replay(chain);

        workflow.perform(chain);

        assertTrue(messageStore.contains(MessageType.ERROR));
        assertEquals(4, messageStore.getFieldMessages(MessageType.ERROR).size());

        EasyMock.verify(chain);
    }
}