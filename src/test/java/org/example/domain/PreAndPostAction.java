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
package org.example.domain;

import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * This class hs pre and post handling in it.
 *
 * @author Brian Pontarelli
 */
@org.primeframework.mvc.action.annotation.Action("{preField}")
public class PreAndPostAction {
  public String notPre;

  public boolean postCalled;

  public boolean preCalled;

  @PreParameter
  public Integer preField;

  private String preProperty;

  @PreParameter
  public String getPreProperty() {
    return preProperty;
  }

  public void setPreProperty(String preProperty) {
    this.preProperty = preProperty;
  }

  public String post() {
    return null;
  }

  @PostParameterMethod
  public void postParameter() {
    postCalled = true;
    assertNotNull(preField);
    assertNotNull(preProperty);
    assertNotNull(notPre);
  }

  @PreParameterMethod
  public void preParameter() {
    preCalled = true;
    assertNotNull(preField);
    assertNotNull(preProperty);
    assertNull(notPre);
  }
}
