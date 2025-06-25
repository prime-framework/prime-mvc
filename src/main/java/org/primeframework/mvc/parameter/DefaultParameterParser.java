/*
 * Copyright (c) 2012-2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import io.fusionauth.http.FileInfo;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.ParameterParser.Parameters.Struct;
import org.primeframework.mvc.security.csrf.CSRFProvider;

/**
 * This class is the default parameter parser. It pulls all the parameters from the request and puts them into groups
 * that are as follows:
 *
 * <table>
 *   <tr><th>Name</th><th>Description</th></tr>
 *   <tr><td>Pre</td><td>Pre-Parameters that get set into the action before any other parameters. These parameters are annotated with @PreParameter</td></tr>
 *   <tr><td>Optional</td><td>Optional parameters that might not exist on the action and if setting them fails all errors are ignored regardless of the configuration. These include image submit button parameters</td></tr>
 *   <tr><td>File</td><td>File upload parameters.</td></tr>
 *   <tr><td>Required</td><td>Everything else. If any of these parameters don't exist on the action an exception will be thrown if configuration is set to disallow unknown parameters</td></tr>
 * </table>
 *
 * @author Brian Pontarelli
 */
public class DefaultParameterParser implements ParameterParser {
  public static final String ACTION_PREFIX = "__a_";

  public static final String CHECKBOX_PREFIX = "__cb_";

  public static final String RADIOBUTTON_PREFIX = "__rb_";

  private final ActionInvocationStore actionInvocationStore;

  private final MVCConfiguration configuration;

  private final CSRFProvider csrfProvider;

  private final HTTPRequest request;

  @Inject
  public DefaultParameterParser(MVCConfiguration configuration, ActionInvocationStore actionInvocationStore, CSRFProvider csrfProvider,
                                HTTPRequest request) {
    this.configuration = configuration;
    this.actionInvocationStore = actionInvocationStore;
    this.csrfProvider = csrfProvider;
    this.request = request;
  }

  @Override
  public Parameters parse() {
    Map<String, List<String>> parameters = request.getParameters();
    Parameters result = new Parameters();

    // Grab the files from the request
    addFiles(result);

    // Pull out the check box, radio button and action parameter
    if (!parameters.isEmpty()) {
      Map<String, List<String>> checkBoxes = new HashMap<>();
      Map<String, List<String>> radioButtons = new HashMap<>();
      Set<String> actions = new HashSet<>();

      separateParameters(parameters, result, checkBoxes, radioButtons, actions);

      preParameters(result);

      // Remove all the existing checkbox, radio and action keys
      checkBoxes.keySet().removeAll(result.optional.keySet());
      checkBoxes.keySet().removeAll(result.required.keySet());
      radioButtons.keySet().removeAll(result.optional.keySet());
      radioButtons.keySet().removeAll(result.required.keySet());

      // Add back in any left overs
      addUncheckedValues(checkBoxes, result);
      addUncheckedValues(radioButtons, result);

      // Remove actions from the parameter as they should be ignored right now
      result.optional.keySet().removeAll(actions);
      result.required.keySet().removeAll(actions);
    }

    return result;
  }

  protected void addFiles(Parameters result) {
    List<FileInfo> files = request.getFiles();
    for (FileInfo file : files) {
      result.files.computeIfAbsent(file.name, key -> new ArrayList<>()).add(file);
    }
  }

  protected void addUncheckedValues(Map<String, List<String>> map, Parameters parameters) {
    for (String key : map.keySet()) {
      List<String> values = map.get(key);
      // Only add the values if there is a single one and it is empty, which denotes that they
      // want to set null into the action, or the values are multiple and they is one non-empty
      // value in the bunch. The second case occurs when they are using multiple checkboxes or
      // radio buttons for the same name. This will cause multiple hidden inputs and they should
      // all either have a unchecked value or should all be empty. If they are all empty, then
      // null should be put into the object.
      if ((values != null && values.size() == 1 && values.get(0).equals("")) || empty(values)) {
        parameters.required.put(key, new Struct());
      } else {
        parameters.required.put(key, new Struct(values));
      }
    }
  }

  protected boolean empty(List<String> values) {
    if (values != null && values.size() > 0) {
      return values.stream().allMatch(value -> value.equals(""));
    }

    return true;
  }

  private void preParameters(Parameters result) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    for (String name : actionInvocation.configuration.preParameterMembers.keySet()) {
      Struct struct = result.optional.remove(name);
      if (struct == null) {
        struct = result.required.remove(name);
      }

      if (struct != null) {
        result.pre.put(name, struct);
      }
    }
  }

  private void separateParameters(Map<String, List<String>> parameters, Parameters result,
                                  Map<String, List<String>> checkBoxes,
                                  Map<String, List<String>> radioButtons, Set<String> actions) {
    for (String key : parameters.keySet()) {
      boolean optional = key.endsWith(".x") || key.endsWith(".y") || key.equals(csrfProvider.getParameterName());

      if (key.startsWith(CHECKBOX_PREFIX)) {
        checkBoxes.put(key.substring(CHECKBOX_PREFIX.length()), parameters.get(key));
      } else if (key.startsWith(RADIOBUTTON_PREFIX)) {
        radioButtons.put(key.substring(RADIOBUTTON_PREFIX.length()), parameters.get(key));
      } else if (key.startsWith(ACTION_PREFIX)) {
        actions.add(key.substring(ACTION_PREFIX.length()));
      } else {
        int index = key.indexOf('@');
        String parameter = (index > 0) ? key.substring(0, index) : key;
        Struct s;
        if (optional) {
          s = result.optional.get(parameter);
        } else {
          s = result.required.get(parameter);
        }

        if (s == null) {
          s = new Struct();
          if (optional) {
            result.optional.put(parameter, s);
          } else {
            result.required.put(parameter, s);
          }
        }

        if (index > 0) {
          s.attributes.put(key.substring(index + 1), parameters.get(key).get(0));
        } else {
          // If the ignore empty parameters flag is set, which IS NOT the default, this
          // block will only ever add the values to the structure if they contain at least
          // one non-empty String.
          List<String> values = parameters.get(parameter);
          if (!configuration.ignoreEmptyParameters() || !empty(values)) {
            s.values = values;
          }
        }
      }
    }
  }
}
