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
package org.primeframework.mvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.ThrowingRunnable;

/**
 * @author Daniel DeGroff
 */
public class TestBuilder {

  public RequestSimulator simulator;

  public Path tempFile;

  public TestBuilder createFile() throws IOException {
    return createFile("Test File");
  }

  public TestBuilder createFile(String contents) throws IOException {
    String tmpdir = System.getProperty("java.io.tmpdir");
    String unique = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()), "UTF-8").substring(0, 5);
    tempFile = Paths.get(tmpdir + "/" + "_prime_binaryContent_" + unique);
    tempFile.toFile().deleteOnExit();

    Files.write(tempFile, contents.getBytes());
    return this;
  }

  public TestBuilder simulate(ThrowingRunnable runnable) throws Exception {
    runnable.run();
    return this;
  }
}
