/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.message;

import org.primeframework.mvc.PrimeBaseTest;
import org.testng.annotations.Test;

public class AlternateMessageResourcesTest extends PrimeBaseTest {
  @Test
  public void action_has_message() {
    // this message exists in a standard path by convention - src/test/web/messages/message-resources-annotated.properties
    simulator.test("/alternate-message-resources-annotated")
             .withParameter("messageKey", "normal_message")
             .get()
             .assertStatusCode(200)
             .assertContainsGeneralMessageCodes(MessageType.INFO, "normal_message");
  }

  @Test
  public void none_of_them_have_message() {
    simulator.test("/alternate-message-resources-annotated")
             .withParameter("messageKey", "foobar")
             .get()
             .assertStatusCode(200)
             .assertContainsGeneralMessageCodes(MessageType.ERROR, "foobar");
  }

  @Test
  public void other_action_has_message() {
    // this message only exists in NestedMessageAction's path - in src/test/web/messages/nested/nested-message.properties
    simulator.test("/alternate-message-resources-annotated")
             .withParameter("messageKey", "nested_message")
             .get()
             .assertStatusCode(200)
             .assertContainsGeneralMessageCodes(MessageType.INFO, "nested_message");
  }
}
