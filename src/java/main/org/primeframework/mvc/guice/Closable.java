/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.guice;

/**
 * This interface defines a class that is Closable by JCatapult's Guice integration. If you mark a class with this
 * interface, JCatapult will attempt to close the class when the contain is shutdown. It is advised to only mark
 * singletons as Closable. Otherwise, JCatapult will end up creating a new instance and then calling close on it, which
 * could fail.
 * <p/>
 * In order to ensure that a class you have marked as Closable will be closed, you must provide a Guice binding for it.
 * Otherwise, Guice will not know anything about the class.
 * <p/>
 * To provide a Guice binding for your Closable, create a Guice module and put it in a package named
 * <strong>guice</strong> in your project. Inside this module provide a binding for your Closable like this:
 * <p/>
 * <pre>
 * bind(MyClosable.class);
 * </pre>
 * <p/>
 * This will allow your class to be Closed.
 *
 * @author Brian Pontarelli
 */
public interface Closable {
  /**
   * Invoked by JCatapult when the container is shutdown.
   */
  void close();
}
