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
package org.jcatapult.mvc.test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Module;
import org.jcatapult.guice.GuiceContainer;
import org.jcatapult.mvc.message.MessageStore;
import org.jcatapult.mvc.servlet.MVCWorkflow;
import org.jcatapult.servlet.ServletObjectsHolder;
import org.jcatapult.servlet.WorkflowChain;
import org.jcatapult.test.servlet.MockHttpServletRequest;
import org.jcatapult.test.servlet.MockHttpServletResponse;
import org.jcatapult.test.servlet.MockHttpSession;
import org.jcatapult.test.servlet.MockServletContext;
import org.jcatapult.test.servlet.WebTestHelper;

/**
 * <p>
 * This class is a test helper that assists in executing MVC actions and
 * testing the entire MVC workflow for the action.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class WebappTestRunner {
    public MVCWorkflow workflow;
    public MessageStore messageStore;
    public MockHttpServletRequest request;
    public MockHttpServletResponse response;
    public MockServletContext context;
    public MockHttpSession session;
    public boolean guiceSetup;

    public WebappTestRunner() {
        this.context = makeContext();
        ServletObjectsHolder.setServletContext(context);

        this.session = makeSession(context);
    }

    @Inject
    public void setServices(MVCWorkflow workflow, MessageStore messageStore) {
        this.workflow = workflow;
        this.messageStore = messageStore;
    }

    public RequestBuilder test(String uri) {
        return new RequestBuilder(uri, this);
    }

    void run(RequestBuilder builder) throws IOException, ServletException {
        // Build the request and response for this pass
        this.request = builder.getRequest();
        ServletObjectsHolder.clearServletRequest();
        ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(request));

        this.response = makeResponse();
        ServletObjectsHolder.clearServletResponse();
        ServletObjectsHolder.setServletResponse(response);

        // If the Guice stuff has already been setup for a previous test using the same runner, use
        // it, but check that they didn't try to mock anything out.
        if (guiceSetup) {
            if (!builder.getModules().isEmpty()) {
                throw new AssertionError("You can't mock out any interfaces unless you create a new " +
                    "WebappTestRunner. Reusing the same WebappTestRunner ensures that the Guice " +
                    "injector is re-used, which simulates multiple requests to the same webapp.");
            }
        } else {
            GuiceContainer.setGuiceModules(builder.getModules().toArray(new Module[builder.getModules().size()]));
            GuiceContainer.inject();
            GuiceContainer.initialize();
            guiceSetup = true;
        }

        // Inject
        GuiceContainer.getInjector().injectMembers(this);

        workflow.perform(new WorkflowChain() {
            public void continueWorkflow() {
            }

            public void reset() {
            }
        });
    }

    /**
     * @return  Makes a HttpServletResponse as a nice mock. Sub-classes can override this
     */
    protected MockHttpServletResponse makeResponse() {
        return new MockHttpServletResponse();
    }

    /**
     * @return  Makes a MockServletContext
     */
    protected MockServletContext makeContext() {
        return WebTestHelper.makeContext();
    }

    /**
     * @param   context The mock servlet context.
     * @return  Makes a MockHttpSession.
     */
    protected MockHttpSession makeSession(MockServletContext context) {
        return WebTestHelper.makeSession(context);
    }
}
