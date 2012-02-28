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
package org.primeframework.mvc.message.scope;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * This interface defines the handler for a specific message scope.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface Scope {
    /**
     * Retrieve a Map of all of the field messages stored in the scope. This Map is not live and
     * modifications are not stored in the scope.
     *
     * @param   type The type of messages to retrieve.
     * @return  The Map and never null.
     */
    Map<String, List<String>> getFieldMessages(MessageType type);

    /**
     * Sets a field message.
     *
     * @param   type The type of messages to retrieve.
     * @param   fieldName The name of the field.
     * @param   message The message.
     */
    void addFieldMessage(MessageType type, String fieldName, String message);

    /**
     * Retrieve a List of all of the action messages stored in the scope. This List is not live and
     * modifications are not stored in the scope.
     *
     * @param   type The type of messages to retrieve.
     * @return  The List and never null.
     */
    List<String> getActionMessages(MessageType type);

    /**
     * Sets an action message.
     *
     * @param   type The type of messages to retrieve.
     * @param   message The message.
     */
    void addActionMessage(MessageType type, String message);

    /**
     * Clears all of the action messages of the given type from the scope completely.
     *
     * @param   type The message type to clear.
     */
    void clearActionMessages(MessageType type);

    /**
     * Clears all of the field messages of the given type from the scope completely.
     *
     * @param   type The message type to clear.
     */
    void clearFieldMessages(MessageType type);
}