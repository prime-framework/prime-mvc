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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.easymock.IArgumentMatcher;
import org.example.domain.Action;
import org.example.domain.PreAndPostAction;
import org.primeframework.mock.servlet.FileInfo;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.config.AbstractPrimeMVCConfiguration;
import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.util.MapBuilder;
import org.primeframework.mvc.util.RequestKeys;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This tests the default parameters workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultParameterWorkflowTest extends PrimeBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  @Test
  public void simpleParameters() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<String, String[]>();
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
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.addresses['home'].city"), same(action), aryEq(ArrayUtils.toArray("Boulder")), eq(new HashMap<String, String>()));
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(ArrayUtils.toArray("32")), eq(MapBuilder.asMap("dateFormat", "MM/dd/yyyy")));
    expressionEvaluator.setValue(eq("user.inches"), same(action), aryEq(ArrayUtils.toArray("tall")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ConversionException());
    expressionEvaluator.setValue(eq("user.name"), same(action), aryEq(ArrayUtils.toArray("")), eq(new HashMap<String, String>()));
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action).times(2);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation).times(2);
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(4);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    FieldMessage message = new SimpleFieldMessage("foo", "bar");
    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getFieldMessage("user.inches", "user.inches.conversionError", "tall")).andReturn(message);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request), 
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void invalidParametersDev() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<String, String[]>();
    values.put("user.age", ArrayUtils.toArray("32"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(ArrayUtils.toArray("32")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException());
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action).times(2);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation).times(2);
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    try {
      workflow.perform(chain);
      fail("Should have thrown an exception");
    } catch (ExpressionException ee) {
      // Expected
    }

    verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void invalidParametersProduction() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<String, String[]>();
    values.put("user.age", ArrayUtils.toArray("32"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(ArrayUtils.toArray("32")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException());
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action).times(2);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation).times(2);
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false);
    expect(config.allowUnknownParameters()).andReturn(true);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void radioButtonsCheckBoxes() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new HashMap<String, String[]>();
    values.put("__cb_user.checkbox['null']", ArrayUtils.toArray(""));
    values.put("__cb_user.checkbox['default']", ArrayUtils.toArray("false"));
    values.put("__rb_user.radio['null']", ArrayUtils.toArray(""));
    values.put("__rb_user.radio['default']", ArrayUtils.toArray("false"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createNiceMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.checkbox['default']"), same(action), aryEq(ArrayUtils.toArray("false")), eq(new HashMap<String, String>()));
    expressionEvaluator.setValue(eq("user.radio['default']"), same(action), aryEq(ArrayUtils.toArray("false")), eq(new HashMap<String, String>()));
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action).times(2);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation).times(2);
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void imageSubmitButton() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new HashMap<String, String[]>();
    values.put("__a_submit", ArrayUtils.toArray(""));
    values.put("submit.x", ArrayUtils.toArray("1"));
    values.put("submit.y", ArrayUtils.toArray("2"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ExpressionEvaluator expressionEvaluator = createNiceMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("submit.x"), same(action), aryEq(ArrayUtils.toArray("1")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException("Not property x"));
    expressionEvaluator.setValue(eq("submit.y"), same(action), aryEq(ArrayUtils.toArray("2")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException("Not property y"));
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action).times(2);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation).times(2);
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(2);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain, provider);
  }

  /*
  * Tests that all of the pre and post handling works correctly.
  */
  @Test
  public void preAndPost() throws IOException, ServletException {
    PreAndPostAction action = new PreAndPostAction();

    Map<String, String[]> values = new HashMap<String, String[]>();
    values.put("preField", ArrayUtils.toArray("1"));
    values.put("preProperty", ArrayUtils.toArray("Pre property"));
    values.put("notPre", ArrayUtils.toArray("Not pre"));

    final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(values);
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    replay(request);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action).times(2);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation).times(2);
    replay(actionInvocationStore);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.ignoreEmptyParameters()).andReturn(false).times(3);
    expect(config.allowUnknownParameters()).andReturn(false);
    replay(config);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    assertTrue(action.preCalled);
    assertTrue(action.postCalled);

    verify(request, invocation, actionInvocationStore, messageStore, config, chain, provider);
  }

  @Test
  public void filesNoAnnotation() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain")));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    replay(request);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(1024000l);
    expect(config.fileUploadAllowedTypes()).andReturn(new String[]{"text/plain"});
    replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(null);
    expressionEvaluator.setValue(eq("userfile"), same(action), assertFile());
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void multipleFilesNoAnnotation() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfiles", asList(
      new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"),
      new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload2.txt", "text/plain")));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    replay(request);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(1024000l);
    expect(config.fileUploadAllowedTypes()).andReturn(new String[]{"text/plain"});
    replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfiles", action)).andReturn(null);
    expressionEvaluator.setValue(eq("userfiles"), same(action), captureMultiple());
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    MessageProvider provider = createStrictMock(MessageProvider.class);
    replay(provider);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesNoAnnotationSizeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    replay(request);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(1l);
    expect(config.fileUploadAllowedTypes()).andReturn(AbstractPrimeMVCConfiguration.ALLOWED_TYPES);
    replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(null);
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    FieldMessage message = new SimpleFieldMessage("foo", "bar");
    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getFieldMessage("userfile", "userfile.fileUploadSizeError", 5l, 1l)).andReturn(message);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesNoAnnotationContentTypeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    replay(request);

    String[] contentTypes = new String[]{"test/xml"};
    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(10l);
    expect(config.fileUploadAllowedTypes()).andReturn(contentTypes);
    replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(null);
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    FieldMessage message = new SimpleFieldMessage("foo", "bar");
    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getFieldMessage("userfile", "userfile.fileUploadContentTypeError", "text/plain", contentTypes)).andReturn(message);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesAnnotationSizeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    replay(request);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(AbstractPrimeMVCConfiguration.MAX_SIZE);
    expect(config.fileUploadAllowedTypes()).andReturn(AbstractPrimeMVCConfiguration.ALLOWED_TYPES);
    replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(new FileUpload() {
      public long maxSize() {
        return 1;
      }

      public String[] contentTypes() {
        return new String[0];
      }

      public Class<? extends Annotation> annotationType() {
        return FileUpload.class;
      }
    });
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    FieldMessage message = new SimpleFieldMessage("foo", "bar");
    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getFieldMessage("userfile", "userfile.fileUploadSizeError", 5l, 1l)).andReturn(message);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore, provider);
  }

  @Test
  public void filesAnnotationContentTypeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getParameterMap()).andReturn(new HashMap());
    expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    replay(request);

    PrimeMVCConfiguration config = createStrictMock(PrimeMVCConfiguration.class);
    expect(config.allowUnknownParameters()).andReturn(false);
    expect(config.fileUploadMaxSize()).andReturn(AbstractPrimeMVCConfiguration.MAX_SIZE);
    expect(config.fileUploadAllowedTypes()).andReturn(AbstractPrimeMVCConfiguration.ALLOWED_TYPES);
    replay(config);

    final String[] annotationTypes = new String[]{"text/xml"};
    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(new FileUpload() {
      public long maxSize() {
        return 10;
      }

      public String[] contentTypes() {
        return annotationTypes;
      }

      public Class<? extends Annotation> annotationType() {
        return FileUpload.class;
      }
    });
    replay(expressionEvaluator);

    ActionInvocation invocation = createStrictMock(ActionInvocation.class);
    expect(invocation.action()).andReturn(action);
    replay(invocation);

    ActionInvocationStore actionInvocationStore = createStrictMock(ActionInvocationStore.class);
    expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    replay(actionInvocationStore);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    FieldMessage message = new SimpleFieldMessage("foo", "bar");
    MessageProvider provider = createStrictMock(MessageProvider.class);
    expect(provider.getFieldMessage("userfile", "userfile.fileUploadContentTypeError", "text/plain", annotationTypes)).andReturn(message);
    replay(provider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(message);
    replay(messageStore);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(
      new DefaultParameterParser(config, expressionEvaluator, actionInvocationStore, request),
      new DefaultParameterHandler(config, actionInvocationStore, expressionEvaluator, provider, messageStore));
    workflow.perform(chain);

    verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore, provider);
  }

  @SuppressWarnings("unchecked")
  public <T> T assertFile() {
    reportMatcher(new IArgumentMatcher() {
      public boolean matches(Object argument) {
        List<FileInfo> list = (List<FileInfo>) argument;
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertNotNull(list.get(0));
        assertEquals(list.get(0).contentType, "text/plain");
        assertEquals(list.get(0).name, "test-file-upload.txt");
        try {
          assertEquals(FileUtils.readFileToString(list.get(0).file), "1234\n");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return true;
      }

      public void appendTo(StringBuffer buffer) {
      }
    });

    return null;
  }

  @SuppressWarnings("unchecked")
  public <T> T captureMultiple() {
    reportMatcher(new IArgumentMatcher() {
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
          assertEquals(FileUtils.readFileToString(list.get(0).file), "1234\n");
          assertEquals(FileUtils.readFileToString(list.get(1).file), "1234\n");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return true;
      }

      public void appendTo(StringBuffer buffer) {
      }
    });

    return null;
  }
}
