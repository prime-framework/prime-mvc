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
package org.jcatapult.mvc.result.control;

import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Map;

import org.jcatapult.mvc.action.ActionInvocationStore;
import org.jcatapult.mvc.message.MessageStore;
import org.jcatapult.test.JCatapultBaseTest;
import static org.junit.Assert.*;
import org.junit.Ignore;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This class is a base test for the controls.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@Ignore
public class ControlBaseTest extends JCatapultBaseTest {
    @Inject protected ActionInvocationStore ais;
    @Inject protected MessageStore messageStore;

    /**
     * Runs the control and verifies the output.
     *
     * @param   control The control to run.
     * @param   attributes The attributes passed to the control.
     * @param   body The body.
     * @param   expected The expected output.
     */
    protected void run(Control control, Map<String, Object> attributes, final String body, String expected) {
        StringWriter writer = new StringWriter();
        control.renderStart(writer, attributes, map("param", "param-value"));
        
        if (body != null) {
            control.renderBody(writer, new Body() {
                public void render(Writer writer) {
                    try {
                        writer.write(body);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        control.renderEnd(writer);
        assertEquals(expected, writer.toString());
    }
}