/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import org.primeframework.mvc.security.saved.SavedRequestHttpServletRequest;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * Default saved request workflow that uses the {@link SavedHttpRequest} from the session to mock out the request.
 *
 * @author Brian Pontarelli
 */
public class DefaultSavedRequestWorkflow implements SavedRequestWorkflow {
  private final HttpServletRequest request;

  @Inject
  public DefaultSavedRequestWorkflow(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
      SavedHttpRequest savedHttpRequest = (SavedHttpRequest) httpSession.getAttribute(SavedHttpRequest.LOGGED_IN_SESSION_KEY);
      if (savedHttpRequest != null) {
        httpSession.removeAttribute(SavedHttpRequest.LOGGED_IN_SESSION_KEY);

        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) request;
        HttpServletRequest previous = (HttpServletRequest) wrapper.getRequest();
        wrapper.setRequest(new SavedRequestHttpServletRequest(previous, savedHttpRequest));
      }
    }

    workflowChain.continueWorkflow();
  }
}
