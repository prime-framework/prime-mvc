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
package org.primeframework.mvc.control.message.control;

import org.example.action.user.Edit;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.control.message.ActionMessages;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.control.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p> This class tests the action messages control. </p>
 *
 * @author Brian Pontarelli
 */
public class ActionMessagesTest extends ControlBaseTest {
  @Inject ActionMessages actionMessages;

  @Test
  public void testActionMessages() {
    Edit action = new Edit();

    ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));

    messageStore.addActionMessage(MessageScope.REQUEST, "actionError1");
    messageStore.addActionMessage(MessageScope.REQUEST, "actionError2");

    run(actionMessages,
      mapNV("errors", false),
      null, "<ul class=\"action-messages\">\n" +
      "  <li class=\"action-message\">error1</li>\n" +
      "  <li class=\"action-message\">error2</li>\n" +
      "</ul>\n"
    );
  }

  @Test
  public void testActionMessageError() {
    Edit action = new Edit();

    ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));

    messageStore.addActionError(MessageScope.REQUEST, "actionError1");
    messageStore.addActionError(MessageScope.REQUEST, "actionError2");

    run(actionMessages,
      mapNV("errors", true),
      null, "<ul class=\"action-errors\">\n" +
      "  <li class=\"action-error\">error1</li>\n" +
      "  <li class=\"action-error\">error2</li>\n" +
      "</ul>\n"
    );
  }
}