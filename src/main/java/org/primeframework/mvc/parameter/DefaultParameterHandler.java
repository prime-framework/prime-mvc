/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.ParameterParser.Parameters;
import org.primeframework.mvc.parameter.ParameterParser.Parameters.Struct;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.util.ReflectionUtils;

import com.google.inject.Inject;
/**
 * This class is the default parameter handler. It sets all of the parameters into the action in the following order
 * (while invoking the correct methods in the order):
 *
 * <ol>
 *   <li>Set pre-parameters</li>
 *   <li>Invoke pre-parameters methods</li>
 *   <li>Set optional parameters</li>
 *   <li>Set required parameters</li>
 *   <li>Set files</li>
 *   <li>Invoke post-parameter methods</li>
 * </ol>
 *
 * @author Brian Pontarelli
 */
public class DefaultParameterHandler implements ParameterHandler {
  private final static Logger logger = Logger.getLogger(DefaultParameterHandler.class.getName());
  private final MVCConfiguration configuration;
  private final ActionInvocationStore actionInvocationStore;
  private final ExpressionEvaluator expressionEvaluator;
  private final MessageProvider messageProvider;
  private final MessageStore messageStore;

  @Inject
  public DefaultParameterHandler(MVCConfiguration configuration, ActionInvocationStore actionInvocationStore,
                                 ExpressionEvaluator expressionEvaluator, MessageProvider messageProvider,
                                 MessageStore messageStore) {
    this.configuration = configuration;
    this.actionInvocationStore = actionInvocationStore;
    this.expressionEvaluator = expressionEvaluator;
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  @Override
  public void handle(Parameters parameters) {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
    Object action = invocation.action();

    // First, process the pre-parameters
    setValues(parameters.pre, action, true);

    // Next, invoke pre methods
    ReflectionUtils.invokeAllWithAnnotation(action, PreParameterMethod.class);

    // Next, process the optional
    setValues(parameters.optional, action, true);

    // Next, process the required
    setValues(parameters.required, action, configuration.allowUnknownParameters());

    // Next, process the files
    if (parameters.files.size() > 0) {
      handleFiles(parameters.files, action);
    }

    // Finally, invoke post methods
    ReflectionUtils.invokeAllWithAnnotation(action, PostParameterMethod.class);
  }

  /**
   * Sets the given values into the action.
   *
   * @param values The value mapping.
   * @param action The action.
   * @param allowUnknownParameters Whether or not invalid parameters should throw an exception or just be ignored and
   *                               log a fine message.
   */
  protected void setValues(Map<String, Struct> values, Object action, boolean allowUnknownParameters) {
    for (String key : values.keySet()) {
      Struct struct = values.get(key);

      // If there are no values to set, skip it
      if (struct.values == null) {
        continue;
      }

      try {
        expressionEvaluator.setValue(key, action, struct.values, struct.attributes);
      } catch (ConversionException ce) {
        FieldMessage message = messageProvider.getFieldMessage(key, key + ".conversionError", (Object[]) struct.values);
        messageStore.add(message);
      } catch (ExpressionException ee) {
        if (!allowUnknownParameters) {
          throw ee;
        }

        logger.log(Level.FINE, "Invalid parameter to action [" + action.getClass().getName() + "]", ee);
      }
    }
  }

  /**
   * Sets the files into the action.
   *
   * @param fileInfos The file info.
   * @param action The action.
   */
  protected void handleFiles(Map<String, List<FileInfo>> fileInfos, Object action) {
    long maxSize = configuration.fileUploadMaxSize();
    String[] allowedContentTypes = configuration.fileUploadAllowedTypes();

    // Set the files into the action
    for (String key : fileInfos.keySet()) {
      // Verify file sizes and types
      List<FileInfo> list = new ArrayList<FileInfo>(fileInfos.get(key));
      FileUpload fileUpload = expressionEvaluator.getAnnotation(FileUpload.class, key, action);
      for (Iterator<FileInfo> i = list.iterator(); i.hasNext(); ) {
        FileInfo info = i.next();

        // Check the size
        if (fileUpload != null && fileUpload.maxSize() != -1) {
          maxSize = fileUpload.maxSize();
        }
        
        long fileSize = info.file.length();
        if (fileSize > maxSize) {
          FieldMessage message = messageProvider.getFieldMessage(key, key + ".fileUploadSizeError", fileSize, maxSize);
          messageStore.add(message);
          i.remove();
        }

        // Check the content type
        if (fileUpload != null && fileUpload.contentTypes().length > 0) {
          allowedContentTypes = fileUpload.contentTypes();
        }

        String contentType = info.contentType;
        if (!ArrayUtils.contains(allowedContentTypes, contentType)) {
          FieldMessage message = messageProvider.getFieldMessage(key, key + ".fileUploadContentTypeError", contentType, allowedContentTypes);
          messageStore.add(message);
          i.remove();
        }
      }

      if (list.size() > 0) {
        // Set the files into the property
        expressionEvaluator.setValue(key, action, list);
      }
    }
  }
}
