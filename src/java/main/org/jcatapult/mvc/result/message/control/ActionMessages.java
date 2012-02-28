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
package org.jcatapult.mvc.result.message.control;

import java.util.Map;

import org.jcatapult.mvc.message.MessageStore;
import org.jcatapult.mvc.message.scope.MessageType;
import org.jcatapult.mvc.result.control.AbstractControl;
import org.jcatapult.mvc.result.control.annotation.ControlAttributes;
import org.jcatapult.mvc.result.control.annotation.ControlAttribute;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the control that outputs the action messages.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "errors", types = {boolean.class, Boolean.class})
    }
)
public class ActionMessages extends AbstractControl {
    private final MessageStore messageStore;

    @Inject
    public ActionMessages(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    /**
     * Adds the action messages.
     *
     * @return  The parameters with the action messages added.
     */
    @Override
    protected Map<String, Object> makeParameters() {
        Map<String, Object> parameters = super.makeParameters();
        parameters.put("action_messages", messageStore.getActionMessages(MessageType.PLAIN));
        parameters.put("action_errors", messageStore.getActionMessages(MessageType.ERROR));
        return parameters;
    }

    /**
     * @return  Null.
     */
    protected String startTemplateName() {
        return null;
    }

    /**
     * @return  The actionmessages.ftl.
     */
    protected String endTemplateName() {
        return "action-messages.ftl";
    }
}