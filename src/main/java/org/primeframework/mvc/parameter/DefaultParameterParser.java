/*
 * Copyright (c) 2012-2018, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.ParameterParser.Parameters.Struct;
import org.primeframework.mvc.parameter.RequestBodyWorkflow.ParameterHttpServletRequestWrapper;
import org.primeframework.mvc.parameter.annotation.ConjoinedRequestParameters;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.util.QueryStringTools;
import org.primeframework.mvc.util.RequestKeys;

/**
 * This class is the default parameter parser. It pulls all of the parameters from the request and puts them into groups
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

  private final HttpServletRequest request;

  @Inject
  public DefaultParameterParser(MVCConfiguration configuration, ActionInvocationStore actionInvocationStore,
                                HttpServletRequest request) {
    this.configuration = configuration;
    this.actionInvocationStore = actionInvocationStore;
    this.request = request;
  }

  @Override
  public Parameters parse() {
    Map<String, String[]> parameters = getParameterMap();
    Parameters result = new Parameters();

    // Grab the files from the request
    addFiles(result);

    // Pull out the check box, radio button and action parameter
    if (!parameters.isEmpty()) {
      Map<String, String[]> checkBoxes = new HashMap<>();
      Map<String, String[]> radioButtons = new HashMap<>();
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

  @SuppressWarnings("unchecked")
  protected void addFiles(Parameters result) {
    Map<String, List<FileInfo>> fileInfos = (Map<String, List<FileInfo>>) request.getAttribute(RequestKeys.FILE_ATTRIBUTE);
    if (fileInfos != null) {
      result.files.putAll(fileInfos);
    }
  }

  protected void addUncheckedValues(Map<String, String[]> map, Parameters parameters) {
    for (String key : map.keySet()) {
      String[] values = map.get(key);
      // Only add the values if there is a single one and it is empty, which denotes that they
      // want to set null into the action, or the values are multiple and they is one non-empty
      // value in the bunch. The second case occurs when they are using multiple checkboxes or
      // radio buttons for the same name. This will cause multiple hidden inputs and they should
      // all either have a unchecked value or should all be empty. If they are all empty, then
      // null should be put into the object.
      if ((values != null && values.length == 1 && values[0].equals("")) || empty(values)) {
        parameters.required.put(key, new Struct());
      } else {
        parameters.required.put(key, new Struct(values));
      }
    }
  }

  protected boolean empty(String[] values) {
    if (values != null && values.length > 0) {
      for (String value : values) {
        if (!value.equals("")) {
          return false;
        }
      }
    }

    return true;
  }

  private Map<String, String[]> getParameterMap() {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    ConjoinedRequestParameters conjoinedRequestParameters = (ConjoinedRequestParameters) actionInvocation.method.annotations.get(ConjoinedRequestParameters.class);

    // If this is a conjoined request, extract the parameters.
    if (conjoinedRequestParameters != null) {
      // Please Note:
      // A current limitation of this feature is that this is occurring in the ParameterWorkflow which is after the
      // ScopeRetrievalWorkflow. This means if you were to use @Request annotation on a field in your action - AND
      // you are using "conjoined" request parameters, the values will not be expanded prior to the scope retrieval. I
      // don't know if this is a use case we want to support, but if we do, this code may need to be moved earlier in
      // the workflow.
      //
      // Most ideally, this would be part of the RequestBodyWorkflow, but that occurs prior to the Action Mapping so we do
      // not yet have the ActionInvocation to see if an annotation exists on the method handler.

      // Request the conjoined string from the request, if it is not provided, the request will be processed normally.
      String requestParametersValue = request.getParameter(conjoinedRequestParameters.value());
      if (requestParametersValue != null) {
        // Convert to a map of String[]
        Map<String, String[]> parameters = QueryStringTools.parseQueryString(requestParametersValue)
                                                           .entrySet()
                                                           .stream()
                                                           .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toArray(new String[]{})));

        // Re-write the request to contain the re-constituted request parameters
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) request;
        HttpServletRequest previous = (HttpServletRequest) wrapper.getRequest();
        HttpServletRequest newRequest = new ParameterHttpServletRequestWrapper(previous, parameters);
        wrapper.setRequest(newRequest);
      }
    }

    return request.getParameterMap();
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

  private void separateParameters(Map<String, String[]> parameters, Parameters result, Map<String, String[]> checkBoxes,
                                  Map<String, String[]> radioButtons, Set<String> actions) {
    for (String key : parameters.keySet()) {
      if (InternalParameters.isInternalParameter(key)) {
        continue;
      }

      boolean optional = (key.endsWith(".x") || key.endsWith(".y"));

      if (key.startsWith(CHECKBOX_PREFIX)) {
        checkBoxes.put(key.substring(CHECKBOX_PREFIX.length()), parameters.get(key));
      } else if (key.startsWith(RADIOBUTTON_PREFIX)) {
        radioButtons.put(key.substring(RADIOBUTTON_PREFIX.length()), parameters.get(key));
      } else if (key.startsWith(ACTION_PREFIX)) {
        actions.add(key.substring(ACTION_PREFIX.length()));
      } else {
        int index = key.indexOf("@");
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
          s.attributes.put(key.substring(index + 1), parameters.get(key)[0]);
        } else {
          // If the ignore empty parameters flag is set, which IS NOT the default, this
          // block will only ever add the values to the structure if they contain at least
          // one non-empty String.
          String[] values = parameters.get(parameter);
          if (!configuration.ignoreEmptyParameters() || !empty(values)) {
            s.values = values;
          }
        }
      }
    }
  }
}
