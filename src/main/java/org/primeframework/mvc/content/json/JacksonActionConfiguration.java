/*
 * Copyright (c) 2013-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.json;

import java.util.Map;

import org.primeframework.mvc.servlet.HTTPMethod;

/**
 * Jackson action configuration.
 *
 * @author Brian Pontarelli
 */
public class JacksonActionConfiguration {
  public final Map<HTTPMethod, RequestMember> requestMembers;

  public final String responseMember;

  public Class<?> serializationView;

  public JacksonActionConfiguration(Map<HTTPMethod, RequestMember> requestMembers, String responseMember) {
    this.requestMembers = requestMembers;
    this.responseMember = responseMember;
    this.serializationView = null;
  }

  public JacksonActionConfiguration(Map<HTTPMethod, RequestMember> requestMembers, String responseMember, Class<?> serializationView) {
    this.requestMembers = requestMembers;
    this.responseMember = responseMember;
    this.serializationView = serializationView;
  }

  public static class RequestMember {
    public String name;

    public Class<?> type;

    public RequestMember(String name, Class<?> type) {
      this.name = name;
      this.type = type;
    }
  }
}
