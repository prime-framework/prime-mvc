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
package org.primeframework.mvc.freemarker;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ModelsHashModel;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.MissingPropertyExpressionException;

import com.google.inject.Inject;
import freemarker.ext.beans.CollectionModel;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * This class is a FreeMarker model that provides access in the templates to the request, session and context attributes
 * as well as values from the action.
 *
 * @author Brian Pontarelli
 */
public class ControllessFreeMarkerMap implements TemplateHashModelEx {
  public static final String REQUEST_MODEL = "Request";
  public static final String REQUEST = "request";
  public static final String SESSION_MODEL = "Session";
  public static final String SESSION = "session";
  public static final String APPLICATION_MODEL = "Application";
  public static final String APPLICATION = "application";
  public static final String JSP_TAGLIBS = "JspTaglibs";
  public static final String ALL_MESSAGES = "allMessages";
  public static final String FIELD_MESSAGES = "fieldMessages";
  public static final String ERROR_MESSAGES = "errorMessages";
  public static final String INFO_MESSAGES = "infoMessages";
  public static final String WARNING_MESSAGES = "warningMessages";

  protected final HttpServletRequest request;
  protected final ExpressionEvaluator expressionEvaluator;
  protected final ActionInvocationStore actionInvocationStore;
  protected final Map<String, Object> objects = new HashMap<String, Object>();
  protected final ServletContext context;
  protected final TaglibFactory taglibFactory;

  @Inject
  public ControllessFreeMarkerMap(ServletContext context, HttpServletRequest request, HttpServletResponse response,
                                  ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore,
                                  MessageStore messageStore, Map<String, Set<NamedTemplateModel>> models) {
    this.context = context;
    this.taglibFactory = new TaglibFactory(context);
    this.request = request;
    this.expressionEvaluator = expressionEvaluator;
    this.actionInvocationStore = actionInvocationStore;
    
    objects.put(REQUEST_MODEL, new HttpRequestHashModel(request, response, FieldSupportBeansWrapper.INSTANCE));
    objects.put(REQUEST, request);
    HttpSession session = request.getSession(false);
    if (session != null) {
      objects.put(SESSION_MODEL, new HttpSessionHashModel(session, FieldSupportBeansWrapper.INSTANCE));
      objects.put(SESSION, session);
    }

    objects.put(APPLICATION_MODEL, new ServletContextHashModel(new GenericServlet() {
      public void service(ServletRequest servletRequest, ServletResponse servletResponse) {
      }

      @Override
      public ServletConfig getServletConfig() {
        return this;
      }

      @Override
      public ServletContext getServletContext() {
        return ControllessFreeMarkerMap.this.context;
      }


    }, FieldSupportBeansWrapper.INSTANCE));
    objects.put(APPLICATION, context);
    objects.put(JSP_TAGLIBS, new TaglibFactory(context));

    List<Message> allMessages = messageStore.get();
    Map<String, List<FieldMessage>> fieldMessages = messageStore.getFieldMessages();
    List<Message> errorMessages = new ArrayList<Message>();
    List<Message> infoMessages = new ArrayList<Message>();
    List<Message> warningMessages = new ArrayList<Message>();

    for (Message message : allMessages) {
      if (!(message instanceof FieldMessage)) {
        if (message.getType() == MessageType.ERROR) {
          errorMessages.add(message);
        } else if (message.getType() == MessageType.INFO) {
          infoMessages.add(message);
        } else if (message.getType() == MessageType.WARNING) {
          warningMessages.add(message);
        }
      }
    }

    objects.put(ALL_MESSAGES, allMessages);
    objects.put(FIELD_MESSAGES, fieldMessages);
    objects.put(ERROR_MESSAGES, errorMessages);
    objects.put(INFO_MESSAGES, infoMessages);
    objects.put(WARNING_MESSAGES, warningMessages);

    // Add the models
    for (String prefix : models.keySet()) {
      objects.put(prefix, new ModelsHashModel(models.get(prefix)));
    }

    // TODO add debugging for figuring out what scope an object is in
  }

