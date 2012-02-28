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
package org.primeframework.mvc.result.message.control;

import org.example.action.user.Edit;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.result.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This class tests the field messages control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class FieldMessagesTest extends ControlBaseTest {
    @Inject FieldMessages fieldMessages;

    @Test
    public void testFieldMessages() {
        Edit action = new Edit();

        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));

        messageStore.addFieldMessage(MessageScope.REQUEST, "field", "fieldError1");
        messageStore.addFieldMessage(MessageScope.REQUEST, "field", "fieldError2");
        run(fieldMessages,
            mapNV("errors", false),
            null, "<ul class=\"field-errors\">\n" +
            "  <li class=\"field-error\">error1</li>\n" +
            "  <li class=\"field-error\">error2</li>\n" +
            "</ul>\n"
        );
    }

    @Test
    public void testFieldMessageError() {
        Edit action = new Edit();

        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));

        messageStore.addFieldError(MessageScope.REQUEST, "field", "fieldError1");
        messageStore.addFieldError(MessageScope.REQUEST, "field", "fieldError2");

        run(fieldMessages,
            mapNV("errors", true),
            null, "<ul class=\"field-errors\">\n" +
            "  <li class=\"field-error\">error1</li>\n" +
            "  <li class=\"field-error\">error2</li>\n" +
            "</ul>\n"
        );
    }

    @Test
    public void testFieldMessageNamedMissing() {
        Edit action = new Edit();

        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));

        messageStore.addFieldMessage(MessageScope.REQUEST, "field", "fieldError1");
        messageStore.addFieldMessage(MessageScope.REQUEST, "field", "fieldError2");

        run(fieldMessages,
            mapNV("errors", false, "fields", "missing"),
            null, ""
        );
    }

    @Test
    public void testFieldMessageNamed() {
        Edit action = new Edit();

        ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));

        messageStore.addFieldMessage(MessageScope.REQUEST, "field", "fieldError1");
        messageStore.addFieldMessage(MessageScope.REQUEST, "field", "fieldError2");
        run(fieldMessages,
            mapNV("errors", false, "fields", "field"),
            null, "<ul class=\"field-errors\">\n" +
            "  <li class=\"field-error\">error1</li>\n" +
            "  <li class=\"field-error\">error2</li>\n" +
            "</ul>\n"
        );
    }
}