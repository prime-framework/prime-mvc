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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

/**
 * <p>
 * This is the flash scope which stores messages in the HttpSession
 * under the flash key. It fetches values from the HttpServletRequest
 * under the same key as well as the HttpSession under that key. This
 * allows for flash messages to be migrated from the session to the request
 * during request handling so that they are not persisted in the session
 * forever. However, it also allows flash values to be retrieved during the
 * initial request from the session.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class FlashScope implements Scope {
    /**
     * The flash key used to store the field errors in the flash.
     */
    public static final String FLASH_FIELD_ERRORS_KEY = "jcatapultFlashFieldErrors";

    /**
     * The flash key used to store the field messages in the flash.
     */
    public static final String FLASH_FIELD_MESSAGES_KEY = "jcatapultFlashFieldMessages";

    /**
     * The flash key used to store the action errors in the flash.
     */
    public static final String FLASH_ACTION_ERRORS_KEY = "jcatapultFlashActionErrors";

    /**
     * The flash key used to store the action messages in the flash.
     */
    public static final String FLASH_ACTION_MESSAGES_KEY = "jcatapultFlashActionMessages";

    private final HttpServletRequest request;

    @Inject
    public FlashScope(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * This combines the flash messages from the request and from the session into a single Map. This
     * allows access to newly added flash messages as well as flash messages from the previous request
     * that have been transfered to the request.
     *
     * @param   type The type.
     * @return  The flash field messages.
     */
    public Map<String, List<String>> getFieldMessages(MessageType type) {
        String key = fieldKey(type);
        Map<String, List<String>> combined = new LinkedHashMap<String, List<String>>();
        FieldMessages fromRequest = (FieldMessages) request.getAttribute(key);
        if (fromRequest != null && fromRequest.size() > 0) {
            combined.putAll(fromRequest);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                FieldMessages fromSession = (FieldMessages) session.getAttribute(key);
                if (fromSession != null && fromSession.size() > 0) {
                    for (String s : fromSession.keySet()) {
                        List<String> list = fromSession.get(s);
                        if (combined.get(s) == null) {
                            combined.put(s, list);
                        } else {
                            combined.get(s).addAll(list);
                        }
                    }
                }
            }
        }

        return combined;
    }

    /**
     * Stores new messages in the flash scope in the session. This correctly synchronizes the session
     * and field messages.
     *
     * @param   type The type of message to store.
     * @param   fieldName The name of the field that the message associated with.
     * @param   message The message itself.
     */
    public void addFieldMessage(MessageType type, String fieldName, String message) {
        HttpSession session = request.getSession(true);
        FieldMessages messages;
        synchronized (session) {
            String key = fieldKey(type);
            messages = (FieldMessages) session.getAttribute(key);
            if (messages == null) {
                messages = new FieldMessages();
                session.setAttribute(key, messages);
            }
        }

        synchronized (messages) {
            messages.addMessage(fieldName, message);
        }
    }

    /**
     * This combines the flash messages from the request and from the session into a single List. This
     * allows access to newly added flash messages as well as flash messages from the previous request
     * that have been transfered to the request.
     *
     * @param   type The type.
     * @return  The flash action messages.
     */
    public List<String> getActionMessages(MessageType type) {
        String key = actionKey(type);
        List<String> combined = new ArrayList<String>();

        List<String> fromRequest = (List<String>) request.getAttribute(key);
        if (fromRequest != null && fromRequest.size() > 0) {
            combined.addAll(fromRequest);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                List<String> fromSession = (List<String>) session.getAttribute(key);
                if (fromSession != null && fromSession.size() > 0) {
                    combined.addAll(fromSession);
                }
            }
        }

        return combined;
    }

    /**
     * Stores new messages in the flash scope in the session. This correctly synchronizes the session
     * and message List.
     *
     * @param   type The type of message to store.
     * @param   message The message itself.
     */
    public void addActionMessage(MessageType type, String message) {
        HttpSession session = request.getSession(true);
        List<String> scope;
        synchronized (session) {
            String key = actionKey(type);
            scope = (List<String>) session.getAttribute(key);
            if (scope == null) {
                scope = new ArrayList<String>();
                session.setAttribute(key, scope);
            }
        }

        synchronized (scope) {
            scope.add(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearActionMessages(MessageType type) {
        if (type == MessageType.ERROR) {
            request.removeAttribute(FLASH_ACTION_ERRORS_KEY);
        } else {
            request.removeAttribute(FLASH_ACTION_MESSAGES_KEY);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                if (type == MessageType.ERROR) {
                    session.removeAttribute(FLASH_ACTION_ERRORS_KEY);
                } else {
                    session.removeAttribute(FLASH_ACTION_MESSAGES_KEY);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearFieldMessages(MessageType type) {
        if (type == MessageType.ERROR) {
            request.removeAttribute(FLASH_FIELD_ERRORS_KEY);
        } else {
            request.removeAttribute(FLASH_FIELD_MESSAGES_KEY);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                if (type == MessageType.ERROR) {
                    session.removeAttribute(FLASH_FIELD_ERRORS_KEY);
                } else {
                    session.removeAttribute(FLASH_FIELD_MESSAGES_KEY);
                }
            }
        }
    }

    /**
     * Moves the flash from the session to the request.
     */
    public void transferFlash() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                transferFlash(request, session, FLASH_FIELD_ERRORS_KEY);
                transferFlash(request, session, FLASH_FIELD_MESSAGES_KEY);
                transferFlash(request, session, FLASH_ACTION_ERRORS_KEY);
                transferFlash(request, session, FLASH_ACTION_MESSAGES_KEY);
            }
        }
    }

    /**
     * Transfers the value from the session under the given key to the request under the given key.
     *
     * @param   request The request.
     * @param   session The session.
     * @param   key The key.
     */
    protected void transferFlash(HttpServletRequest request, HttpSession session, String key) {
        Object flash = session.getAttribute(key);
        if (flash != null) {
            session.removeAttribute(key);
            request.setAttribute(key, flash);
        }
    }

    /**
     * @param   type The message type.
     * @return  The correct key for the message type.
     */
    private String fieldKey(MessageType type) {
        return (type == MessageType.ERROR) ? FLASH_FIELD_ERRORS_KEY : FLASH_FIELD_MESSAGES_KEY;
    }

    /**
     * @param   type The message type.
     * @return  The correct key for the message type.
     */
    private String actionKey(MessageType type) {
        return (type == MessageType.ERROR) ? FLASH_ACTION_ERRORS_KEY : FLASH_ACTION_MESSAGES_KEY;
    }
}
