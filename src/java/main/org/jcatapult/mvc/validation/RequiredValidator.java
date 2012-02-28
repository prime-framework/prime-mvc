/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.validation;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.jcatapult.mvc.validation.annotation.Required;

/**
 * <p>
 * This class verifies that the value is not null.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class RequiredValidator implements Validator<Required> {
    /**
     * @param   annotation Not used.
     * @param   container Not used.
     * @param   value The value to check.
     * @return  True if the value is not null, false if it is null.
     */
    public boolean validate(Required annotation, Object container, Object value) {
        if (value != null && value instanceof Collection) {
            Collection c = (Collection) value;
            return !c.isEmpty();
        }

        if (value != null && value instanceof Map) {
            Map c = (Map) value;
            return !c.isEmpty();
        }

        if (value != null && value.getClass().isArray()) {
            return Array.getLength(value) > 0;
        }

        return value != null;
    }
}