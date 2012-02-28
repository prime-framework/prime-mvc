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
package org.primeframework.mvc.result.message.jsp;

import org.primeframework.mvc.result.jsp.AbstractControlTag;
import org.primeframework.mvc.result.message.control.ActionMessages;

/**
 * <p>
 * This class is a JSP taglib that can retrieve action messages from the
 * {@link org.primeframework.mvc.message.MessageStore} and output them using a
 * FreeMarker template.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ActionMessagesTag extends AbstractControlTag<ActionMessages> {

    /**
     * @return  The tags errors flag that determines if action errors or plain action messages are
     *          output.
     */
    public Boolean getErrors() {
        return (Boolean) attributes.get("errors");
    }

    /**
     * Populates the tags errors attribute that determines if action errors or plain action messages are
     * output.
     *
     * @param	errors The error flag.
     */
    public void setErrors(Boolean errors) {
        attributes.put("errors", errors);
    }

    /**
     * @return  The ActionMessages class.
     */
    protected Class<ActionMessages> controlClass() {
        return ActionMessages.class;
    }
}