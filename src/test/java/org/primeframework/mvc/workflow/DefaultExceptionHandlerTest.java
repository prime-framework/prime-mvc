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

import org.primeframework.mvc.ErrorException;
import org.testng.annotations.Test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * @author James Humphrey
 */
public class DefaultExceptionHandlerTest {

  @Test
  public void handleRuntimeException() {
    TypedExceptionHandlerFactory factory = new TypedExceptionHandlerFactory(null);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(factory);

    try {
      handler.handle(new RuntimeException());
      fail("Should have thrown a RuntimeException");
    } catch (RuntimeException e) {
      // no op, test successful
    }
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void errorException() {
    ErrorExceptionHandler errorExceptionHandler = createStrictMock(ErrorExceptionHandler.class);
    errorExceptionHandler.handle(isA(ErrorException.class));
    replay(errorExceptionHandler);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ErrorExceptionHandler.class)).andReturn(errorExceptionHandler);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ErrorExceptionHandler.class)).andReturn(null);
    replay(binder);

    TypedExceptionHandlerFactory factory = new TypedExceptionHandlerFactory(injector);
    TypedExceptionHandlerFactory.addExceptionHandler(binder, ErrorException.class, ErrorExceptionHandler.class);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(factory);

    try {
      handler.handle(new ErrorException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }

    verify(errorExceptionHandler, injector, binder);
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void fooException() {
    ErrorExceptionHandler errorExceptionHandler = createStrictMock(ErrorExceptionHandler.class);
    errorExceptionHandler.handle(isA(FooException.class));
    replay(errorExceptionHandler);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ErrorExceptionHandler.class)).andReturn(errorExceptionHandler);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ErrorExceptionHandler.class)).andReturn(null);
    replay(binder);

    TypedExceptionHandlerFactory factory = new TypedExceptionHandlerFactory(injector);
    TypedExceptionHandlerFactory.addExceptionHandler(binder, ErrorException.class, ErrorExceptionHandler.class);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(factory);

    try {
      handler.handle(new FooException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }

    verify(errorExceptionHandler, injector, binder);
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void barException() {
    BarExceptionHandler<BarException> errorExceptionHandler = createStrictMock(BarExceptionHandler.class);
    errorExceptionHandler.handle(isA(BarException.class));
    replay(errorExceptionHandler);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(BarExceptionHandler.class)).andReturn(errorExceptionHandler);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(BarExceptionHandler.class)).andReturn(null);
    replay(binder);

    TypedExceptionHandlerFactory factory = new TypedExceptionHandlerFactory(injector);
    TypedExceptionHandlerFactory.addExceptionHandler(binder, BarException.class, BarExceptionHandler.class);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(factory);

    try {
      handler.handle(new BarException());
    } catch (RuntimeException e) {
      fail("Should have handled it");
    }

    verify(errorExceptionHandler, injector, binder);
  }

  @Test
  @SuppressWarnings(value = "unchecked")
  public void testMultipleExceptions() {
    ErrorExceptionHandler defaultHandler = createStrictMock(ErrorExceptionHandler.class);
    defaultHandler.handle(isA(ErrorException.class));
    defaultHandler.handle(isA(FooException.class));
    replay(defaultHandler);

    BarExceptionHandler<BarException> barHandler = createStrictMock(BarExceptionHandler.class);
    barHandler.handle(isA(BarException.class));
    barHandler.handle(isA(BazException.class));
    replay(barHandler);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ErrorExceptionHandler.class)).andReturn(defaultHandler);
    expect(injector.getInstance(ErrorExceptionHandler.class)).andReturn(defaultHandler);
    expect(injector.getInstance(BarExceptionHandler.class)).andReturn(barHandler);
    expect(injector.getInstance(BarExceptionHandler.class)).andReturn(barHandler);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ErrorExceptionHandler.class)).andReturn(null);
    expect(binder.bind(BarExceptionHandler.class)).andReturn(null);
    replay(binder);

    TypedExceptionHandlerFactory factory = new TypedExceptionHandlerFactory(injector);
    TypedExceptionHandlerFactory.addExceptionHandler(binder, ErrorException.class, ErrorExceptionHandler.class);
    TypedExceptionHandlerFactory.addExceptionHandler(binder, BarException.class, BarExceptionHandler.class);
    DefaultExceptionHandler handler = new DefaultExceptionHandler(factory);

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

    verify(defaultHandler, barHandler, injector, binder);
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
