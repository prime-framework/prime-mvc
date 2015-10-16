/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import org.testng.annotations.Test;

/**
 * @author Daniel DeGroff
 */
public class AcceptanceTest extends PrimeBaseTest {

  @Test
  public void get_absoluteForward() throws Exception {
    simulator.test("/absolute-forward-result")
             .get()
             .assertStatusCode(200)
             .assertBody("Absolute Forward");
  }

  @Test
  public void get_action_noTemplate() throws Exception {
    simulator.test("/no-template")
             .expectException(PrimeException.class)
             .get()
             .assertStatusCode(500)
             .assertBodyIsEmpty();
  }

  @Test
  public void get_action_withTemplate() throws Exception {
    simulator.test("/action-template")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Action and Template")
             .assertBodyContainsMessagesFromKeys("message-key");
  }

  @Test
  public void get_defaultForward() throws Exception {
    simulator.test("/default-forward-result")
             .get()
             .assertStatusCode(200)
             .assertBody("Default Forward");
  }

  @Test
  public void get_noAction_noTemplate() throws Exception {
    // simulator doesn't support chain.continueWorkflow(), so we'll get UnsupportedOperationException
    simulator.test("/no-action-no-template")
             .expectException(UnsupportedOperationException.class)
             .get()
             .assertStatusCode(500)
             .assertBodyIsEmpty();
  }

  @Test
  public void get_noAction_withTemplate() throws Exception {
    simulator.test("/actionless")
             .get()
             .assertStatusCode(200)
             .assertBody("Hello Actionless World");
  }

  @Test
  public void get_nonPrimeResourceEndingWithSlash() throws Exception {
    // simulator doesn't support chain.continueWorkflow(), so we'll get UnsupportedOperationException
    simulator.test("/foo/")
             .expectException(UnsupportedOperationException.class)
             .get()
             .assertStatusCode(500)
             .assertBodyIsEmpty();
  }

  @Test
  public void get_redirectToIndex() throws Exception {
    simulator.test("/redirect")
             .get()
             .assertStatusCode(301)
             .assertRedirect("/redirect/");
  }

  @Test
  public void get_redirectToIndexWithTrailingSlash() throws Exception {
    simulator.test("/redirect/")
             .get()
             .assertStatusCode(200)
             .assertBody("Redirected");
  }

  @Test
  public void get_relativeForward() throws Exception {
    simulator.test("/relative-forward-result")
             .get()
             .assertStatusCode(200)
             .assertBody("Relative Forward");
  }
}
