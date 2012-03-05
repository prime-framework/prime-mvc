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
package org.primeframework.mvc.control;

import javax.servlet.http.HttpServletRequest;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;
import org.primeframework.mvc.control.form.AppendAttributesMethod;
import org.primeframework.mvc.control.form.JoinMethod;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.locale.annotation.CurrentLocale;

import net.java.error.ErrorList;

import com.google.inject.Inject;

/**
 * This class an abstract Control implementation that is useful for creating new controls that might need access to
 * things such as the request, the action invocation and attributes.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractControl implements Control {
  protected final Map<String, Object> attributes = new TreeMap<String, Object>();
  protected final Map<String, String> dynamicAttributes = new TreeMap<String, String>();
  protected final Map<String, Object> parameters = new TreeMap<String, Object>();
  protected Locale locale;
  protected FreeMarkerService freeMarkerService;
  protected HttpServletRequest request;
  protected ActionInvocationStore actionInvocationStore;
  protected Object root;

  @Inject
  public void setServices(@CurrentLocale Locale locale, HttpServletRequest request,
                          ActionInvocationStore actionInvocationStore, FreeMarkerService freeMarkerService) {
    this.locale = locale;
    this.request = request;
    this.freeMarkerService = freeMarkerService;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * Implements the controls renderStart method that is called directly by the JSP taglibs. This method is the main
   * render point for the control and it uses the {@link FreeMarkerService} to render the control. Sub-classes need to
   * implement a number of methods in order to setup the Map that is passed to FreeMarker as well as determine the name
   * of the template.
   *
   * @param writer            The writer to output to.
   * @param attributes        The attributes.
   * @param dynamicAttributes The dynamic attributes from the tag. Dynamic attributes start with an underscore.
   */
  public void renderStart(Writer writer, Map<String, Object> attributes, Map<String, String> dynamicAttributes) {
    this.attributes.clear();
    this.dynamicAttributes.clear();
    this.parameters.clear();

    this.attributes.putAll(attributes);
    this.dynamicAttributes.putAll(dynamicAttributes);

    verifyAttributes();
    addAdditionalAttributes();
    this.parameters.putAll(makeParameters());
    this.root = makeRoot();

    if (startTemplateName() != null) {
      String templateName = "/WEB-INF/control-templates/" + startTemplateName();
      freeMarkerService.render(writer, templateName, root, locale);
    }
  }

  /**
   * This implementation just calls the Body implementation to render the body.
   *
   * @param writer The writer to write the body to.
   * @param body   The body.
   */
  public void renderBody(Writer writer, Body body) {
    body.render(writer);
  }

  /**
   * Implements the controls renderEnd method that is called directly by the JSP taglibs. This method is the main render
   * point for the control and it uses the {@link FreeMarkerService} to render the control. Sub-classes need to
   * implement a number of methods in order to setup the Map that is passed to FreeMarker as well as determine the name
   * of the template
   *
   * @param writer The writer to output to.
   */
  public void renderEnd(Writer writer) {
    if (endTemplateName() != null) {
      String templateName = "/WEB-INF/control-templates/" + endTemplateName();
      freeMarkerService.render(writer, templateName, root, locale);
    }
  }

  /**
   * Creates the parameters Map that is the root node used by the FreeMarker template when rendering. This places these
   * values in the root map:
   * <p/>
   * <ul> <li>attributes - The attributes</li> <li>dynamic_attributes - The dynamic attributes</li>
   * <li>append_attributes - A FreeMarker method that appends attributes ({@link AppendAttributesMethod})</li> </ul>
   *
   * @return The Parameters Map.
   */
  protected Map<String, Object> makeParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("attributes", attributes);
    parameters.put("dynamic_attributes", dynamicAttributes);
//        parameters.put("append_attributes", new AppendAttributesMethod());
    parameters.put("make_class", new JoinMethod());
    return parameters;
  }

  /**
   * Converts the given parameters into a FreeMarker root node. This can be overridden by sub-classes to convert the Map
   * or wrap it. This method simply returns the given Map.
   *
   * @return The root.
   */
  protected Object makeRoot() {
    return parameters;
  }

  /**
   * Sub-classes can implement this method to add additional attributes. This is primarily used by control tags to
   * determine values, checked states, selected options, etc.
   */
  protected void addAdditionalAttributes() {
  }

  /**
   * @return The name of the FreeMarker template that this control renders when it starts.
   */
  protected abstract String startTemplateName();

  /**
   * @return The name of the FreeMarker template that this control renders when it ends.
   */
  protected abstract String endTemplateName();

  /**
   * @return The control name, which is usually the simple class name all lowercased.
   */
  protected String controlName() {
    return getClass().getSimpleName().toLowerCase();
  }

  /**
   * @return The current action invocation.
   */
  protected ActionInvocation currentInvocation() {
    return actionInvocationStore.getCurrent();
  }

  /**
   * @return The current action or null.
   */
  protected Object currentAction() {
    return currentInvocation().action();
  }

  /**
   * Verifies that all the attributes are correctly defined for the control.
   */
  private void verifyAttributes() {
    ErrorList errors = new ErrorList();
    Class<?> type = getClass();
    ControlAttributes ca = type.getAnnotation(ControlAttributes.class);
    if (ca != null) {
      ControlAttribute[] requiredAttributes = ca.required();
      verifyAttributes(attributes, requiredAttributes, true, errors);
      verifyAttributes(attributes, requiredAttributes, false, errors);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(errors.toString());
    }
  }

  private void verifyAttributes(Map<String, Object> attributes, ControlAttribute[] controlAttributes,
                                boolean required, ErrorList errors) {
    for (ControlAttribute controlAttribute : controlAttributes) {
      Object value = attributes.get(controlAttribute.name());
      if (value == null && required) {
        errors.addError("The control [" + controlName() + "] is missing the required attribute [" +
          controlAttribute.name() + "]");
      } else if (value != null) {
        Class<?>[] attributeTypes = controlAttribute.types();
        boolean found = false;
        for (Class<?> attributeType : attributeTypes) {
          found |= attributeType.isInstance(value);
          if (found) {
            break;
          }
        }

        if (!found) {
          errors.addError("The control [" + controlName() + "] has an invalid attribute [" +
            controlAttribute.name() + "]. It must be an instance of [" +
            toTypeListString(attributeTypes) + "]");
        }
      }
    }
  }

  private String toTypeListString(Class<?>[] attributeTypes) {
    StringBuilder build = new StringBuilder();
    for (int i = 0; i < attributeTypes.length; i++) {
      Class<?> attributeType = attributeTypes[i];
      build.append(attributeType.toString());
      if (i == attributeTypes.length - 2) {
        build.append(", or ");
      } else if (i > 0) {
        build.append(", ");
      }
    }

    return build.toString();
  }
}