/*
 * Copyright (c) 2009, JCatapult.org, All Rights Reserved
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

import org.jcatapult.mvc.parameter.annotation.PostParameterMethod;
import org.jcatapult.mvc.parameter.annotation.PreParameter;
import org.jcatapult.mvc.parameter.annotation.PreParameterMethod;
import static org.junit.Assert.*;

/**
 * <p>
 * This class hs pre and post handling in it.
 * </p>
 *
 * @author Brian Pontarelli
 */
@org.jcatapult.mvc.action.annotation.Action("{preField}")
public class PreAndPostAction {
    @PreParameter
    public Integer preField;

    private String preProperty;

    public String notPre;

    public boolean preCalled;
    public boolean postCalled;

    @PreParameter
    public String getPreProperty() {
        return preProperty;
    }

    public void setPreProperty(String preProperty) {
        this.preProperty = preProperty;
    }

    @PreParameterMethod
    public void pre() {
        preCalled = true;
        assertNotNull(preField);
        assertNotNull(preProperty);
        assertNull(notPre);
    }

    @PostParameterMethod
    public void post() {
        postCalled = true;
        assertNotNull(preField);
        assertNotNull(preProperty);
        assertNotNull(notPre);
    }
}
