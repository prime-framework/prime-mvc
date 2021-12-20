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

import org.primeframework.mvc.content.binary.annotation.BinaryRequest;
import org.primeframework.mvc.content.binary.annotation.BinaryResponse;

/**
 * @author Daniel DeGroff
 */
public class BinaryActionConfiguration {
  /**
   * Indicates if Prime should delete the file annotated with {@link BinaryRequest} once the action has completed.
   */
  public boolean deleteRequestMemberUponCompletion;

  /**
   * Indicates if Prime should delete the file annotated with {@link BinaryResponse} once it has been successfully
   * written to the output stream.
   */
  public boolean deleteResponseMemberUponCompletion;

  public String requestMember;

  public String responseMember;

  public BinaryActionConfiguration(String requestMember, String responseMember) {
    this.requestMember = requestMember;
    this.responseMember = responseMember;
  }
}
