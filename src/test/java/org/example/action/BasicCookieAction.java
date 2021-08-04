/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.scope.annotation.BasicCookie;

/**
 * @author Daniel DeGroff
 */
@Action
@Status
public class BasicCookieAction {

  public boolean deleteCookie1;

  public boolean deleteCookie2;

  public boolean deleteCookie3;

  @BasicCookie(encrypt = false)

  public String stringCookie1;

  @BasicCookie(encrypt = false)

  public String stringCookie2;

  @BasicCookie(value = "fusionauth.sso", encrypt = false)
  public String stringCookie3;

  public String writeCookie1;

  public String writeCookie2;

  public String writeCookie3;

  public String get() {
    if (writeCookie1 != null) {
      stringCookie1 = writeCookie1;
    }

    if (writeCookie2 != null) {
      stringCookie2 = writeCookie2;
    }


    if (writeCookie3 != null) {
      stringCookie3 = writeCookie3;
    }

    if (deleteCookie1) {
      stringCookie1 = null;
    }

    if (deleteCookie2) {
      stringCookie2 = null;
    }

    if (deleteCookie3) {
      stringCookie3 = null;
    }

    return "success";
  }
}
