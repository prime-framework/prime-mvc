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
package org.jcatapult.mvc.message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.jcatapult.l10n.MessageProvider;
import org.jcatapult.l10n.MissingMessageException;
import org.jcatapult.mvc.action.ActionInvocation;
import org.jcatapult.mvc.action.ActionInvocationStore;
import org.jcatapult.mvc.message.scope.MessageScope;
import org.jcatapult.mvc.message.scope.MessageType;
import org.jcatapult.mvc.message.scope.Scope;
import org.jcatapult.mvc.message.scope.ScopeProvider;

/**
 * <p>
 * This is the default message workflow implementation. It removes
 * all flash messages from the session and places them in the request.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultMessageStore implements MessageStore {
    private final ActionInvocationStore actionInvocationStore;
    private final MessageProvider messageProvider;
    private final ScopeProvider scopeProvider;

    @Inject
    public DefaultMessageStore(ActionInvocationStore actionInvocationStore, MessageProvider messageProvider,
            ScopeProvider scopeProvider) {
        this.actionInvocationStore = actionInvocationStore;
        this.messageProvider = messageProvider;
        this.scopeProvider = scopeProvider;
    }

    /**
     * {@inheritDoc}
     */
    public void addConversionError(String field, String uri, Map<String, String> dynamicAttributes, Object... values)
    throws MissingMessageException {
        String key = field + ".conversionError";
        String message = messageProvider.getMessage(uri, key, dynamicAttributes, (Object[]) values);
        Scope scope = scopeProvider.lookup(MessageScope.REQUEST);
        scope.addFieldMessage(MessageType.ERROR, field, message);
    }

    /**
     * {@inheritDoc}
     */
    public void addFileUploadSizeError(String field, String uri, long size) throws MissingMessageException {
        String key = field + ".fileUploadSize";
        String message = messageProvider.getMessage(uri, key, size);
        Scope scope = scopeProvider.lookup(MessageScope.REQUEST);
        scope.addFieldMessage(MessageType.ERROR, field, message);
    }

    /**
     * {@inheritDoc}
     */
    public void addFileUploadContentTypeError(String field, String uri, String contentType) throws MissingMessageException {
        String key = field + ".fileUploadContentType";
        String message = messageProvider.getMessage(uri, key, contentType);
        Scope scope = scopeProvider.lookup(MessageScope.REQUEST);
        scope.addFieldMessage(MessageType.ERROR, field, message);
    }

    /**
     * {@inheritDoc}
     */
    public void addFieldMessage(MessageScope scope, String field, String uri, String key, Object... values)
    throws MissingMessageException {
        String message = messageProvider.getMessage(uri, key, (Object[]) values);
        Scope s = scopeProvider.lookup(scope);
        s.addFieldMessage(MessageType.PLAIN, field, message);
    }

    /**
     * {@inheritDoc}
     */
    public void addFieldMessage(MessageScope scope, String field, String key, Object... values)
    throws MissingMessageException {
        ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
        addFieldMessage(scope, field, actionInvocation.actionURI(), key, values);
    }

    /**
     * {@inheritDoc}
     */
    public void addFieldError(MessageScope scope, String field, String uri, String key, Object... values)
    throws MissingMessageException {
        String message = messageProvider.getMessage(uri, key, (Object[]) values);
        Scope s = scopeProvider.lookup(scope);
        s.addFieldMessage(MessageType.ERROR, field, message);
    }

    /**
     * {@inheritDoc}
     */
    public void addFieldError(MessageScope scope, String field, String key, Object... values)
    throws MissingMessageException {
        ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
        addFieldError(scope, field, actionInvocation.actionURI(), key, values);
    }

    /**
     * {@inheritDoc}
     */
    public void addActionMessage(MessageScope scope, String uri, String key, Object... values)
    throws MissingMessageException {
        String message = messageProvider.getMessage(uri, key, (Object[]) values);
        Scope s = scopeProvider.lookup(scope);
        s.addActionMessage(MessageType.PLAIN, message);
    }

    /**
     * {@inheritDoc}
     */
    public void addActionMessage(MessageScope scope, String key, Object... values)
    throws MissingMessageException {
        ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
        addActionMessage(scope, actionInvocation.actionURI(), key, values);
    }

    /**
     * {@inheritDoc}
     */
    public void addActionError(MessageScope scope, String uri, String key, Object... values)
    throws MissingMessageException {
        String message = messageProvider.getMessage(uri, key, (Object[]) values);
        Scope s = scopeProvider.lookup(scope);
        s.addActionMessage(MessageType.ERROR, message);
    }

    /**
     * {@inheritDoc}
     */
    public void addActionError(MessageScope scope, String key, Object... values)
    throws MissingMessageException {
        ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
        addActionError(scope, actionInvocation.actionURI(), key, values);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getActionMessages(MessageType type) {
        List<String> allMessages = new ArrayList<String>();
        List<Scope> allScopes = scopeProvider.getAllScopes();
        for (Scope scope : allScopes) {
            List<String> messages = scope.getActionMessages(type);
            if (messages != null) {
                allMessages.addAll(messages);
            }
        }

        return allMessages;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getActionMessages() {
        return getActionMessages(MessageType.PLAIN);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getActionErrors() {
        return getActionMessages(MessageType.ERROR);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getFieldMessages(MessageType type) {
        Map<String, List<String>> allMessages = new LinkedHashMap<String, List<String>>();
        List<Scope> allScopes = scopeProvider.getAllScopes();
        for (Scope scope : allScopes) {
            Map<String, List<String>> messages = scope.getFieldMessages(type);
            if (messages != null) {
                allMessages.putAll(messages);
            }
        }

        return allMessages;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getFieldMessages() {
        return getFieldMessages(MessageType.PLAIN);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getFieldErrors() {
        return getFieldMessages(MessageType.ERROR);
    }

    /**
     * {@inheritDoc}
     */
    public void clearActionMessages(MessageType type) {
        List<Scope> allScopes = scopeProvider.getAllScopes();
        for (Scope scope : allScopes) {
            scope.clearActionMessages(type);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearFieldMessages(MessageType type) {
        List<Scope> allScopes = scopeProvider.getAllScopes();
        for (Scope scope : allScopes) {
            scope.clearFieldMessages(type);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(MessageType type) {
        return !getActionMessages(type).isEmpty() || !getFieldMessages(type).isEmpty();
    }
}
