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

import java.io.StringWriter;

import org.primeframework.guice.GuiceContainer;
import org.primeframework.mvc.result.message.control.Message;

import static net.java.util.CollectionTools.mapNV;

/**
 * <p>
 * This class is a JSP function that can retrieve messages from the
 * {@link org.primeframework.l10n.MessageProvider} and output them.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class MessageFunction {

    /**
     * Retrieves the message for the given key and the current action invocation.
     *
     * @param   key The key.
     * @return  The message.
     */
    public static String message(String key) {
        return getMessage(key, null);
    }

    /**
     * Retrieves the message for the given key and bundle.
     *
     * @param   key The key.
     * @param   bundle The bundle.
     * @return  The message.
     */
    public static String message(String key, String bundle) {
        return getMessage(key, bundle);
    }

    /**
     * Grabs the message using the {@link Message} control.
     *
     * @param   key The key.
     * @param   bundle (Optional) The bundle name.
     * @return  The message.
     */
    protected static String getMessage(String key, String bundle) {
        StringWriter writer = new StringWriter();
        Message message = GuiceContainer.getInjector().getInstance(Message.class);
        message.renderStart(writer, mapNV("key", key, "bundle", bundle), null);
        message.renderEnd(writer);
        return writer.toString();
    }
}