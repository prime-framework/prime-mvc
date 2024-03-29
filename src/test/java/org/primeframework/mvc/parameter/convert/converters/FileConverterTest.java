/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.convert.converters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * This tests the file converter.
 *
 * @author Brian Pontarelli
 */
public class FileConverterTest {
  /**
   * Test the conversion from Strings.
   */
  @Test
  public void fromStrings() {
    Map<String, String> map = new HashMap<>();
    GlobalConverter converter = new FileConverter();
    File f = (File) converter.convertFromStrings(File.class, map, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(f);

    f = (File) converter.convertFromStrings(File.class, map, "testExpr", ArrayUtils.toArray(""));
    assertNull(f);

    f = (File) converter.convertFromStrings(File.class, map, "testExpr", ArrayUtils.toArray("/tmp"));
    assertEquals(f.getAbsolutePath(), "/tmp");

    f = (File) converter.convertFromStrings(File.class, map, "testExpr", ArrayUtils.toArray("/tmp", "prime"));
    assertEquals(f.getAbsolutePath(), "/tmp/prime");

    f = (File) converter.convertFromStrings(File.class, map, "testExpr", ArrayUtils.toArray("build.savant"));
    assertEquals(f.getAbsolutePath(), new File("build.savant").getAbsolutePath());

    File[] fa = (File[]) converter.convertFromStrings(File[].class, map, "testExpr", ArrayUtils.toArray("build.savant", "build.xml"));
    assertEquals(fa[0].getAbsolutePath(), new File("build.savant").getAbsolutePath());
    assertEquals(fa[1].getAbsolutePath(), new File("build.xml").getAbsolutePath());

    // Test parentDir
    map.put("parentDir", "/tmp");
    f = (File) converter.convertFromStrings(File.class, map, "testExpr", ArrayUtils.toArray("build.savant"));
    assertEquals(f.getAbsolutePath(), new File("/tmp/build.savant").getAbsolutePath());
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new FileConverter();
    String str = converter.convertToString(File.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(File.class, null, "testExpr", new File("build.savant"));
    assertEquals(str, new File("build.savant").getAbsolutePath());
  }
}
