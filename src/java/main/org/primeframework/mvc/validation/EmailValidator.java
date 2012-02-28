/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.validation;

import java.util.regex.Pattern;

import org.primeframework.mvc.validation.annotation.Email;

/**
 * <p>
 * This class verifies that the value is an email address using a relaxed regex
 * (because the all two letter TLDs are allowed).
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class EmailValidator implements Validator<Email> {
    public static final Pattern emailPattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+(?:[a-z]{2}|aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel)");

    /**
     * @param   annotation Not used.
     * @param   container Not used.
     * @param   value The value to check.
     * @return  True if the value matches the pattern, false otherwise.
     */
    public boolean validate(Email annotation, Object container, Object value) {
        if (value == null) {
            return true;
        }
        
        String email = value.toString();
        return emailPattern.matcher(email.toLowerCase()).matches();
    }
}