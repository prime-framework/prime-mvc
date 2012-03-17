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
package org.primeframework.mvc.control;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.util.MapBuilder;

import com.google.inject.Inject;
import static org.testng.Assert.*;

/**
 * This class is a base test for the controls.
 *
 * @author Brian Pontarelli
 */
public class ControlBaseTest extends PrimeBaseTest {
  @Inject protected ActionInvocationStore ais;
  @Inject protected MessageStore messageStore;

  /**
   * A tester for controls.
   */
  public static class ControlTester {
    private final Control control;
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private String body;

    public ControlTester(Control control) {
      this.control = control;
    }

    public ControlTester attr(String name, Object value) {
      this.attributes.put(name, value);
      return this;
    }
    
    public ControlTester body(String body) {
      this.body = body;
      return this;
    }
    
    public void go(String result) {
      StringWriter writer = new StringWriter();
      control.renderStart(writer, attributes, MapBuilder.map("param", "param-value").done());

      if (body != null) {
        control.renderBody(writer, new Body() {
          public void render(Writer writer) {
            try {
              writer.write(body);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }

      control.renderEnd(writer);
      assertEquals(result, writer.toString());
    }
  }
}