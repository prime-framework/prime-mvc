/*
 * Copyright (c) 2001-2007, Inversoft, All Rights Reserved
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
package org.jcatapult.jndi;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class is a mock JNDI tree that is used to store data sources and other such items for testing. I decided not to
 * use the Spring one because I didn't want to tie TM to Spring.
 *
 * @author Brian Pontarelli
 */
public class MockJNDI implements InitialContextFactoryBuilder {
  private Map<String, Object> context = new HashMap<String, Object>();

  public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
    return new MockInitialContextFactory(context);
  }

  /**
   * Binds an object to the name.
   *
   * @param name  The name to bind to.
   * @param value The value to bind.
   */
  public void bind(String name, Object value) {
    context.put(name, value);
  }

  /**
   * Activates this JNDI context.
   */
  public void activate() {
    try {
      NamingManager.setInitialContextFactoryBuilder(this);
    } catch (NamingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This mock initial context factory.
   */
  public static class MockInitialContextFactory implements InitialContextFactory {
    private Map<String, Object> context = new HashMap<String, Object>();

    public MockInitialContextFactory(Map<String, Object> context) {
      this.context = context;
    }

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
      return new MockContext(context);
    }
  }

  /**
   * The mock context.
   */
  public static class MockContext implements Context {
    private Map<String, Object> context = new HashMap<String, Object>();

    public MockContext(Map<String, Object> context) {
      this.context = context;
    }

    public Object lookup(Name name) throws NamingException {
      String fullName = name.toString();
      Object value = context.get(fullName);
      if (value == null) {
        throw new NamingException("Nothing at name [" + fullName + "]");
      }
      return value;
    }

    public Object lookup(String name) throws NamingException {
      Object value = context.get(name);
      if (value == null) {
        throw new NamingException("Nothing at name [" + name + "]");
      }
      return value;
    }

    public void bind(Name name, Object obj) throws NamingException {
      String fullName = name.toString();
      if (context.containsKey(fullName)) {
        throw new NamingException("Name already bound [" + name + "]");
      }
      context.put(fullName, obj);
    }

    public void bind(String name, Object obj) throws NamingException {
      if (context.containsKey(name)) {
        throw new NamingException("Name already bound [" + name + "]");
      }
      context.put(name, obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
      String fullName = name.toString();
      context.put(fullName, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
      context.put(name, obj);
    }

    public void unbind(Name name) throws NamingException {
      String fullName = name.toString();
      context.remove(fullName);
    }

    public void unbind(String name) throws NamingException {
      context.remove(name);
    }

    public void rename(Name oldName, Name newName) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public void rename(String oldName, String newName) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public void destroySubcontext(Name name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public void destroySubcontext(String name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public Context createSubcontext(Name name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public Context createSubcontext(String name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public Object lookupLink(Name name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public Object lookupLink(String name) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public NameParser getNameParser(Name name) throws NamingException {
      return new NameParser() {
        @Override
        public Name parse(String name) throws NamingException {
          return new CompositeName(name);
        }
      };
    }

    public NameParser getNameParser(String name) throws NamingException {
      return new NameParser() {
        @Override
        public Name parse(String name) throws NamingException {
          return new CompositeName(name);
        }
      };
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public String composeName(String name, String prefix) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public void close() throws NamingException {
      throw new NamingException("not implemented yet");
    }

    public String getNameInNamespace() throws NamingException {
      throw new NamingException("not implemented yet");
    }
  }
}