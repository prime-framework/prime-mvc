/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.action.config.ActionConfigurator;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.util.ReflectionUtils;

import java.util.Map;

/**
 * Uses the Jackson JSON processor to marshall JSON into Java objects and set them into the action.
 *
 * @author Brian Pontarelli
 */
public class JacksonActionConfigurator implements ActionConfigurator {
  @Override
  public Object configure(Class<?> actionClass) {
    Map<String, JSONRequest> jsonRequestMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, JSONRequest.class);
    Map<String, JSONResponse> jsonResponseMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, JSONResponse.class);
    if (jsonRequestMembers.size() > 1 || jsonResponseMembers.size() > 1) {
      throw new IllegalArgumentException("Action class [" + actionClass + "] contains multiple fields with the @JSONRequest or @JSONResponse annotation. This annotation should only exist on a single field.");
    }

    String requestMember = (jsonRequestMembers.size() == 1) ? jsonRequestMembers.keySet().iterator().next() : null;
    Class<?> requestMemberType = (requestMember != null) ? ReflectionUtils.getMemberType(actionClass, requestMember) : null;
    String responseMember = (jsonResponseMembers.size() == 1) ? jsonResponseMembers.keySet().iterator().next() : null;
    Class<?> responseMemberType = (responseMember != null) ? ReflectionUtils.getMemberType(actionClass, responseMember) : null;
    if (requestMember != null || responseMember != null) {
      return new JacksonActionConfiguration(requestMember, requestMemberType, responseMember, responseMemberType);
    }

    return null;
  }
}
