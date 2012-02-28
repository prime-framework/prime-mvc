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
package org.jcatapult.test;

import javax.servlet.http.HttpServletRequestWrapper;
import java.util.List;
import java.util.logging.Logger;

import org.jcatapult.guice.GuiceContainer;
import org.jcatapult.jndi.MockJNDI;
import org.jcatapult.servlet.ServletObjectsHolder;
import org.jcatapult.test.servlet.MockHttpServletRequest;
import org.jcatapult.test.servlet.MockHttpServletResponse;
import org.jcatapult.test.servlet.MockHttpSession;
import org.jcatapult.test.servlet.MockServletContext;
import org.jcatapult.test.servlet.WebTestHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import net.java.util.CollectionTools;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * <p>
 * This class is a base test for the JCatapult framework that helps to boot-strap
 * testing by setting up JNDI, Guice and other things. This assumes that applications
 * are constructed in the manner specified by the JCatapult documentation in order
 * to find the correct configuration files.
 * </p>
 *
 * @author  Brian Pontarelli and James Humphrey
 */
@Ignore
public abstract class JCatapultBaseTest {
    private static final Logger logger = Logger.getLogger(JCatapultBaseTest.class.getName());
    public static final MockJNDI jndi = new MockJNDI();
    protected List<Module> modules = CollectionTools.list();
    protected Injector injector;
    protected MockServletContext context;
    protected MockHttpSession session;
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    /**
     * Sets up a mock JNDI tree and sets the environment to test.
     */
    @BeforeClass
    public static void setUpJNDI() {
        jndi.bind("java:comp/env/environment", "development");
        jndi.activate();
    }

    /**
     * Allows sub-classes to setup a different set of modules to use. This should be called from the
     * constructor.
     *
     * @param   modules The modules to use for injection.
     */
    public void setModules(Module... modules) {
        this.modules = CollectionTools.llist(modules);
    }

    /**
     * Allows sub-classes to setup a different set of modules to use. This should be called from the
     * constructor.
     *
     * @param   modules The modules to use for injection.
     */
    public void addModules(Module... modules) {
        this.modules.addAll(CollectionTools.list(modules));
    }

    /**
     * Sets up Guice and Configuration.
     */
    @Before
    public void setUp() {
        setUpServletObjects();
        setUpGuice();
    }

    /**
     * Creates the servlet request, servlet response and context and puts them into the holder.
     */
    protected void setUpServletObjects() {
        this.context = makeContext();
        this.session = makeSession(context);
        this.request = makeRequest(session);
        this.response = makeResponse();

        ServletObjectsHolder.setServletContext(context);
        ServletObjectsHolder.clearServletRequest();
        ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(request));
        ServletObjectsHolder.clearServletResponse();
        ServletObjectsHolder.setServletResponse(response);
    }

    /**
     * Constructs a request whose URI is /test, Locale is US, is a GET and encoded using UTF-8
     * by calling the by calling the {@link WebTestHelper#makeRequest(MockHttpSession)} method.
     *
     * @param   session The mock session.
     * @return  The mock request.
     */
    protected MockHttpServletRequest makeRequest(MockHttpSession session) {
        return WebTestHelper.makeRequest(session);
    }

    /**
     * Constructs a mock response by calling the {@link WebTestHelper#makeResponse()} method.
     *
     * @return  The mock response.
     */
    protected MockHttpServletResponse makeResponse() {
        return WebTestHelper.makeResponse();
    }

    /**
     * Constructs a mock session by calling the {@link WebTestHelper#makeSession(MockServletContext)} method.
     *
     * @param   context The mock servlet context.
     * @return  The mock session.
     */
    protected MockHttpSession makeSession(MockServletContext context) {
        return WebTestHelper.makeSession(context);
    }

    /**
     * Constructs a mock servlet context by calling the {@link WebTestHelper#makeContext()} method.
     *
     * @return  The mock context.
     */
    protected MockServletContext makeContext() {
        return WebTestHelper.makeContext();
    }

    /**
     * Sets up the configuration and then the injector.
     */
    public void setUpGuice() {
        if (modules.size() > 0) {
            StringBuffer moduleNames = new StringBuffer(" ");
            for (Module module : modules) {
                moduleNames.append(module.getClass().getName()).append(" ");
            }

            logger.fine("Setting up injection with modules [" + moduleNames.toString() + "]");
            GuiceContainer.setGuiceModules(modules.toArray(new Module[modules.size()]));
        }

        GuiceContainer.inject();
        GuiceContainer.initialize();
        injector = GuiceContainer.getInjector();
        injector.injectMembers(this);
    }
}
