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

/**
 * <p>
 * This class is an abstract implementation of the Scope interface. It
 * implements all of the Scope methods and forces sub-classes to simply
 * focus on retrieving the Map and List to storage the messages from
 * the correct location.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public abstract class AbstractJEEScope implements Scope {
    /**
     * The location where the field errors are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the field errors Map.
     */
    public static final String FIELD_ERROR_KEY = "jcatapultFieldErrors";

    /**
     * The location where the field messages are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the field messages Map.
     */
    public static final String FIELD_MESSAGE_KEY = "jcatapultFieldMessages";

    /**
     * The location where the action errors are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the action errors List.
     */
    public static final String ACTION_ERROR_KEY = "jcatapultActionErrors";

    /**
     * The location where the action messages are stored. This keys into the session a Map whose key
     * is the action's class name and whose value is the action messages List.
     */
    public static final String ACTION_MESSAGE_KEY = "jcatapultActionMessages";

    protected String fieldKey(MessageType type) {
        return (type == MessageType.ERROR) ? FIELD_ERROR_KEY : FIELD_MESSAGE_KEY;
    }

    protected String actionKey(MessageType type) {
        return (type == MessageType.ERROR) ? ACTION_ERROR_KEY : ACTION_MESSAGE_KEY;
    }
}