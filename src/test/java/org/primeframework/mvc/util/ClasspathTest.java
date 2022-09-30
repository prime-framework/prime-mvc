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
package org.primeframework.mvc.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import org.primeframework.mvc.util.Classpath.ClasspathBuilder;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel DeGroff
 */
public class ClasspathTest {

  /**
   * Assert that we do not escape a plus sign, but we escape curly brackets.
   */
  @Test
  public void clean() throws Exception {
    assertCleanedPath("src/test/resources/jars/test+foo.jar");
    assertCleanedPath("src/test/resources/jars/test-foo-{integration}.jar");
  }

  private void assertCleanedPath(String urlString)
      throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException, URISyntaxException {
    URL[] urls = new URL[]{new File(urlString).toURI().toURL()};
    URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
    ClasspathBuilder classPathBuilder = new ClasspathBuilder(urlClassLoader);

    Method method = ClasspathBuilder.class.getDeclaredMethod("clean", URL.class);
    method.setAccessible(true);
    String actualPath = (String) method.invoke(classPathBuilder, urls[0]);
    assertEquals(actualPath, new File(urlString).getAbsoluteFile().toString());
  }
}
