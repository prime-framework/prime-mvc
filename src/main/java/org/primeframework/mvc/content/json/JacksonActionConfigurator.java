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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fusionauth.http.HTTPMethod;
import org.primeframework.mvc.action.config.ActionConfigurator;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.JSONPropertyFilterConfig;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.ResponseMember;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONPropertyFilter;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * Uses the Jackson JSON processor to marshall JSON into Java objects and set them into the action.
 *
 * @author Brian Pontarelli
 */
public class JacksonActionConfigurator implements ActionConfigurator {
  @Override
  public Object configure(Class<?> actionClass) {
    List<Method> jsonFilterMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, JSONPropertyFilter.class);
    Map<String, JSONRequest> jsonRequestMember = ReflectionUtils.findAllMembersWithAnnotation(actionClass, JSONRequest.class);
    Map<String, JSONPatch> jsonPatchRequestMember = ReflectionUtils.findAllMembersWithAnnotation(actionClass, JSONPatch.class);
    Map<String, JSONResponse> jsonResponseMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, JSONResponse.class);
    if (jsonResponseMembers.size() > 1) {
      throw new IllegalArgumentException("Action class [" + actionClass + "] contains multiple fields with the @JSONResponse annotation. This annotation should only exist on a single field.");
    }

    if (jsonFilterMethods.size() > 1) {
      throw new IllegalArgumentException("Action class [" + actionClass + "] contains multiple fields with the @JSONPropertyFilter annotation. This annotation should only exist on a single method.");
    }

    // @JSONRequest members, more than one is allowed, up to one per HTTP Method
    Map<HTTPMethod, RequestMember> configuredMembers = new HashMap<>(4);
    for (Map.Entry<String, JSONRequest> requestMember : jsonRequestMember.entrySet()) {
      String memberName = requestMember.getKey();
      String[] httpMethods = requestMember.getValue().httpMethods();
      for (String method : httpMethods) {
        HTTPMethod httpMethod = HTTPMethod.of(method);
        if (configuredMembers.containsKey(httpMethod)) {
          throw new IllegalArgumentException("Action class [" + actionClass + "] contains multiple fields with the @JSONRequest annotation and they are not distinct for HTTPMethod [ " + httpMethod + "]."
              + " This annotation should only exist on a single field for a particular HTTP Method.");
        }

        // Optionally include the JSONPatch annotation if HTTP Patch
        // - Unless we keep values in this annotation, we could also just leave a boolean marker to indicate this request member is enabled for PATCH.
        JSONPatch jsonPatch = httpMethod == HTTPMethod.PATCH
            ? jsonPatchRequestMember.get(memberName)
            : null;

        configuredMembers.put(httpMethod, new RequestMember(memberName, ReflectionUtils.getMemberType(actionClass, memberName), jsonPatch));
      }

      // If PATCH is not enabled, you can't have JSONPatch on this annotation
      if (jsonPatchRequestMember.containsKey(memberName) && Arrays.stream(httpMethods).noneMatch(m -> m.equals(HTTPMethod.PATCH.name()))) {
        throw new IllegalArgumentException("Action class [" + actionClass + "] contains a field annotated with the @JSONRequest annotation and the @JSONPatch annotation, and is not enabled for the PATCH HTTP Method. In order to use the @JSONPatch annotation, you will need to enable the PATCH HTTP Method on the @JSONRequest annotation.");
      }
    }

    // Response
    Map.Entry<String, JSONResponse> entry = (jsonResponseMembers.size() == 1) ? jsonResponseMembers.entrySet().iterator().next() : null;
    ResponseMember responseMember = null;
    if (entry != null) {
      responseMember = new ResponseMember(entry.getValue(), entry.getKey());
    }

    JSONPropertyFilterConfig jsonFilterConfig = null;
    if (jsonFilterMethods.size() > 0) {
      Method method = jsonFilterMethods.get(0);
      JSONPropertyFilter annotation = method.getAnnotation(JSONPropertyFilter.class);
      jsonFilterConfig = new JSONPropertyFilterConfig(method, annotation.value(), annotation.mixinSource(), annotation.mixinTarget());
    }

    if (!configuredMembers.isEmpty() || responseMember != null) {
      return new JacksonActionConfiguration(configuredMembers, responseMember, jsonFilterConfig);
    }

    return null;
  }
}
