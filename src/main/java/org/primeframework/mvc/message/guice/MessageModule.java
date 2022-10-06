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
package org.primeframework.mvc.message.guice;

import java.util.ResourceBundle;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.primeframework.mvc.message.DefaultMessageStore;
import org.primeframework.mvc.message.DefaultMessageWorkflow;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageWorkflow;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.ResourceBundleMessageProvider;
import org.primeframework.mvc.message.l10n.WebControl;

/**
 * This class is a Guice module for the Prime MVC message and l10n handling.
 *
 * @author Brian Pontarelli
 */
public class MessageModule extends AbstractModule {
  protected void bindMessageProvider() {
    bind(MessageProvider.class).to(ResourceBundleMessageProvider.class);
  }

  protected void bindMessageStore() {
    bind(MessageStore.class).to(DefaultMessageStore.class);
  }

  protected void bindMessageWorkflow() {
    bind(MessageWorkflow.class).to(DefaultMessageWorkflow.class);
  }

  @Override
  protected void configure() {
    bind(ResourceBundle.Control.class).to(WebControl.class).in(Singleton.class);

    bindMessageStore();
    bindMessageWorkflow();
    bindMessageProvider();
  }
}
