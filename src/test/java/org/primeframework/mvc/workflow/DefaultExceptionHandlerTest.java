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
package org.primeframework.mvc.workflow;

import java.util.HashMap;
import java.util.Map;

import org.primeframework.mvc.ErrorException;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * @author James Humphrey
 */
public class DefaultExceptionHandlerTest {

  @Test
  public void handleRuntimeException() {
    DefaultExceptionHandler handler = new DefaultExceptionHandler(null);

    try {
      handler.handle(new RuntimeException());
      fail("Should have thrown a RuntimeException");
    } catch (RuntimeException e) {
      // no op, test successful
    }
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void testErrorException() {

    ErrorExceptionHandler errorExceptionHandler = createStrictMock(ErrorExceptionHandler.class);
    errorExceptionHandler.handle(isA(ErrorException.class));
    replay(errorExceptionHandler);

    Map<Class<?>, TypedExceptionHandler<?>> handlers = new HashMap<Class<?>, TypedExceptionHandler<?>>();
    handlers.put(ErrorException.class, errorExceptionHandler);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(handlers);

    try {
      handler.handle(new ErrorException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void testFooException() {

    ErrorExceptionHandler errorExceptionHandler = createStrictMock(ErrorExceptionHandler.class);
    errorExceptionHandler.handle(isA(FooException.class));
    replay(errorExceptionHandler);

    Map<Class<?>, TypedExceptionHandler<?>> handlers = new HashMap<Class<?>, TypedExceptionHandler<?>>();
    handlers.put(FooException.class, errorExceptionHandler);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(handlers);

    try {
      handler.handle(new FooException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void testBarException() {

    BarExceptionHandler<BarException> errorExceptionHandler = createStrictMock(BarExceptionHandler.class);
    errorExceptionHandler.handle(isA(BarException.class));
    replay(errorExceptionHandler);

    Map<Class<?>, TypedExceptionHandler<?>> handlers = new HashMap<Class<?>, TypedExceptionHandler<?>>();
    handlers.put(BarException.class, errorExceptionHandler);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(handlers);

    try {
      handler.handle(new BarException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void testBazException() {

    BarExceptionHandler<BazException> errorExceptionHandler = createStrictMock(BarExceptionHandler.class);
    errorExceptionHandler.handle(isA(BazException.class));
    replay(errorExceptionHandler);

    Map<Class<?>, TypedExceptionHandler<?>> handlers = new HashMap<Class<?>, TypedExceptionHandler<?>>();
    handlers.put(BazException.class, errorExceptionHandler);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(handlers);

    try {
      handler.handle(new BazException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void testMultipleExceptions() {

    ErrorExceptionHandler defaultHandler = createStrictMock(ErrorExceptionHandler.class);
    defaultHandler.handle(isA(ErrorException.class));
    replay(defaultHandler);

    ErrorExceptionHandler fooHandler = createStrictMock(ErrorExceptionHandler.class);
    fooHandler.handle(isA(FooException.class));
    replay(fooHandler);

    BarExceptionHandler<BarException> barHandler = createStrictMock(BarExceptionHandler.class);
    barHandler.handle(isA(BarException.class));
    replay(barHandler);

    BarExceptionHandler<BazException> bazHandler = createStrictMock(BarExceptionHandler.class);
    bazHandler.handle(isA(BazException.class));
    replay(bazHandler);

    Map<Class<?>, TypedExceptionHandler<?>> handlers = new HashMap<Class<?>, TypedExceptionHandler<?>>();
    handlers.put(ErrorException.class, defaultHandler);
    handlers.put(FooException.class, fooHandler);
    handlers.put(BarException.class, barHandler);
    handlers.put(BazException.class, bazHandler);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(handlers);

    try {
      handler.handle(new ErrorException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
    try {
      handler.handle(new FooException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
    try {
      handler.handle(new BarException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
    try {
      handler.handle(new BazException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }
    try {
      handler.handle(new RuntimeException());
      fail("Should not have handled it");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * For testing.  Foo does not have its own exception handler.  Consequently, it should default to use
   * the ErrorExceptionHandler
   */
  public class FooException extends ErrorException {

  }

  /**
   * extends Foo but has it's own exception handler and, as a result, should use it.
   */
  public class BarException extends FooException {

  }

  /**
   * Extends bar but doesn't have its own handler.  However, because bar has one, it should use bar's exception
   * handler
   */
  public class BazException extends BarException {

  }

  /**
   * Exception handler for Foo
   *
   * @param <T> a bar exception
   */
  public class BarExceptionHandler<T extends BarException> implements TypedExceptionHandler<T> {

    @Override
    public void handle(T exception) {

    }
  }
}
