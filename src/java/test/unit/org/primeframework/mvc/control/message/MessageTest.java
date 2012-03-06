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
package org.primeframework.mvc.control.message;

import org.example.action.user.Edit;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import static org.testng.Assert.*;

/**
 * This class tests the message control.
 *
 * @author Brian Pontarelli
 */
public class MessageTest extends ControlBaseTest {
  @Inject Message message;

  @Test
  public void messageAction() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
    new ControlTester(message).
      attr("key", "key").
      go("American English Message");
  }

  @Test
  public void messageBundle() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
    new ControlTester(message).
      attr("key", "key").
      attr("bundle", "/user/edit-bundle").
      go("Bundle Message");
  }

  @Test
  public void messageBundleWithParams() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
    new ControlTester(message).
      attr("key", "params").
      attr("bundle", "/user/edit-bundle").
      attr("values", asList("Params")).
      go("Params Message");
  }

  @Test
  public void messageFailure() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
    try {
      new ControlTester(message).
        attr("key", "bad").
        go("Bundle message");
      fail("Should have failed");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  @Test
  public void defaultMessage() {
    Edit action = new Edit();
    ais.setCurrent(new DefaultActionInvocation(action, "/user/edit", null, null));
    new ControlTester(message).
      attr("key", "bad").
      attr("default", "Message").
      go("Message");
  }
}