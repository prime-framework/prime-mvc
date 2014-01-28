/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter;

import org.primeframework.mvc.parameter.fileupload.FileInfo;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This interface defines how parameters are retrieved from the request (usually) and separated into Required, Optional
 * PreParameter, and File groups. Each parameter key can also have attributes from the request that are used for things
 * like conversions and such.
 *
 * @author Brian Pontarelli
 */
public interface ParameterParser {

  /**
   * Parses the parameters.
   *
   * @return The parsed parameters.
   */
  Parameters parse();

  public static class Parameters {
    public final Map<String, Struct> required = new LinkedHashMap<String, Struct>();
    public final Map<String, Struct> optional = new LinkedHashMap<String, Struct>();
    public final Map<String, Struct> pre = new LinkedHashMap<String, Struct>();
    public final Map<String, List<FileInfo>> files = new LinkedHashMap<String, List<FileInfo>>();

    public static class Struct {
      public Map<String, String> attributes = new LinkedHashMap<String, String>();
      public String[] values;

      public Struct() {
      }

      public Struct(String[] values) {
        this.values = values;
      }

      @Override
      public String toString() {
        return "Struct{" +
            "attributes=" + attributes +
            ", values=" + Arrays.toString(values) +
            '}';
      }
    }

    @Override
    public String toString() {
      return "Parameters{" +
          "required=" + required +
          ", optional=" + optional +
          ", pre=" + pre +
          ", files=" + files +
          '}';
    }
  }
}
