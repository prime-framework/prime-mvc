/*
 * Copyright (c) 2017-2018, Inversoft Inc., All Rights Reserved
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
package org.example.action.scope;

import io.fusionauth.http.FileInfo;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;

/**
 * This action takes the URL segment and sets it to 'id' field.
 *
 * @author Daniel DeGroff
 */
@Action(value = "{id}")
public class PageOneAction {
  // This only exists on PageOneAction, not PageTwoAction
  @FileUpload(contentTypes = {"*"})
  public FileInfo file;

  public String id;

  public String get() {
    return "input";
  }

  public String post() {
    return "input";
  }
}
