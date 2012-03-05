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
import java.util.logging.Logger;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.example.domain.Action;
import org.example.domain.PreAndPostAction;
import org.primeframework.mock.servlet.FileInfo;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.primeframework.mvc.test.JCatapultBaseTest;
import org.primeframework.mvc.util.RequestKeys;
import org.testng.annotations.Test;

import net.java.io.FileTools;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import static net.java.util.CollectionTools.*;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * <p> This tests the default parameters workflow. </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings({"ALL"})
public class DefaultParameterWorkflowTest extends JCatapultBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  /*
  * Tests the workflow method.
  */
  @Test
  public void simpleParameters() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<String, String[]>();
    values.put("user.addresses['home'].city", array("Boulder"));
    values.put("user.age", array("32"));
    values.put("user.age@dateFormat", array("MM/dd/yyyy"));
    values.put("user.inches", array("tall"));
    values.put("user.name", array(""));

    final HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(values);
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    EasyMock.replay(request);

    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.addresses['home'].city"), same(action), aryEq(array("Boulder")), eq(new HashMap<String, String>()));
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(array("32")), eq(map("dateFormat", "MM/dd/yyyy")));
    expressionEvaluator.setValue(eq("user.inches"), same(action), aryEq(array("tall")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ConversionException());
    expressionEvaluator.setValue(eq("user.name"), same(action), aryEq(array("")), eq(new HashMap<String, String>()));
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.expect(invocation.actionURI()).andReturn("/test");
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    messageStore.addConversionError(eq("user.inches"), eq("/test"), eq(new HashMap<String, String>()), eq("tall"));
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(10l);
    EasyMock.expect(config.ignoreEmptyParameters()).andReturn(false).times(4);
    EasyMock.replay(config);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain);
  }

  /*
  * Tests the invalid parameters in development mode.
  */
  @Test
  public void invalidParametersDev() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<String, String[]>();
    values.put("user.age", array("32"));

    final HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(values);
    EasyMock.replay(request);

    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(array("32")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException());
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(chain);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(10l);
    EasyMock.expect(config.ignoreEmptyParameters()).andReturn(false);
    EasyMock.expect(config.allowUnknownParameters()).andReturn(false);
    EasyMock.replay(config);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.logger = Logger.getLogger("test");
    try {
      workflow.perform(chain);
      fail("Should have thrown an exception");
    } catch (ExpressionException ee) {
      // Expected
    }

    EasyMock.verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain);
  }

  /*
  * Tests the invalid parameters in production mode.
  */
  @Test
  public void invalidParametersProduction() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new LinkedHashMap<String, String[]>();
    values.put("user.age", array("32"));

    final HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(values);
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    EasyMock.replay(request);

    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.age"), same(action), aryEq(array("32")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException());
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(10l);
    EasyMock.expect(config.ignoreEmptyParameters()).andReturn(false);
    EasyMock.expect(config.allowUnknownParameters()).andReturn(true);
    EasyMock.replay(config);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.logger = Logger.getLogger("test");
    workflow.perform(chain);

    EasyMock.verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain);
  }

  /*
  * Tests radio buttons and checkboxes.
  */
  @Test
  public void radioButtonsCheckBoxes() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new HashMap<String, String[]>();
    values.put("__cb_user.checkbox['null']", array(""));
    values.put("__cb_user.checkbox['default']", array("false"));
    values.put("__rb_user.radio['null']", array(""));
    values.put("__rb_user.radio['default']", array("false"));

    final HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(values);
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    EasyMock.replay(request);

    ExpressionEvaluator expressionEvaluator = EasyMock.createNiceMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("user.checkbox['default']"), same(action), aryEq(array("false")), eq(new HashMap<String, String>()));
    expressionEvaluator.setValue(eq("user.radio['default']"), same(action), aryEq(array("false")), eq(new HashMap<String, String>()));
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(10l);
    EasyMock.replay(config);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain);
  }

  /*
  * Tests image submit button which will try to set the x and y values into the action, but they
  * should be optional and therefore throw exceptions that are ignored.
  */
  @Test
  public void imageSubmitButton() throws IOException, ServletException {
    Action action = new Action();

    Map<String, String[]> values = new HashMap<String, String[]>();
    values.put("__a_submit", array(""));
    values.put("submit.x", array("1"));
    values.put("submit.y", array("2"));

    final HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(values);
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    EasyMock.replay(request);

    ExpressionEvaluator expressionEvaluator = EasyMock.createNiceMock(ExpressionEvaluator.class);
    expect(expressionEvaluator.getAllMembers(action.getClass())).andReturn(new HashSet<String>());
    expressionEvaluator.setValue(eq("submit.x"), same(action), aryEq(array("1")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException("Not property x"));
    expressionEvaluator.setValue(eq("submit.y"), same(action), aryEq(array("2")), eq(new HashMap<String, String>()));
    expectLastCall().andThrow(new ExpressionException("Not property y"));
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(10l);
    EasyMock.expect(config.ignoreEmptyParameters()).andReturn(false).times(2);
    EasyMock.replay(config);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, expressionEvaluator, invocation, actionInvocationStore, messageStore, config, chain);
  }

  /*
  * Tests that all of the pre and post handling works correctly.
  */
  @Test
  public void preAndPost() throws IOException, ServletException {
    PreAndPostAction action = new PreAndPostAction();

    Map<String, String[]> values = new HashMap<String, String[]>();
    values.put("preField", array("1"));
    values.put("preProperty", array("Pre property"));
    values.put("notPre", array("Not pre"));

    final HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(values);
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(null);
    EasyMock.replay(request);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(10l);
    EasyMock.expect(config.ignoreEmptyParameters()).andReturn(false).times(3);
    EasyMock.replay(config);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    assertTrue(action.preCalled);
    assertTrue(action.postCalled);

    EasyMock.verify(request, invocation, actionInvocationStore, messageStore, config, chain);
  }

  @Test
  public void filesNoAnnotation() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain")));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    EasyMock.replay(request);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[]{"text/plain"});
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(1024000l);
    EasyMock.replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(null);
    expressionEvaluator.setValue(eq("userfile"), same(action), capture());
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, null, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator);
  }

  @Test
  public void multipleFilesNoAnnotation() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfiles", asList(
      new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"),
      new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload2.txt", "text/plain")));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    EasyMock.replay(request);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[]{"text/plain"});
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(1024000l);
    EasyMock.replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfiles", action)).andReturn(null);
    expressionEvaluator.setValue(eq("userfiles"), same(action), captureMultiple());
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, null, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator);
  }

  @Test
  public void filesNoAnnotationSizeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    EasyMock.replay(request);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(1l);
    EasyMock.replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(null);
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.expect(invocation.actionURI()).andReturn("/test");
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    messageStore.addFileUploadSizeError("userfile", "/test", 5);
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore);
  }

  @Test
  public void filesNoAnnotationContentTypeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    EasyMock.replay(request);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[]{"test/xml"});
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(10l);
    EasyMock.replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(null);
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.expect(invocation.actionURI()).andReturn("/test");
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    messageStore.addFileUploadContentTypeError("userfile", "/test", "text/plain");
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore);
  }

  @Test
  public void filesAnnotationSizeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    EasyMock.replay(request);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(1024000l);
    EasyMock.replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(new FileUpload() {
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
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.expect(invocation.actionURI()).andReturn("/test");
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    messageStore.addFileUploadSizeError("userfile", "/test", 5);
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore);
  }

  @Test
  public void filesAnnotationContentTypeError() throws IOException, ServletException {
    Map<String, List<FileInfo>> files = new HashMap<String, List<FileInfo>>();
    files.put("userfile", new ArrayList<FileInfo>(asList(new FileInfo(new java.io.File("src/java/test/unit/org/primeframework/mvc/parameter/test-file-upload.txt"), "test-file-upload.txt", "text/plain"))));

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getParameterMap()).andReturn(new HashMap());
    EasyMock.expect(request.getAttribute(RequestKeys.FILE_ATTRIBUTE)).andReturn(files);
    EasyMock.replay(request);

    PrimeMVCConfiguration config = EasyMock.createStrictMock(PrimeMVCConfiguration.class);
    EasyMock.expect(config.fileUploadAllowedTypes()).andReturn(new String[0]);
    EasyMock.expect(config.fileUploadMaxSize()).andReturn(1024000l);
    EasyMock.replay(config);

    Object action = new Object();
    ExpressionEvaluator expressionEvaluator = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(expressionEvaluator.getAnnotation(FileUpload.class, "userfile", action)).andReturn(new FileUpload() {
      public long maxSize() {
        return 10;
      }

      public String[] contentTypes() {
        return new String[]{"text/xml"};
      }

      public Class<? extends Annotation> annotationType() {
        return FileUpload.class;
      }
    });
    EasyMock.replay(expressionEvaluator);

    ActionInvocation invocation = EasyMock.createStrictMock(ActionInvocation.class);
    EasyMock.expect(invocation.action()).andReturn(action);
    EasyMock.expect(invocation.actionURI()).andReturn("/test");
    EasyMock.replay(invocation);

    ActionInvocationStore actionInvocationStore = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(actionInvocationStore.getCurrent()).andReturn(invocation);
    EasyMock.replay(actionInvocationStore);

    MessageStore messageStore = EasyMock.createStrictMock(MessageStore.class);
    messageStore.addFileUploadContentTypeError("userfile", "/test", "text/plain");
    EasyMock.replay(messageStore);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultParameterWorkflow workflow = new DefaultParameterWorkflow(request, actionInvocationStore, messageStore, messageProvider, expressionEvaluator, config);
    workflow.perform(chain);

    EasyMock.verify(request, config, chain, actionInvocationStore, invocation, expressionEvaluator, messageStore);
  }

  @SuppressWarnings("unchecked")
  public <T> T capture() {
    reportMatcher(new IArgumentMatcher() {
      public boolean matches(Object argument) {
        List<FileInfo> list = (List<FileInfo>) argument;
        assertNotNull(list);
        assertEquals(1, list.size());
        assertNotNull(list.get(0));
        assertEquals("text/plain", list.get(0).contentType);
        assertEquals("test-file-upload.txt", list.get(0).name);
        try {
          assertEquals("1234\n", FileTools.read(list.get(0).file).toString());
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
        assertEquals(2, list.size());
        assertNotNull(list.get(0));
        assertNotNull(list.get(1));
        assertEquals("text/plain", list.get(0).contentType);
        assertEquals("text/plain", list.get(1).contentType);
        assertEquals("test-file-upload.txt", list.get(0).name);
        assertEquals("test-file-upload2.txt", list.get(1).name);
        try {
          assertEquals("1234\n", FileTools.read(list.get(0).file).toString());
          assertEquals("1234\n", FileTools.read(list.get(1).file).toString());
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
