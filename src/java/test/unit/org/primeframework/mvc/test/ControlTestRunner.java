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
package org.primeframework.mvc.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.primeframework.guice.GuiceContainer;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.result.control.Body;
import org.primeframework.mvc.result.control.Control;
import static org.testng.Assert.*;

/**
 * <p>
 * This class is a test helper that assists in testing custom controls.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ControlTestRunner {
    /**
     * Tests the given control and returns a builder that can be used to build up attributes, the
     * body, etc.
     *
     * @param   control The control.
     * @return  The ControlBuilder.
     */
    public ControlBuilder test(Control control) {
        return new ControlBuilder(control);
    }

    /**
     * Runs the control and verifies the output.
     *
     * @param   builder The builder.
     */
    static void run(final ControlBuilder builder) {
        // Setup the action invocation if it isn't null
        ActionInvocationStore ais = GuiceContainer.getInjector().getInstance(ActionInvocationStore.class);
        if (builder.actionInvocation != null) {
            ais.setCurrent(builder.actionInvocation);
        }

        // Run the control
        Control control = builder.control;
        StringWriter writer = new StringWriter();
        control.renderStart(writer, builder.attributes, builder.dynamicAttributes);

        if (builder.body != null) {
            control.renderBody(writer, new Body() {
                public void render(Writer writer) {
                    try {
                        writer.write(builder.body);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        control.renderEnd(writer);
        assertEquals(builder.result, writer.toString());
    }
}