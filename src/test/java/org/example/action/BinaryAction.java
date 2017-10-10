/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
package org.example.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.binary.annotation.BinaryRequest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Daniel DeGroff
 */
@Action
@Status
public class BinaryAction {
  public String expected;

  @BinaryRequest
  public Path file;

  public String post() {
    assertNotNull(expected, "You need to pass in the expected value on the request.");
    assertNotNull(file);
    try {
      assertEquals(new String(Files.readAllBytes(file)), expected);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertEquals(file.toFile().length(), expected.getBytes().length);

    return "success";
  }
}
