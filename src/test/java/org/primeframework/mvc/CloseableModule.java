/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.primeframework.mvc.guice.TestClosable;
import org.primeframework.mvc.guice.TestCloseableImplementation;
import org.primeframework.mvc.guice.TestCloseableInterface;

/**
 * Test module outside of a package named Guice to test explicit modules.
 *
 * @author Brian Pontarelli
 */
public class CloseableModule implements Module {
  public void configure(Binder binder) {
    binder.bind(TestClosable.class);
    binder.requestStaticInjection(TestClosable.class);
    binder.bind(TestCloseableInterface.class).to(TestCloseableImplementation.class);
  }
}
