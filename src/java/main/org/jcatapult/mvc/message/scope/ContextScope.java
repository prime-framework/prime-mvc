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
package org.jcatapult.mvc.message.scope;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

/**
 * <p>
 * This is the message scope which fetches and stores values in the
 * ServletContext. The values are stored in the servlet context using
 * a variety of different keys.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ContextScope extends AbstractJEEScope {
    private final ServletContext context;

    @Inject
    public ContextScope(ServletContext context) {
        this.context = context;
    }

    /**
     * Correctly synchronizes the context.
     *
     * @param   type Used to determine the key to lookup the message map from.
     * @return  The Map or an empty map if the context doesn't contain the FieldMessages yet.
     */
    public Map<String, List<String>> getFieldMessages(MessageType type) {
        FieldMessages messages;
        synchronized (context) {
            messages = (FieldMessages) context.getAttribute(fieldKey(type));
            if (messages == null) {
                return Collections.emptyMap();
            }

            // Copy the map to protect it from threading
            return new LinkedHashMap<String, List<String>>(messages);
        }
    }

    /**
     * Correctly synchronizes the context and the FieldMessages object.
     *
     * @param   type Used to determine the key to lookup the message map from.
     * @param   fieldName The name of the field.
     * @param   message The message to append.
     */
    public void addFieldMessage(MessageType type, String fieldName, String message) {
        FieldMessages messages;
        synchronized (context) {
            String key = fieldKey(type);
            messages = (FieldMessages) context.getAttribute(key);
            if (messages == null) {
                messages = new FieldMessages();
                context.setAttribute(key, messages);
            }
        }

        synchronized (messages) {
            messages.addMessage(fieldName, message);
        }
    }

    /**
     * Correctly synchronizes the context.
     *
     * @param   type Used to determine the key to lookup the message map from.
     * @return  The List or an empty List if the context doesn't contain the action messages yet.
     */
    public List<String> getActionMessages(MessageType type) {
        List<String> messages;
        synchronized (context) {
            messages = (List<String>) context.getAttribute(actionKey(type));
            if (messages == null) {
                return Collections.emptyList();
            }

            // Copy the map to protect it from threading
            return new ArrayList<String>(messages);
        }
    }

    /**
     * Correctly synchronizes the context and the action messages List.
     *
     * @param   type Used to determine the key to lookup the message map from.
     * @param   message The message to append.
     */
    public void addActionMessage(MessageType type, String message) {
        List<String> messages;
        synchronized (context) {
            String key = actionKey(type);
            messages = (List<String>) context.getAttribute(key);
            if (messages == null) {
                messages = new ArrayList<String>();
                context.setAttribute(key, messages);
            }
        }

        synchronized (messages) {
            messages.add(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearActionMessages(MessageType type) {
        synchronized (context) {
            if (type == MessageType.ERROR) {
                context.removeAttribute(ACTION_ERROR_KEY);
            } else {
                context.removeAttribute(ACTION_MESSAGE_KEY);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearFieldMessages(MessageType type) {
        synchronized (context) {
            if (type == MessageType.ERROR) {
                context.removeAttribute(FIELD_ERROR_KEY);
            } else {
                context.removeAttribute(FIELD_MESSAGE_KEY);
            }
        }
    }
}
