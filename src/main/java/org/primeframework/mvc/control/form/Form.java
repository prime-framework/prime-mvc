/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.control.form;

import java.io.Writer;
import java.util.Map;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ActionMapper;
import org.primeframework.mvc.control.AbstractControl;
import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;

import com.google.inject.Inject;

/**
 * This is the form control that is used for rendering the open and close form tags.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
  required = {
    @ControlAttribute(name = "action")
  }
)
public class Form extends AbstractControl {
  private final FormPreparer formPreparer;
  private final ActionInvocationStore actionInvocationStore;
  private final ActionMapper actionMapper;
  private boolean differentURI = false;

  @Inject
  public Form(FormPreparer formPreparer, ActionInvocationStore actionInvocationStore, ActionMapper actionMapper) {
    this.formPreparer = formPreparer;
    this.actionInvocationStore = actionInvocationStore;
    this.actionMapper = actionMapper;
  }

  @Override
  public String getName() {
    return "form";
  }

  /**
   * Overrides the renderStart in order to change the current ActionInvocation if the action for the form is different
   * than the current invocation action.
   *
   * @param writer            The writer to output to.
   * @param attributes        The attributes.
   * @param dynamicAttributes The dynamic attributes from the tag.
   */
  @Override
  public void renderStart(Writer writer, Map<String, Object> attributes, Map<String, String> dynamicAttributes) {
    String action = (String) attributes.get("action");
    boolean fullyQualified = action.startsWith("http://") || action.startsWith("https://");

    // Handle relative URIs such as 'delete' with a current URI of '/user/' will result in
    // a new URI of '/user/delete'
    if (!action.startsWith("/") && !fullyQualified) {
      String currentURI = currentInvocation().uri();
      int index = currentURI.lastIndexOf("/");
      if (index >= 0) {
        action = currentURI.substring(0, index) + "/" + action;
      } else if (currentURI.equals("")) {
        action = "/" + action;
      }
    }

    if (!fullyQualified) {
      ActionInvocation current = actionInvocationStore.getCurrent();

      ActionInvocation actionInvocation = actionMapper.map(action, false);
      if (actionInvocation == null || actionInvocation.action() == null) {
        throw new PrimeException("The form action [" + action + "] is not a valid URI that maps to an action " +
          "class by the Prime MVC.");
      } else if (current == null || current.action() == null ||
        !current.action().getClass().equals(actionInvocation.action().getClass())) {
        actionInvocationStore.setCurrent(actionInvocation);
        differentURI = true;
      }
    }

    formPreparer.prepare();

    // Fix the action URI to include the context path
    String contextPath = request.getContextPath();
    if (contextPath.length() > 0 && !fullyQualified) {
      attributes.put("action", contextPath + action);
    }

    // Render
    super.renderStart(writer, attributes, dynamicAttributes);
  }

  /**
   * Overrides the renderEnd method to pop the action invocation of the form from the stack.
   *
   * @param writer The writer to output to.
   */
  @Override
  public void renderEnd(Writer writer) {
    if (differentURI) {
      actionInvocationStore.removeCurrent();
    }

    super.renderEnd(writer);
  }

  /**
   * @return form-start.ftl
   */
  protected String startTemplateName() {
    return "form-start.ftl";
  }

  /**
   * @return form-end.ftl
   */
  protected String endTemplateName() {
    return "form-end.ftl";
  }
}