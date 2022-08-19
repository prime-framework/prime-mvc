/*
 * Copyright (c) 2013-2022, Inversoft Inc., All Rights Reserved
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
import java.util.Set;

import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.http.HTTPMethod;

/**
 * Jackson action configuration.
 *
 * @author Brian Pontarelli
 */
public class JacksonActionConfiguration {
  public final Map<HTTPMethod, RequestMember> requestMembers;

  public final ResponseMember responseMember;

  public JacksonActionConfiguration(Map<HTTPMethod, RequestMember> requestMembers, ResponseMember responseMember) {
    this.requestMembers = requestMembers;
    this.responseMember = responseMember;
  }

  public Class<?> getSerializationView() {
    if (responseMember == null || responseMember.annotation == null) {
      return null;
    }

    return responseMember.annotation.view();
  }

  public static class RequestMember {
    public Set<String> allowedContentTypes;

    public String name;

    public Class<?> type;

    public RequestMember(String name, Class<?> type) {
      this.name = name;
      this.allowedContentTypes = Set.of("application/json");
      this.type = type;
    }

    public RequestMember(String name, Set<String> contentTYpe, Class<?> type) {
      this.name = name;
      this.allowedContentTypes = contentTYpe;
      this.type = type;
    }
  }

  public static class ResponseMember {
    public JSONResponse annotation;

    public String name;

    public ResponseMember(JSONResponse annotation, String name) {
      this.annotation = annotation;
      this.name = name;
    }
  }
}
