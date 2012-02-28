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
package org.jcatapult.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * <p>
 * This is the default workflow resolver and it locates the workflows defined in the configuration file
 * when it is loaded up. This is a singleton for performance reasons.
 * </p>
 *
 * @author Brian Pontarelli
 */
@Singleton
public class DefaultWorkflowResolver implements WorkflowResolver {
    private static final Logger logger = Logger.getLogger(DefaultWorkflowResolver.class.getName());
    private final List<Class<? extends Workflow>> types = new ArrayList<Class<? extends Workflow>>();
    private final Injector injector;

    @Inject
    @SuppressWarnings("unchecked")
    public DefaultWorkflowResolver(@Named("jcatapult.workflows") String workflows, Injector injector) {
        this.injector = injector;
      
        String[] types = workflows.split(",");
        for (String type : types) {
            try {
                Class<? extends Workflow> klass = (Class<? extends Workflow>) Class.forName(type);
                this.types.add(klass);
            } catch (ClassNotFoundException e) {
                logger.severe("++++++++++++++++++++++++++++++++ A workflow was defined that doesn't exist [" + type + "] ++++++++++++++++++++++++++++++++");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Workflow> resolve() {
        List<Workflow> workflows = new ArrayList<Workflow>();
        for (Class<? extends Workflow> type : types) {
            workflows.add(injector.getInstance(type));
        }

        return workflows;
    }

    /**
     * {@inheritDoc}
     */
    public List<Class<? extends Workflow>> getTypes() {
        return types;
    }
}
