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
package org.primeframework.mvc.parameter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.primeframework.config.PrimeMVCConfiguration;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.util.MethodTools;
import org.primeframework.mvc.util.RequestKeys;
import org.primeframework.servlet.WorkflowChain;
import org.primeframework.servlet.multipart.FileInfo;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import static net.java.lang.ObjectTools.*;

/**
 * <p>
 * This class uses the {@link ExpressionEvaluator} to process the incoming
 * request parameters. It also handles check boxes, submit buttons and radio
 * buttons.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultParameterWorkflow implements ParameterWorkflow {
    public static final String CHECKBOX_PREFIX = "__jc_cb_";
    public static final String RADIOBUTTON_PREFIX = "__jc_rb_";
    public static final String ACTION_PREFIX = "__jc_a_";

    @Inject public Logger logger;
    private final HttpServletRequest request;
    private final ActionInvocationStore actionInvocationStore;
    private final MessageStore messageStore;
    private final ExpressionEvaluator expressionEvaluator;
    private final PrimeMVCConfiguration configuration;

    private final long maxSize;
    private final String[] contentTypes;
    private boolean ignoreEmptyParameters = false;

    @Inject
    public DefaultParameterWorkflow(HttpServletRequest request, ActionInvocationStore actionInvocationStore,
                                    MessageStore messageStore, ExpressionEvaluator expressionEvaluator,
                                    PrimeMVCConfiguration configuration) {
        this.request = request;
        this.actionInvocationStore = actionInvocationStore;
        this.messageStore = messageStore;
        this.expressionEvaluator = expressionEvaluator;
      this.configuration = configuration;
        this.contentTypes = configuration.fileUploadAllowedTypes();
        this.maxSize = configuration.fileUploadMaxSize();
    }

    @Inject(optional = true)
    public void setIgnoreEmptyParamaters(@Named("jcatapult.mvc.ignoreEmptyParameters") boolean ignoreEmptyParameters) {
        this.ignoreEmptyParameters = ignoreEmptyParameters;
    }

    /**
     * Handles the incoming HTTP request parameters.
     *
     * @param   chain The workflow chain.
     */
    @SuppressWarnings("unchecked")
    public void perform(WorkflowChain chain) throws IOException, ServletException {
        ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
        Object action = actionInvocation.action();

        if (action != null) {
            Map<String, String[]> parameters = request.getParameterMap();

            // First grab the structs
            Parameters params = getValuesToSet(parameters);

            // Next, handle pre parameters if there are any parameters at all
            if (parameters.size() > 0) {
                handlePreParameters(params, action, actionInvocation);
            }

            // Next, invoke pre methods
            MethodTools.invokeAllWithAnnotation(action, PreParameterMethod.class);

            // Next, set the remaining parameters if there are any left
            if (params.optional.size() > 0 || params.required.size() > 0) {
                // Next, set the parameters
                handleParameters(params, action, actionInvocation);
            }

            // Set the files
            Map<String, List<FileInfo>> fileInfos = (Map<String, List<FileInfo>>) request.getAttribute(RequestKeys.FILE_ATTRIBUTE);
            if (fileInfos != null && fileInfos.size() > 0) {
                handleFiles(fileInfos, action, actionInvocation);
            }

            // Finally, invoke post methods
            MethodTools.invokeAllWithAnnotation(action, PostParameterMethod.class);
        }

        chain.continueWorkflow();
    }

    /**
     * Cleanses the HTTP request parameters by removing all the special JCatapult MVC marker parameters
     * for checkboxes, radio buttons and actions. It also adds into the parameters null values for
     * any un-checked checkboxes and un-selected radio buttons. It also collects all of the dynamic
     * attributes for each parameter using the {@code @} delimiter character.
     *
     * @param   parameters The request parameters.
     * @return  The parameters to set into the aciton.
     */
    protected Parameters getValuesToSet(Map<String, String[]> parameters) {
        Parameters result = new Parameters();
        if (parameters.isEmpty()) {
            return result;
        }

        // Pull out the check box, radio button and action parameter
        Map<String, String[]> checkBoxes = new HashMap<String, String[]>();
        Map<String, String[]> radioButtons = new HashMap<String, String[]>();
        Set<String> actions = new HashSet<String>();
        for (String key : parameters.keySet()) {
            if (InternalParameters.isInternalParameter(key)){
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
                    if (!ignoreEmptyParameters || !empty(values)) {
                        s.values = values;
                    }
                }
            }
        }

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

        return result;
    }

    /**
     * Handles any fields or properties annotated with PreParameter. These are removed from the parameters
     * given and set into the PreParameter fields.
     *
     * @param   params The parameters.
     * @param   action The action.
     * @param   actionInvocation The action invocation for the URI and anything else that might be
     *          needed.
     */
    protected void handlePreParameters(Parameters params, Object action, ActionInvocation actionInvocation) {
        Set<String> members = expressionEvaluator.getAllMembers(action.getClass());
        for (String member : members) {
            PreParameter annotation = null;
            try {
                annotation = expressionEvaluator.getAnnotation(PreParameter.class, member, action);
            } catch (ExpressionException e) {
                // Ignore
            }

            if (annotation != null) {
                Struct struct = params.optional.remove(member);
                if (struct == null) {
                    struct = params.required.remove(member);
                }

                if (struct == null) {
                    continue;
                }

                try {
                    expressionEvaluator.setValue(member, action, struct.values, struct.attributes);
                } catch (ConversionException ce) {
                    messageStore.addConversionError(member, actionInvocation.actionURI(), struct.attributes, (Object[]) struct.values);
                }
            }
        }
    }

    /**
     * Sets all of the parameters into the action.
     *
     * @param   params The parameters.
     * @param   action The action.
     * @param   actionInvocation The action invocation for the URI and anything else that might be
     *          needed.
     */
    protected void handleParameters(Parameters params, Object action, ActionInvocation actionInvocation) {
        // First, process the optional
        for (String key : params.optional.keySet()) {
            Struct struct = params.optional.get(key);

            // If there are no values to set, skip it
            if (struct.values == null) {
                continue;
            }

            try {
                expressionEvaluator.setValue(key, action, struct.values, struct.attributes);
            } catch (ConversionException ce) {
                messageStore.addConversionError(key, action.getClass().getName(), struct.attributes, (Object[]) struct.values);
            } catch (ExpressionException ee) {
                // Ignore, these are optional
            }
        }

        // Next, process the required
        for (String key : params.required.keySet()) {
            Struct struct = params.required.get(key);

            // If there are no values to set, skip it
            if (struct.values == null) {
                continue;
            }

            try {
                expressionEvaluator.setValue(key, action, struct.values, struct.attributes);
            } catch (ConversionException ce) {
                messageStore.addConversionError(key, actionInvocation.actionURI(), struct.attributes, (Object[]) struct.values);
            } catch (ExpressionException ee) {
              if (!configuration.allowUnknownParameters()) {
                throw ee;
              }

                logger.log(Level.FINE, "Invalid parameter to action [" + action.getClass().getName() + "]", ee);
            }
        }
    }

    private void handleFiles(Map<String, List<FileInfo>> fileInfos, Object action, ActionInvocation actionInvocation) {
        // Set the files into the action
        for (String key : fileInfos.keySet()) {
            // Verify file sizes and types
            List<FileInfo> list = new ArrayList<FileInfo>(fileInfos.get(key));
            FileUpload fileUpload = expressionEvaluator.getAnnotation(FileUpload.class, key, action);
            for (Iterator<FileInfo> i = list.iterator(); i.hasNext();) {
                FileInfo info = i.next();
                if ((fileUpload != null && tooBig(info, fileUpload)) ||
                        ((fileUpload == null || fileUpload.maxSize() == -1) && tooBig(info))) {
                    messageStore.addFileUploadSizeError(key, actionInvocation.actionURI(), info.file.length());
                    i.remove();
                } else if ((fileUpload != null && invalidContentType(info, fileUpload)) ||
                        ((fileUpload == null || fileUpload.contentTypes().length == 0) && invalidContentType(info))) {
                    messageStore.addFileUploadContentTypeError(key, actionInvocation.actionURI(), info.getContentType());
                    i.remove();
                }
            }

            if (list.size() > 0) {
                // Set the files into the property
                expressionEvaluator.setValue(key, action, list);
            }
        }
    }

    private boolean empty(String[] values) {
        if (values != null && values.length > 0) {
            for (String value : values) {
                if (!value.equals("")) {
                    return false;
                }
            }
        }

        return true;
    }

    private void addUncheckedValues(Map<String, String[]> map, Parameters parameters) {
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

    /**
     * Checks the size of the given file against the annotation.
     *
     * @param   info The file info.
     * @param   fileUpload The annotation.
     * @return  False if the file is okay, true if it is too big.
     */
    private boolean tooBig(FileInfo info, FileUpload fileUpload) {
        return fileUpload.maxSize() != -1 && info.file.length() > fileUpload.maxSize();
    }

    /**
     * Checks the size of the given file against the global settings.
     *
     * @param   info The file info.
     * @return  False if the file is okay, true if it is too big.
     */
    private boolean tooBig(FileInfo info) {
        return info.file.length() > maxSize;
    }

    /**
     * Checks the content type of the given file against the annotation.
     *
     * @param   info The file info.
     * @param   fileUpload The annotation.
     * @return  False if the file is okay, true if it is an invalid type.
     */
    private boolean invalidContentType(FileInfo info, FileUpload fileUpload) {
        return fileUpload.contentTypes().length != 0 && !arrayContains(fileUpload.contentTypes(), info.contentType);
    }

    /**
     * Checks the content type of the global settings.
     *
     * @param   info The file info.
     * @return  False if the file is okay, true if it is an invalid type.
     */
    private boolean invalidContentType(FileInfo info) {
        return !arrayContains(contentTypes, info.contentType);
    }

    private class Parameters {
        private Map<String, Struct> required = new LinkedHashMap<String, Struct>();
        private Map<String, Struct> optional = new LinkedHashMap<String, Struct>();
    }

    private class Struct {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        String[] values;

        private Struct() {
        }

        private Struct(String[] values) {
            this.values = values;
        }
    }
}
