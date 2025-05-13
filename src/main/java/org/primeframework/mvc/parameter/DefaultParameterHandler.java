/*
 * Copyright (c) 2014-2025, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import io.fusionauth.http.FileInfo;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.PreParameterMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.parameter.ParameterParser.Parameters;
import org.primeframework.mvc.parameter.ParameterParser.Parameters.Struct;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.MultipleParametersUnsupportedException;
import org.primeframework.mvc.parameter.el.BeanExpressionException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.el.ReadExpressionException;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.util.ArrayBuilder;
import org.primeframework.mvc.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the default parameter handler. It sets all the parameters into the action in the following order (while invoking the correct methods
 * in the order):
 * <p>
 * <ol>
 * <li>Set pre-parameters</li>
 * <li>Invoke pre-parameters methods</li>
 * <li>Set optional parameters</li>
 * <li>Set required parameters</li>
 * <li>Set files</li>
 * </ol>
 * If the action has post-parameter methods, they are handled in a subsequent workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultParameterHandler implements ParameterHandler {
  private final static Logger logger = LoggerFactory.getLogger(DefaultParameterHandler.class);

  private final ActionInvocationStore actionInvocationStore;

  private final MVCConfiguration configuration;

  private final ExpressionEvaluator expressionEvaluator;

  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  private final HTTPRequest request;

  @Inject
  public DefaultParameterHandler(MVCConfiguration configuration, ActionInvocationStore actionInvocationStore,
                                 ExpressionEvaluator expressionEvaluator, MessageProvider messageProvider,
                                 MessageStore messageStore, HTTPRequest request) {
    this.configuration = configuration;
    this.actionInvocationStore = actionInvocationStore;
    this.expressionEvaluator = expressionEvaluator;
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
    this.request = request;
  }

  @Override
  public void handle(Parameters parameters) {
    if (logger.isDebugEnabled()) {
      logger.debug("Parameters found [{}] ", parameters);
    }

    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;

    // First, process the pre-parameters
    setValues(parameters.pre, actionInvocation, true);

    // Next, invoke pre methods
    ActionConfiguration actionConfiguration = actionInvocation.configuration;
    if (actionConfiguration.preParameterMethods.size() > 0) {
      HTTPMethod method = request.getMethod();
      if (actionConfiguration.preParameterMethods.containsKey(method)) {
        for (PreParameterMethodConfiguration methodConfig : actionConfiguration.preParameterMethods.get(method)) {
          ReflectionUtils.invoke(methodConfig.method, action);
        }
      }
    }

    // Next, process the optional
    setValues(parameters.optional, actionInvocation, true);

    // Next, process the required
    setValues(parameters.required, actionInvocation, configuration.allowUnknownParameters());

    // Next, process the files
    if (parameters.files.size() > 0) {
      handleFiles(parameters.files, actionConfiguration, action);
    }
  }

  /**
   * Sets the files into the action.
   *
   * @param fileInfos The file info.
   * @param action    The action.
   */
  protected void handleFiles(Map<String, List<FileInfo>> fileInfos, ActionConfiguration actionConfiguration,
                             Object action) {
    // Only set the files into the object if this is a multipart request
    if (!request.isMultipart()) {
      return;
    }

    long maxSize = configuration.fileUploadMaxSize();
    final Set<String> allowedContentTypes = configuration.fileUploadAllowedTypes();

    // Set the files into the action
    for (String key : fileInfos.keySet()) {
      // Verify file sizes and types
      List<FileInfo> list = new ArrayList<>(fileInfos.get(key));
      FileUpload fileUpload = actionConfiguration.fileUploadMembers.get(key);
      for (Iterator<FileInfo> i = list.iterator(); i.hasNext(); ) {
        FileInfo info = i.next();

        // Check the size
        if (fileUpload != null && fileUpload.maxSize() != -1) {
          maxSize = fileUpload.maxSize();
        }

        try {
          long fileSize = Files.size(info.file);
          if (fileSize > maxSize) {
            String code = "[fileUploadTooBig]" + key;
            String message = messageProvider.getMessage(code, key, fileSize, maxSize);
            messageStore.add(new SimpleFieldMessage(MessageType.ERROR, key, code, message));
            i.remove();
          }

          // Check the content type
          Set<String> contentTypesToCheck = allowedContentTypes;
          if (fileUpload != null && fileUpload.contentTypes().length > 0) {
            String[] override = fileUpload.contentTypes();
            if (override.length > 0) {
              contentTypesToCheck = Set.of(override);
            }
          }

          // If a single allowed content type of '*' is specified, skip this check, all Content Types are allowed.
          if (!contentTypesToCheck.contains("*")) {
            String contentType = info.contentType;
            if (!contentTypesToCheck.contains(contentType)) {
              String code = "[fileUploadBadContentType]" + key;
              String message = messageProvider.getMessage(code, key, contentType, contentTypesToCheck.toArray(new String[0]));
              messageStore.add(new SimpleFieldMessage(MessageType.ERROR, key, code, message));
              i.remove();
            }
          }
        } catch (IOException e) {
          String code = "[fileUploadFailed]" + key;
          String message = messageProvider.getMessage(code, key);
          messageStore.add(new SimpleFieldMessage(MessageType.ERROR, key, code, message));
        }
      }

      if (list.size() > 0) {
        // Set the files into the property
        expressionEvaluator.setValue(key, action, list);
      }
    }
  }

  /**
   * Sets the given values into the action.
   *
   * @param values                 The value mapping.
   * @param actionInvocation       The action invocation.
   * @param allowUnknownParameters Whether invalid parameters should throw an exception or just be ignored and log a fine message.
   */
  protected void setValues(Map<String, Struct> values, ActionInvocation actionInvocation,
                           boolean allowUnknownParameters) {
    Object action = actionInvocation.action;
    for (String key : values.keySet()) {
      Struct struct = values.get(key);

      // If there are no values to set, skip it
      if (struct.values == null) {
        continue;
      }

      try {
        expressionEvaluator.setValue(key, action, struct.values.toArray(new String[0]), struct.attributes);
      } catch (ConversionException ce) {
        addCouldNotConvertMessage(key, struct, ce);
      } catch (ConverterStateException cse) {
        // This type of exception is most likely the cause of a dev time issue, or a fuzzing attack.
        // - The assumption is that in a production runtime you want to allow unknown parameters. This isn't exactly an unknown parameter,
        //   but this is used fairly generically in parameter handling to be more or less verbose. We could consider adding another
        //   configuration parameters such as "ignoreConversionErrors" as these are almost always a dev time issue or a fuzzing result.
        //   In those cases, you want to fail fast in dev with exceptions, and at runtime ignore the fuzzing attacks.
        if (allowUnknownParameters) {
          logger.debug("Invalid parameter to action [{}]", action.getClass().getName(), cse);
        } else {
          throw cse;
        }
      } catch (BeanExpressionException ee) {
        throw ee;
      } catch (ExpressionException ee) {
        // If unknownParametersField is defined, then the allowUnknownParameters is ignored.
        if (actionInvocation.configuration.unknownParametersField != null) {
          captureUnknownParameter(key, struct, actionInvocation);
        } else if (allowUnknownParameters || actionInvocation.configuration.allowUnknownParameters) {
          logger.debug("Invalid parameter to action [" + action.getClass().getName() + "]", ee);
        } else {
          throw ee;
        }
      } catch (MultipleParametersUnsupportedException e) {
        // Re-throw after adding some meta-data about the current request to make it easier to debug in the log.
        // - Intentionally not recording the value to avoid logging anything sensitive.
        throw new MultipleParametersUnsupportedException(e.getMessage() +
                                                         " Action class [" + action.getClass().getName() + "] Request URI [" + actionInvocation.actionURI + "] Parameter name [" + key + "]");
      }
    }
  }

  private void addCouldNotConvertMessage(String key, Struct struct, ConversionException ce) {
    String code = "[couldNotConvert]" + key;
    try {
      String message = messageProvider.getMessage(code, (Object[]) new ArrayBuilder<>(String.class, key).addAll(struct.values).done());
      messageStore.add(new SimpleFieldMessage(MessageType.ERROR, key, code, message));
    } catch (MissingMessageException mme) {
      // Retry if the message key is using a bracketed syntax for a map or an array
      // For example: foo['bar'] or foo[0]
      String part = key.replaceAll("\\[.+?]$", "");
      if (!part.equals(key)) {
        try {
          String modifiedCode = "[couldNotConvert]" + part + "[]";
          String message = messageProvider.getMessage(modifiedCode, (Object[]) new ArrayBuilder<>(String.class, key).addAll(struct.values).done());
          messageStore.add(new SimpleFieldMessage(MessageType.ERROR, key, modifiedCode, message));
          return;
        } catch (MissingMessageException ignore) {
        }
      }

      messageStore.add(new SimpleFieldMessage(MessageType.ERROR, key, code, ce.getMessage()));
    }
  }

  private void captureUnknownParameter(String key, Struct struct, ActionInvocation actionInvocation) {
    Field field = actionInvocation.configuration.unknownParametersField;
    try {
      @SuppressWarnings("unchecked")
      Map<String, String[]> unknownParameters = (Map<String, String[]>) field.get(actionInvocation.action);
      unknownParameters.put(key, struct.values.toArray(new String[0]));
    } catch (IllegalAccessException e) {
      throw new ReadExpressionException("Illegal access for field [" + field + "]", e);
    }
  }
}
