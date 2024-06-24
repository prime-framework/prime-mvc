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
package org.example.action.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Stream;

@Action
@Stream
public class NoContentTypeJsonAction {
  public int length;

  public String name;

  public InputStream stream;

  public String type;

  public String get() {
    try (StringWriter stringWriter = new StringWriter()) {
      stringWriter.write("{\"hello\": 123}");
      stream = new ByteArrayInputStream(stringWriter.toString().getBytes());
      length = stringWriter.getBuffer().length();
      name = "";
      type = "text/plain";
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return "success";
  }
}
