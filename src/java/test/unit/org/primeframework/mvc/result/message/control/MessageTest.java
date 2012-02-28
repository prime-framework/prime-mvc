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
package org.primeframework.mvc.result.message.control;

import org.example.action.user.Edit;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.result.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static java.util.Arrays.asList;
import static net.java.util.CollectionTools.*;
import static org.testng.Assert.*;

/**
 * <p>
 * This class tests the message control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class MessageTest extends ControlBaseTest {
    @Inject Message message;

    @Test
    public void testMessageAction() {
        Edit action = new Edit();
        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        run(message,
            mapNV("key", "key"),
            null, "American English Message"
        );
    }

    @Test
    public void testMessageBundle() {
        Edit action = new Edit();
        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        run(message,
            mapNV("key", "key", "bundle", "/user/edit-bundle"),
            null, "Bundle Message"
        );
    }

    @Test
    public void messageBundleWithParams() {
        Edit action = new Edit();
        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        run(message,
            mapNV("key", "params", "bundle", "/user/edit-bundle", "values", asList("Params")),
            null, "Params Message"
        );
    }

    @Test
    public void testMessageFailure() {
        Edit action = new Edit();
        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        try {
            run(message,
            mapNV("key", "bad"),
                null, "Bundle message"
            );
            fail("Should have failed");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void testDefaultMessage() {
        Edit action = new Edit();
        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
        run(message,
            mapNV("key", "bad", "default", "Message"),
            null, "Message"
        );
    }
}