  @Override
  public int size() {
    int size = objects.size() + count(request.getAttributeNames());

    HttpSession session = request.getSession(false);
    if (session != null) {
      size += count(session.getAttributeNames());
    }

    size += count(context.getAttributeNames());

    Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
    if (actionInvocations != null) {
      for (ActionInvocation actionInvocation : actionInvocations) {
        if (actionInvocation.action != null) {
          size += expressionEvaluator.getAllMembers(actionInvocation.action.getClass()).size();
        }
      }
    }

    return size;
  }

  @Override
  public boolean isEmpty() {
    return size() > 0;
  }

  @Override
  public TemplateModel get(String key) {
    // First check the action
    Object value = null;

    Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
    if (actionInvocations != null) {
      for (ActionInvocation actionInvocation : actionInvocations) {
        if (actionInvocation.action != null) {
          try {
            value = expressionEvaluator.getValue(key, actionInvocation.action);
            if (value != null) {
              break;
            }
          } catch (MissingPropertyExpressionException e) {
            // Smother because the value is probably somewhere else
          }
        }
      }
    }

    // Next, check the objects
    if (value == null && objects.containsKey(key)) {
      value = objects.get(key);
    }

    // Next, check the request
    if (value == null) {
      value = request.getAttribute(key);
    }

    // Next, check the session
    if (value == null) {
      HttpSession session = request.getSession(false);
      if (session != null) {
        value = session.getAttribute(key);
      }
    }

    // Next, check the context
    if (value == null) {
      value = context.getAttribute(key);
    }

    try {
      return FieldSupportBeansWrapper.INSTANCE.wrap(value);
    } catch (TemplateModelException e) {
      throw new PrimeException(e);
    }
  }

  @Override
  public TemplateCollectionModel keys() {
    Set<String> keys = new HashSet<String>(objects.keySet());
    keys.addAll(enumToSet(request.getAttributeNames()));

    HttpSession session = request.getSession(false);
    if (session != null) {
      keys.addAll(enumToSet(session.getAttributeNames()));
    }

    keys.addAll(enumToSet(context.getAttributeNames()));

    Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
    if (actionInvocations != null) {
      for (ActionInvocation actionInvocation : actionInvocations) {
        if (actionInvocation.action != null) {
          keys.addAll(expressionEvaluator.getAllMembers(actionInvocation.action.getClass()));
        }
      }
    }

    keys.add(JSP_TAGLIBS);

    return new CollectionModel(keys, FieldSupportBeansWrapper.INSTANCE);
  }

  @Override
  public TemplateCollectionModel values() {
    Collection<Object> values = new ArrayList<Object>(objects.values());
    Deque<ActionInvocation> actionInvocations = actionInvocationStore.getDeque();
    if (actionInvocations != null) {
      for (ActionInvocation actionInvocation : actionInvocations) {
        if (actionInvocation.action != null) {
          values.addAll(expressionEvaluator.getAllMemberValues(actionInvocation.action));
        }
      }
    }

    Enumeration en = request.getAttributeNames();
    while (en.hasMoreElements()) {
      String name = (String) en.nextElement();
      values.add(request.getAttribute(name));
    }

    HttpSession session = request.getSession(false);
    if (session != null) {
      en = session.getAttributeNames();
      while (en.hasMoreElements()) {
        String name = (String) en.nextElement();
        values.add(session.getAttribute(name));
      }
    }

    en = context.getAttributeNames();
    while (en.hasMoreElements()) {
      String name = (String) en.nextElement();
      values.add(context.getAttribute(name));
    }

    values.add(taglibFactory);

    return new CollectionModel(values, FieldSupportBeansWrapper.INSTANCE);
  }

  private int count(Enumeration enumeration) {
    int count = 0;
    while (enumeration.hasMoreElements()) {
      count++;
      enumeration.nextElement();
    }

    return count;
  }

  private Set<String> enumToSet(Enumeration enumeration) {
    Set<String> set = new HashSet<String>();
    while (enumeration.hasMoreElements()) {
      set.add((String) enumeration.nextElement());
    }

    return set;
  }
}