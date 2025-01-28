/*
 * Copyright (c) 2015-2025, Inversoft Inc., All Rights Reserved
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
package org.example.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fusionauth.http.HTTPValues.Methods;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.annotation.ConstraintOverrideMethod;

/**
 * Secure action.
 *
 * @author Brian Pontarelli
 */
@Action(requiresAuthentication = true, constraints = {"admin", "user"}, scheme = "user")
@Status.List({
    @Status(code = "unauthenticated", status = 401),
    @Status(code = "unauthorized", status = 403)
})
public class SecureAction {
  @UnknownParameters
  public static Map<String, String[]> UnknownParameters = new HashMap<>();

  @ConstraintOverrideMethod(httpMethods = {Methods.PATCH})
  public List<String> customConstraintsForPatch() {
    return List.of("patch-only");
  }

  @ConstraintOverrideMethod(httpMethods = {Methods.PUT})
  public List<String> customConstraintsForPut() {
    return List.of("put-only");
  }

  @ConstraintOverride("delete-only")
  public String delete() {
    return "success";
  }

  public String get() {
    return "success";
  }

  public String post() {
    return "success";
  }

  public String put() {
    return "success";
  }
}
