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
package org.jcatapult.mvc.validation;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>
 * This tests the email validator.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class EmailValidatorTest {
    @Test
    public void testSimpleEmail() {
        EmailValidator validator = new EmailValidator();
        assertTrue(validator.validate(null, null, "test@test.com"));
        assertTrue(validator.validate(null, null, "brian.pontarelli@example.com.il"));
        assertFalse(validator.validate(null, null, "frank"));
        assertFalse(validator.validate(null, null, "frank@"));
        assertFalse(validator.validate(null, null, "@frank@"));
        assertFalse(validator.validate(null, null, "frank@bad"));
    }
}
