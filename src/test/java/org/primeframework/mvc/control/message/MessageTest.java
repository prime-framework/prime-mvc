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

import java.util.List;

import com.google.inject.Inject;
import org.example.action.user.EditAction;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.control.ControlBaseTest;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.testng.Assert.fail;

/**
 * This class tests the message control.
 *
 * @author Brian Pontarelli
 */
public class MessageTest extends ControlBaseTest {
  @Inject Message message;

  @Test
  public void defaultMessage() {
    EditAction action = new EditAction();
    ais.setCurrent(new ActionInvocation(action, null, "/user/edit", null, null));
    new ControlTester(message).
        attr("key", "bad").
        attr("default", "Message").
        go("Message");
  }

  @Test
  public void messageAction() {
    EditAction action = new EditAction();
    ais.setCurrent(new ActionInvocation(action, null, "/user/edit", null, null));
    new ControlTester(message).
        attr("key", "key").
        go("American English Message");
  }

  @Test
  public void messageBundleWithParams() {
    EditAction action = new EditAction();
    ais.setCurrent(new ActionInvocation(action, null, "/user/edit", null, null));
    new ControlTester(message).
        attr("key", "params").
        attr("values", List.of("Params")).
        go("Params Message");
  }

  @Test
  public void messageFailure() {
    EditAction action = new EditAction();
    ais.setCurrent(new ActionInvocation(action, null, "/user/edit", null, null));
    try {
      new ControlTester(message).
          attr("key", "bad").
          go("Bundle message");
      fail("Should have failed");
    } catch (PrimeException e) {
      // Expected
    }
  }
}