/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ActionMapper;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.control.AbstractControl;
import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;
import org.primeframework.mvc.parameter.ParameterHandler;
import org.primeframework.mvc.parameter.ParameterParser;
import org.primeframework.mvc.parameter.ParameterParser.Parameters;
import org.primeframework.mvc.parameter.PostParameterHandler;
import org.primeframework.mvc.scope.ScopeRetriever;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.servlet.ServletTools;
import org.primeframework.mvc.workflow.DefaultMVCWorkflow;

/**
 * This is the form control that is used for rendering the open and close form tags.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(required = {
    @ControlAttribute(name = "action")
})
public class Form extends AbstractControl {
  private final ActionConfigurationProvider actionConfigurationProvider;

  private final ActionInvocationStore actionInvocationStore;

  private final ActionMapper actionMapper;

  private final FormPreparer formPreparer;

  private final ParameterHandler parameterHandler;

  private final ParameterParser parameterParser;

  private final PostParameterHandler postParameterHandler;

  private final ScopeRetriever scopeRetriever;

  private boolean differentURI = false;

  @Inject
  public Form(ActionConfigurationProvider actionConfigurationProvider,
              FormPreparer formPreparer, ActionInvocationStore actionInvocationStore, ActionMapper actionMapper,
              ParameterParser parameterParser, ParameterHandler parameterHandler,
              PostParameterHandler postParameterHandler, ScopeRetriever scopeRetriever) {
    this.actionConfigurationProvider = actionConfigurationProvider;
    this.formPreparer = formPreparer;
    this.actionInvocationStore = actionInvocationStore;
    this.actionMapper = actionMapper;
    this.parameterHandler = parameterHandler;
    this.parameterParser = parameterParser;
    this.postParameterHandler = postParameterHandler;
    this.scopeRetriever = scopeRetriever;
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
      differentURI = false;
    }

    super.renderEnd(writer);
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
      String method = (String) attributes.get("method");
      HTTPMethod httpMethod = HTTPMethod.GET;
      if (method != null && !method.toUpperCase().equals("GET") && !method.toUpperCase().equals("POST")) {
        throw new PrimeException("Invalid method [" + method + "] for form. Only standard GET and POST methods are allowed.");
      } else if (method != null) {
        httpMethod = HTTPMethod.valueOf(method.toUpperCase());
      }

      ActionInvocation current = actionInvocationStore.getCurrent();
      ActionInvocation actionInvocation = actionConfigurationProvider.lookup(action);
      if (current == null || current.action == null || actionInvocation == null || actionInvocation.configuration == null ||
          current.action.getClass() != actionInvocation.configuration.actionClass) {
        // - Now that we know we need it, map the action before setting it as the current value in the invocation store
        //   And we can now verify we can build the action. Not sure this is necessary?
        actionInvocation = actionMapper.map(httpMethod, action, false);
        if (actionInvocation == null || actionInvocation.action == null) {
          throw new PrimeException("The form action [" + action + "] is not a valid URI that maps to an action " +
              "class by the Prime MVC.");
        }

        // Call setCurrent first, then prepare the action
        actionInvocationStore.setCurrent(actionInvocation);
        prepareActionInvocation(actionInvocation);
        differentURI = true;
      }
    }

    formPreparer.prepare();

    // Fix the action URI to include the context path and jsessionid (if one exists)
    String contextPath = request.getContextPath();
    if (contextPath.length() > 0 && !fullyQualified) {
      action = contextPath + action;
    }

    action += ServletTools.getSessionId(request);
    attributes.put("action", action);

    // Render
    super.renderStart(writer, attributes, dynamicAttributes);
  }

  /**
   * @return form-end.ftl
   */
  protected String endTemplateName() {
    return "form-end.ftl";
  }

  /**
   * @return form-start.ftl
   */
  protected String startTemplateName() {
    return "form-start.ftl";
  }

  /**
   * Fill out the action so that when the form prepare methods are called the action is properly constructed. This is
   * intended to be used when this action invocation is not really the current, but it has been set as the current to
   * handle embedded forms.
   * <p>
   * Any equivalent workflow tasks performed should follow the same order as used in the {@link DefaultMVCWorkflow}
   *
   * @param actionInvocation the action invocation.
   */
  private void prepareActionInvocation(ActionInvocation actionInvocation) {
    // See ScopeRetrievalWorkflow
    scopeRetriever.setScopedValues(actionInvocation);

    // Move required to optional since we are handling request parameters for a form action and it is possible that not all of the
    // parameters will exist in the target action.
    Parameters parameters = parameterParser.parse();
    parameters.optional.putAll(parameters.required);
    parameters.required.clear();

    // Also do not attempt to handle files in this workflow.
    parameters.files.clear();

    // See ParameterWorkflow
    parameterHandler.handle(parameters);

    // See PostParameterWorkflow
    postParameterHandler.handle(actionInvocation);
  }
}