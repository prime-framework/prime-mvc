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
package org.primeframework.mvc.parameter.guice;

import org.primeframework.mvc.parameter.DefaultParameterHandler;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.parameter.DefaultParameterWorkflow;
import org.primeframework.mvc.parameter.DefaultPostParameterHandler;
import org.primeframework.mvc.parameter.DefaultPostParameterWorkflow;
import org.primeframework.mvc.parameter.DefaultURIParameterWorkflow;
import org.primeframework.mvc.parameter.ParameterHandler;
import org.primeframework.mvc.parameter.ParameterParser;
import org.primeframework.mvc.parameter.ParameterWorkflow;
import org.primeframework.mvc.parameter.PostParameterHandler;
import org.primeframework.mvc.parameter.PostParameterWorkflow;
import org.primeframework.mvc.parameter.URIParameterWorkflow;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.DefaultConverterProvider;
import org.primeframework.mvc.parameter.el.DefaultExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Brian Pontarelli
 */
public class ParameterModule extends AbstractModule {
  @Override
  protected void configure() {
    bindParameterHandler();
    bindParameterParser();
    bindParameterWorkflow();
    bindPostParameterHandler();
    bindPostParameterWorkflow();
    bindURIParameterWorkflow();
    bindConverterProvider();
    bindExpressionEvaluator();
  }

  protected void bindPostParameterHandler() {
    bind(PostParameterHandler.class).to(DefaultPostParameterHandler.class);
  }

  protected void bindParameterHandler() {
    bind(ParameterHandler.class).to(DefaultParameterHandler.class);
  }

  protected void bindParameterParser() {
    bind(ParameterParser.class).to(DefaultParameterParser.class);
  }

  protected void bindParameterWorkflow() {
    bind(ParameterWorkflow.class).to(DefaultParameterWorkflow.class);
  }

  protected void bindPostParameterWorkflow() {
    bind(PostParameterWorkflow.class).to(DefaultPostParameterWorkflow.class);
  }

  protected void bindURIParameterWorkflow() {
    bind(URIParameterWorkflow.class).to(DefaultURIParameterWorkflow.class);
  }

  protected void bindConverterProvider() {
    bind(ConverterProvider.class).to(DefaultConverterProvider.class).in(Singleton.class);
  }

  protected void bindExpressionEvaluator() {
    bind(ExpressionEvaluator.class).to(DefaultExpressionEvaluator.class).in(Singleton.class);
  }
}
