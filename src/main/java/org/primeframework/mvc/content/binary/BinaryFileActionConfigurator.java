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
package org.primeframework.mvc.content.binary;

import java.util.Map;

import org.primeframework.mvc.action.config.ActionConfigurator;
import org.primeframework.mvc.content.binary.annotation.BinaryRequest;
import org.primeframework.mvc.content.binary.annotation.BinaryResponse;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * @author Daniel DeGroff
 */
public class BinaryFileActionConfigurator implements ActionConfigurator {
  @Override
  public Object configure(Class<?> actionClass) {
    Map<String, BinaryRequest> binaryRequestMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, BinaryRequest.class);
    Map<String, BinaryResponse> binaryResponseMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, BinaryResponse.class);
    if (binaryRequestMembers.size() > 1 || binaryResponseMembers.size() > 1) {
      throw new IllegalArgumentException("Action class [" + actionClass + "] contains multiple fields with the @BinaryRequest or @BinaryResponse annotation. This annotation should only exist on a single field.");
    }

    String requestMember = (binaryRequestMembers.size() == 1) ? binaryRequestMembers.keySet().iterator().next() : null;
    String responseMember = (binaryResponseMembers.size() == 1) ? binaryResponseMembers.keySet().iterator().next() : null;
    if (requestMember != null || responseMember != null) {
      return new BinaryFileActionConfiguration(requestMember, responseMember);
    }

    return null;
  }
}
