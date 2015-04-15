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
package org.primeframework.mvc.action.result.guice;

import com.google.inject.AbstractModule;
import org.primeframework.mvc.action.result.ForwardResult;
import org.primeframework.mvc.action.result.JSONResult;
import org.primeframework.mvc.action.result.NoOpResult;
import org.primeframework.mvc.action.result.RedirectResult;
import org.primeframework.mvc.action.result.ResultFactory;
import org.primeframework.mvc.action.result.ReexecuteSavedRequestResult;
import org.primeframework.mvc.action.result.SaveRequestResult;
import org.primeframework.mvc.action.result.StatusResult;
import org.primeframework.mvc.action.result.StreamResult;
import org.primeframework.mvc.action.result.XMLStreamResult;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.NoOp;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Stream;
import org.primeframework.mvc.action.result.annotation.XMLStream;

/**
 * Binds results.
 *
 * @author Brian Pontarelli
 */
public class ResultModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ResultFactory.class);
    ResultFactory.addResult(binder(), Forward.class, ForwardResult.class);
    ResultFactory.addResult(binder(), Status.class, StatusResult.class);
    ResultFactory.addResult(binder(), NoOp.class, NoOpResult.class);
    ResultFactory.addResult(binder(), Redirect.class, RedirectResult.class);
    ResultFactory.addResult(binder(), ReexecuteSavedRequest.class, ReexecuteSavedRequestResult.class);
    ResultFactory.addResult(binder(), SaveRequest.class, SaveRequestResult.class);
    ResultFactory.addResult(binder(), Stream.class, StreamResult.class);
    ResultFactory.addResult(binder(), XMLStream.class, XMLStreamResult.class);
    ResultFactory.addResult(binder(), JSON.class, JSONResult.class);
  }
}
