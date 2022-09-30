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
 *
 */
package org.primeframework.mvc.freemarker;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This tests the URLTemplateSource class.
 *
 * @author Brian Pontarelli
 */
public class URLTemplateSourceTest {
  /**
   * Ensures that the file in the JAR is older that the one on the file system. This also ensures that the JAR file last
   * modified doesn't change.
   *
   * @throws IOException Never probably.
   */
  @Test
  public void classPath() throws IOException {
    File file = new File("src/conf/test/unit/logging.properties");
    URLTemplateSource source = new URLTemplateSource(Thread.currentThread().getContextClassLoader().getResource("logging.properties"));
    assertTrue(source.lastModified() > file.lastModified());
    assertEquals(source.lastModified(), source.lastModified());
    assertNotNull(source.getInputStream());
    source.close();
  }

  @Test
  public void file() throws IOException {
    File file = new File("build.savant");
    URLTemplateSource source = new URLTemplateSource(file.toURI().toURL());
    assertEquals(file.lastModified(), source.lastModified());
    assertNotNull(source.getInputStream());
    source.close();
  }
}