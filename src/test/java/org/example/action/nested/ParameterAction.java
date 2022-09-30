/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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
package org.example.action.nested;

import org.primeframework.mvc.action.annotation.Action;

/**
 * /nested/foo/parameter/bar
 * <p>
 * 1. /nested/foo/parameter/bar 2. /nested/foo/parameter/index 3. /nested/foo/parameter {bar} 4. /nested/foo {parameter}
 * {bar} 5. /nested {foo} {parameter} {bar} 6. /index {nested} {foo} {parameter} {bar}
 * <p>
 * node(package{nested}) -> node(param{preParam1}) -> node(param{preParam2} -> node(action{parameter})
 * <p>
 * /.well-known/well-known/.well-known/openid-configuration
 * <p>
 * node(package{.well-known}) -> node(package{well-known}) -> node(package{.well-known}) ->
 * node(action{openid-configuration})
 *
 * @author Brian Pontarelli
 */
@Action(value = "{endParam1}/{endParam2}", prefixParameters = "{preParam1}/{preParam2}")
public class ParameterAction {
  public String endParam1;

  public String endParam2;

  public String preParam1;

  public String preParam2;

  public String execute() {
    return "input";
  }
}
