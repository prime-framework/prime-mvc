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
package org.jcatapult.domain.contact;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * <p>
 * This class is a simple address object that provides just a
 * few helper methods for picking apart address Strings.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@Embeddable
public class EmailAddress implements Serializable {
    private static final long serialVersionUID = 1;

    @Column(name = "email_display", nullable = true)
    private String display;

    @Column(name = "email_address", nullable = true)
    private String address;

    /**
     * Constructs an empty email address.
     */
    public EmailAddress() {
    }

    /**
     * Constructs a new EmailAddress with only and address and no display name.
     *
     * @param   address The address.
     */
    public EmailAddress(String address) {
        this.address = address;
    }

    /**
     * Constructs an email address with the address and display name.
     *
     * @param   address The address.
     * @param   display The display name. The encoding is always UTF-8.
     */
    public EmailAddress(String address, String display) {
        this.address = address;
        this.display = display;
    }

    /**
     * Retrieves the address address.
     *
     * @return  The address address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address address.
     *
     * @param   address The address address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return  The display name for the address address.
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Sets the display name of the address address.
     *
     * @param   display The display name.
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @return  Returns the host portion of the address address. This is the part after the at sign.
     *          (for example, bob@example.com would return example.com)
     */
    public String getHost() {
        return address.split("@")[1];
    }

    /**
     * @return  Returns the account portion of the address address. This is the part before the at
     *          sign. (for example, bob@example.com would return bob).
     */
    public String getAccount() {
        return address.split("@")[0];
    }

    /**
     * Compares the email to the given object for equality. This only compares the address fields.
     *
     * @param   o The object to compare for equality.
     * @return  The if the object is an email and is equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmailAddress email1 = (EmailAddress) o;

        if (!address.equals(email1.address)) return false;

        return true;
    }

    /**
     * Generates a hash code from the address field.
     *
     * @return  The hashcode.
     */
    @Override
    public int hashCode() {
        return address.hashCode();
    }
}