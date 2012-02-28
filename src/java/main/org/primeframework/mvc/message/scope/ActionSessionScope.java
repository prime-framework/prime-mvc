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
package org.primeframework.mvc.message.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;

/**
 * <p>
 * This is the message scope which fetches and stores messages in the
 * HttpSession, but are associated with a specific action. In order to
 * accomplish this, a Map is put into the session under various different
 * keys (one key for each type of message - i.e. field errors, action
 * messages, etc.). The key of this Map is the name of the action class
 * and the value is the location where the messages are stored, either a
 * Map or a List depending on the type of message.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ActionSessionScope implements Scope {
    /**
     * The location where the field errors are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the field errors Map.
     */
    public static final String ACTION_SESSION_FIELD_ERROR_KEY = "jcatapultActionSessionFieldErrors";

    /**
     * The location where the field messages are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the field messages Map.
     */
    public static final String ACTION_SESSION_FIELD_MESSAGE_KEY = "jcatapultActionSessionFieldMessages";

    /**
     * The location where the action errors are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the action errors List.
     */
    public static final String ACTION_SESSION_ACTION_ERROR_KEY = "jcatapultActionSessionActionErrors";

    /**
     * The location where the action messages are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the action messages List.
     */
    public static final String ACTION_SESSION_ACTION_MESSAGE_KEY = "jcatapultActionSessionActionMessages";

    private final HttpServletRequest request;
    private final ActionInvocationStore actionInvocationStore;

    @Inject
    public ActionSessionScope(HttpServletRequest request, ActionInvocationStore actionInvocationStore) {
        this.request = request;
        this.actionInvocationStore = actionInvocationStore;
    }


    /**
     * Correctly synchronizes on the session, action session and field messages. It also returns empty
     * maps if anything is missing so that the session doesn't get all filled up with empty maps.
     *
     * @param   type The message type.
     * @return  The Map of field messages or an empty map is there aren't any.
     */
    public Map<String, List<String>> getFieldMessages(MessageType type) {
        String key = (type == MessageType.ERROR) ? ACTION_SESSION_FIELD_ERROR_KEY : ACTION_SESSION_FIELD_MESSAGE_KEY;
        HttpSession session = request.getSession(false);
        Map<String, FieldMessages> actionSession = null;
        if (session != null) {
            synchronized (session) {
                actionSession = (Map<String, FieldMessages>) session.getAttribute(key);
            }
        }

        if (actionSession == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> values;
        synchronized (actionSession) {
            ActionInvocation invocation = actionInvocationStore.getCurrent();
            if (invocation.action() == null) {
                throw new IllegalStateException("Attempting to set an action session message without " +
                    "an action associated with the URL");
            }

            String className = invocation.action().getClass().getName();
            values = actionSession.get(className);
            if (values == null) {
                return Collections.emptyMap();
            }

            // Copy it to protect the Map from threading
            values = new LinkedHashMap<String, List<String>>(values);
        }

        return values;
    }

    /**
     * Correctly synchronizes on the session, action session, and field messages.
     *
     * @param   type The message type.
     * @param   fieldName The field name.
     * @param   message The new message to append.
     */
    public void addFieldMessage(MessageType type, String fieldName, String message) {
        String key = (type == MessageType.ERROR) ? ACTION_SESSION_FIELD_ERROR_KEY : ACTION_SESSION_FIELD_MESSAGE_KEY;
        HttpSession session = request.getSession(true);
        Map<String, FieldMessages> actionSession;
        synchronized (session) {
            actionSession = (Map<String, FieldMessages>) session.getAttribute(key);
            if (actionSession == null) {
                actionSession = new LinkedHashMap<String, FieldMessages>();
                session.setAttribute(key, actionSession);
            }
        }

        FieldMessages values;
        synchronized (actionSession) {
            ActionInvocation invocation = actionInvocationStore.getCurrent();
            if (invocation.action() == null) {
                throw new IllegalStateException("Attempting to set an action session message without " +
                    "an action associated with the URL");
            }

            String className = invocation.action().getClass().getName();
            values = actionSession.get(className);
            if (values == null) {
                values = new FieldMessages();
                actionSession.put(className, values);
            }
        }

        synchronized (values) {
            values.addMessage(fieldName, message);
        }
    }

    /**
     * Correctly synchronizes on the session, action session and action messages. It also returns empty
     * Lists if anything is missing so that the session doesn't get all filled up with empty objects.
     *
     * @param   type The message type.
     * @return  The List of action messages or an empty List is there aren't any.
     */
    public List<String> getActionMessages(MessageType type) {
        String key = (type == MessageType.ERROR) ? ACTION_SESSION_ACTION_ERROR_KEY : ACTION_SESSION_ACTION_MESSAGE_KEY;
        HttpSession session = request.getSession(false);
        Map<String, List<String>> actionSession = null;
        if (session != null) {
            synchronized (session) {
                actionSession = (Map<String, List<String>>) session.getAttribute(key);
            }
        }

        if (actionSession == null) {
            return Collections.emptyList();
        }

        List<String> values;
        synchronized (actionSession) {
            ActionInvocation invocation = actionInvocationStore.getCurrent();
            if (invocation.action() == null) {
                throw new IllegalStateException("Attempting to set an action session message without " +
                    "an action associated with the URL");
            }

            String className = invocation.action().getClass().getName();
            values = actionSession.get(className);
            if (values == null) {
                return Collections.emptyList();
            }

            // Copy it to protect the list from threading
            values = new ArrayList<String>(values);
        }

        return values;
    }

    /**
     * Correctly synchronizes on the session, action session, and action messages.
     *
     * @param   type The message type.
     * @param   message The new message to append.
     */
    public void addActionMessage(MessageType type, String message) {
        String key = (type == MessageType.ERROR) ? ACTION_SESSION_ACTION_ERROR_KEY : ACTION_SESSION_ACTION_MESSAGE_KEY;
        HttpSession session = request.getSession(true);
        Map<String, List<String>> actionSession;
        synchronized (session) {
            actionSession = (Map<String, List<String>>) session.getAttribute(key);
            if (actionSession == null) {
                actionSession = new LinkedHashMap<String, List<String>>();
                session.setAttribute(key, actionSession);
            }
        }

        List<String> values;
        synchronized (actionSession) {
            ActionInvocation invocation = actionInvocationStore.getCurrent();
            if (invocation.action() == null) {
                throw new IllegalStateException("Attempting to set an action session message without " +
                    "an action associated with the URL");
            }

            String className = invocation.action().getClass().getName();
            values = actionSession.get(className);
            if (values == null) {
                values = new ArrayList<String>();
                actionSession.put(className, values);
            }
        }

        synchronized (values) {
            values.add(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearActionMessages(MessageType type) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                if (type == MessageType.ERROR) {
                    session.removeAttribute(ACTION_SESSION_ACTION_ERROR_KEY);
                } else {
                    session.removeAttribute(ACTION_SESSION_ACTION_MESSAGE_KEY);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearFieldMessages(MessageType type) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                if (type == MessageType.ERROR) {
                    session.removeAttribute(ACTION_SESSION_FIELD_ERROR_KEY);
                } else {
                    session.removeAttribute(ACTION_SESSION_FIELD_MESSAGE_KEY);
                }
            }
        }
    }
}
