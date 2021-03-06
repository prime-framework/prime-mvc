/*
 * Copyright (c) 2014-2018, Inversoft Inc., All Rights Reserved
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
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.easymock.IArgumentMatcher;
import org.example.action.user.EditAction;
import org.example.domain.Action;
import org.example.domain.ActionField;
import org.example.domain.PreAndPostAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.config.AbstractMVCConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.MapBuilder;
import org.primeframework.mvc.util.RequestKeys;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * This tests the default parameters workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultParameterWorkflowTest extends PrimeBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  @SuppressWarnings("unchecked")
  public <T> T assertFile() {
    reportMatcher(new IArgumentMatcher() {
      public void appendTo(StringBuffer buffer) {
      }

      public boolean matches(Object argument) {
        List<FileInfo> list = (List<FileInfo>) argument;
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertNotNull(list.get(0));
        assertEquals(list.get(0).contentType, "text/plain");
        assertEquals(list.get(0).name, "test-file-upload.txt");
        try {
          assertEquals(FileUtils.readFileToString(list.get(0).file, StandardCharsets.UTF_8), "1234\n");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return true;
      }
    });

    return null;
  }

  @SuppressWarnings("unchecked")
  public <T> T captureMultiple() {
    reportMatcher(new IArgumentMatcher() {
      public void appendTo(StringBuffer buffer) {
      }

      public boolean matches(Object argument) {
        List<FileInfo> list = (List<FileInfo>) argument;
        assertNotNull(list);
        assertEquals(list.size(), 2);
        assertNotNull(list.get(0));
        assertNotNull(list.get(1));
        assertEquals(list.get(0).contentType, "text/plain");
        assertEquals(list.get(1).contentType, "text/plain");
        assertEquals(list.get(0).name, "test-file-upload.txt");
        assertEquals(list.get(1).name, "test-file-upload2.txt");
        try {
          assertEquals(FileUtils.readFileToString(list.get(0).file, StandardCharsets.UTF_8), "1234\n");
          assertEquals(FileUtils.readFileToString(list.get(1).file, StandardCharsets.UTF_8), "1234\n");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return true;
      }
    });

    return null;
  }

  @Test
  public void filesAnnotationContentTypeError() throws Exception {
    Map<String, List<FileInfo>> files = new HashMap<>();
    files.put("userfile", new ArrayList<>(asList(new FileInfo(new java.io.File("src/test/java/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(AbstractMVCConfiguration.MAX_SIZE);
    expect(config.fileUploadAllowedTypes()).andReturn(AbstractMVCConfiguration.ALLOWED_TYPES);
    replay(config);

    final String[] annotationTypes = new String[]{"text/xml"};
    EditAction action = new EditAction();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ai.configuration.fileUploadMembers.put("userfile", new FileUpload() {
      public Class<? extends Annotation> annotationType() {
        return FileUpload.class;
      }

      public String[] contentTypes() {
        return annotationTypes;
      }

      public long maxSize() {
        return 10;
      }
    });
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getMessage("[fileUploadBadContentType]userfile", "userfile", "text/plain", annotationTypes)).andReturn("bar");
    replay(provider);

    FieldMessage message = new SimpleFieldMessage(MessageType.ERROR, "userfile", "[fileUploadBadContentType]userfile", "bar");
    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesAnnotationSizeError() throws Exception {
    Map<String, List<FileInfo>> files = new HashMap<>();
    files.put("userfile", new ArrayList<>(asList(new FileInfo(new java.io.File("src/test/java/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(AbstractMVCConfiguration.MAX_SIZE);
    expect(config.fileUploadAllowedTypes()).andReturn(AbstractMVCConfiguration.ALLOWED_TYPES);
    replay(config);

    EditAction action = new EditAction();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ai.configuration.fileUploadMembers.put("userfile", new FileUpload() {
      public Class<? extends Annotation> annotationType() {
        return FileUpload.class;
      }

      public String[] contentTypes() {
        return new String[0];
      }

      public long maxSize() {
        return 1;
      }
    });
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getMessage("[fileUploadTooBig]userfile", "userfile", 5l, 1l)).andReturn("bar");
    replay(provider);

    FieldMessage message = new SimpleFieldMessage(MessageType.ERROR, "userfile", "[fileUploadTooBig]userfile", "bar");
    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesNoAnnotation() throws Exception {
    Map<String, List<FileInfo>> files = new HashMap<>();
    files.put("userfile", asList(new FileInfo(new java.io.File("src/test/java/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain")));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(1024000l);
    expect(config.fileUploadAllowedTypes()).andReturn(new String[]{"text/plain"});
    replay(config);

    EditAction action = new EditAction();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expressionEvaluator.setValue(eq("userfile"), same(action), assertFile());
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesNoAnnotationContentTypeError() throws Exception {
    Map<String, List<FileInfo>> files = new HashMap<>();
    files.put("userfile", new ArrayList<>(asList(new FileInfo(new java.io.File("src/test/java/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    String[] contentTypes = new String[]{"test/xml"};
    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(10l);
    expect(config.fileUploadAllowedTypes()).andReturn(contentTypes);
    replay(config);

    EditAction action = new EditAction();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getMessage("[fileUploadBadContentType]userfile", "userfile", "text/plain", contentTypes)).andReturn("bar");
    replay(provider);

    FieldMessage message = new SimpleFieldMessage(MessageType.ERROR, "userfile", "[fileUploadBadContentType]userfile", "bar");
    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesNoAnnotationSizeError() throws Exception {
    Map<String, List<FileInfo>> files = new HashMap<>();
    files.put("userfile", new ArrayList<>(asList(new FileInfo(new java.io.File("src/test/java/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(1l);
    expect(config.fileUploadAllowedTypes()).andReturn(AbstractMVCConfiguration.ALLOWED_TYPES);
    replay(config);

    EditAction action = new EditAction();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getMessage("[fileUploadTooBig]userfile", "userfile", 5l, 1l)).andReturn("bar");
    replay(provider);

    FieldMessage message = new SimpleFieldMessage(MessageType.ERROR, "userfile", "[fileUploadTooBig]userfile", "bar");
    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void imageSubmitButton() throws Exception {
    Action action = new Action();

    Map<String, String[]> values = new HashMap<>();
    values.put("submit.x", ArrayUtils.toArray("1"));
    values.put("submit.y", ArrayUtils.toArray("2"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createNiceMock(ExpressionEvaluator.class);
    expressionEvaluator.setValue(eq("submit.x"), same(action), aryEq(ArrayUtils.toArray("1")), eq(new HashMap<>()));
    expectLastCall().andThrow(new ExpressionException("Not property x"));
    expressionEvaluator.setValue(eq("submit.y"), same(action), aryEq(ArrayUtils.toArray("2")), eq(new HashMap<>()));
    expectLastCall().andThrow(new ExpressionException("Not property y"));
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(2);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, expressionEvaluator, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void invalidParametersDev() throws Exception {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<>();
    values.put("user.age", ArrayUtils.toArray("32"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(ArrayUtils.toArray("32")), eq(new HashMap<>()));
    expectLastCall().andThrow(new ExpressionException());
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    try {
      workflow.perform(chain);
      fail("Should have thrown an exception");
    } catch (ExpressionException ee) {
      // Expected
    }

    verify(request, expressionEvaluator, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void invalidParametersProduction() throws Exception {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<>();
    values.put("user.age", ArrayUtils.toArray("32"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(ArrayUtils.toArray("32")), eq(new HashMap<>()));
    expectLastCall().andThrow(new ExpressionException());
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false);
    expect(config.allowUnknownParameters()).andReturn(true);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, expressionEvaluator, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void multipleFilesNoAnnotation() throws Exception {
    Map<String, List<FileInfo>> files = new HashMap<>();
    files.put("userfiles", asList(
        new FileInfo(new java.io.File("src/test/java/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"),
        new FileInfo(new java.io.File("src/test/java/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload2.txt", "text/plain")));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap<>());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(1024000l);
    expect(config.fileUploadAllowedTypes()).andReturn(new String[]{"text/plain"});
    replay(config);

    EditAction action = new EditAction();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expressionEvaluator.setValue(eq("userfiles"), same(action), captureMultiple());
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, expressionEvaluator, messageStore, provider);
  }

  /*
   * Tests that all of the pre and post handling works correctly.
   */
  @Test
  public void preAndPost() throws Exception {
    PreAndPostAction action = new PreAndPostAction();

    Map<String, String[]> values = new HashMap<>();
    values.put("preField", ArrayUtils.toArray("1"));
    values.put("preProperty", ArrayUtils.toArray("Pre property"));
    values.put("notPre", ArrayUtils.toArray("Not pre"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    expectLastCall().times(2);
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(3);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    assertTrue(action.preCalled);
    assertFalse(action.postCalled);

    PostParameterHandler postParameterHandler = new DefaultPostParameterHandler();
    PostParameterWorkflow postParameterWorkflow = new DefaultPostParameterWorkflow(actionInvocationStore, postParameterHandler);
    postParameterWorkflow.perform(chain);

    assertTrue(action.postCalled);

    verify(request, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void radioButtonsCheckBoxes() throws Exception {
    Action action = new Action();

    Map<String, String[]> values = new HashMap<>();
    values.put("__cb_user.checkbox['null']", ArrayUtils.toArray(""));
    values.put("__cb_user.checkbox['default']", ArrayUtils.toArray("false"));
    values.put("__rb_user.radio['null']", ArrayUtils.toArray(""));
    values.put("__rb_user.radio['default']", ArrayUtils.toArray("false"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createNiceMock(ExpressionEvaluator.class);
    expressionEvaluator.setValue(eq("user.checkbox['default']"), same(action), aryEq(ArrayUtils.toArray("false")), eq(new HashMap<>()));
    expressionEvaluator.setValue(eq("user.radio['default']"), same(action), aryEq(ArrayUtils.toArray("false")), eq(new HashMap<>()));
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, expressionEvaluator, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void simpleParameter_boolean() throws Exception {
    ActionField action = new ActionField();

    Map<String, String[]> values = new LinkedHashMap<>();
    values.put("foo", ArrayUtils.toArray("true"));
    values.put("bar", ArrayUtils.toArray("false"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(2);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, actionInvocationStore, messageStore, config, chain, provider);
    assertTrue(action.foo);
    assertFalse(action.bar);

  }

  @Test
  public void simpleParameter_boolean_default() throws Exception {
    ActionField action = new ActionField();

    Map<String, String[]> values = new LinkedHashMap<>();

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, actionInvocationStore, messageStore, config, chain, provider);
    assertFalse(action.foo);
    assertFalse(action.bar);
  }

  @Test
  public void simpleParameter_boolean_in_baseClass() throws Exception {
    ActionField action = new ActionField();

    Map<String, String[]> values = new LinkedHashMap<>();
    values.put("superFoo", ArrayUtils.toArray("true"));
    values.put("superBar", ArrayUtils.toArray("false"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(2);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, actionInvocationStore, messageStore, config, chain, provider);
    assertTrue(action.superFoo);
    assertFalse(action.superBar);
  }

  @Test
  public void simpleParameter_boolean_model_object() throws Exception {
    ActionField action = new ActionField();

    Map<String, String[]> values = new LinkedHashMap<>();
    values.put("user.active", ArrayUtils.toArray("true"));
    values.put("user.bar", ArrayUtils.toArray("false"));


    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(2);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, actionInvocationStore, messageStore, config, chain, provider);
    assertTrue(action.user.active);
    assertFalse(action.user.bar);
  }

  @Test
  public void simpleParameters() throws Exception {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<>();
    values.put("user.addresses['home'].city", ArrayUtils.toArray("Boulder"));
    values.put("user.age", ArrayUtils.toArray("32"));
    values.put("user.age@dateFormat", ArrayUtils.toArray("MM/dd/yyyy"));
    values.put("user.inches", ArrayUtils.toArray("tall"));
    values.put("user.name", ArrayUtils.toArray(""));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expressionEvaluator.setValue(eq("user.addresses['home'].city"), same(action), aryEq(ArrayUtils.toArray("Boulder")), eq(new HashMap<>()));
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(ArrayUtils.toArray("32")), eq(MapBuilder.asMap("dateFormat", "MM/dd/yyyy")));
    expressionEvaluator.setValue(eq("user.inches"), same(action), aryEq(ArrayUtils.toArray("tall")), eq(new HashMap<>()));
    expectLastCall().andThrow(new ConversionException());
    expressionEvaluator.setValue(eq("user.name"), same(action), aryEq(ArrayUtils.toArray("")), eq(new HashMap<>()));
    replay(expressionEvaluator);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "");
    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(ai).anyTimes();
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MVCConfiguration config = createStrictMock(MVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(4);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getMessage("[couldNotConvert]user.inches", "user.inches", "tall")).andReturn("bar");
    replay(provider);

    FieldMessage message = new SimpleFieldMessage(MessageType.ERROR, "user.inches", "[couldNotConvert]user.inches", "bar");
    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
        actionInvocationStore, new DefaultParameterParser(config, actionInvocationStore, request),
        new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore, request));
    workflow.perform(chain);

    verify(request, expressionEvaluator, actionInvocationStore, messageStore, config, chain, provider);
  }
}
