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
package org.primeframework.mvc.result.message.control;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primeframework.l10n.MessageProvider;
import org.primeframework.l10n.MissingMessageException;
import org.primeframework.mvc.result.control.AbstractControl;
import org.primeframework.mvc.result.control.annotation.ControlAttribute;
import org.primeframework.mvc.result.control.annotation.ControlAttributes;

import com.google.inject.Inject;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This class a a FreeMarker method model as well as the control for
 * the message JSP tag.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "key")
    },
    optional = {
        @ControlAttribute(name = "values", types = List.class)
    }
)
public class Message extends AbstractControl implements TemplateMethodModel {
    private final MessageProvider messageProvider;

    @Inject
    public Message(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    /**
     * Determines the bundle and then gets the message and puts it into the attributes.
     *
     */
    @Override
    protected void addAdditionalAttributes() {
        String bundle = determineBundleName(attributes);
        String key = (String) attributes.remove("key");
        String defaultMesg = (String) attributes.remove("default");
        List values = (List) attributes.remove("values");
        if (values == null) {
            values = new ArrayList();
        }

        String message;
        try {
            message = messageProvider.getMessage(bundle, key, values.toArray());
        } catch (MissingMessageException e) {
            message = defaultMesg;
        }

        if (message == null) {
            throw new IllegalStateException("The message for the key [" + key + "] is missing and " +
                "there was no default set using the [default] attribute.");
        }

        attributes.put("message", message);
    }

    /**
     * Calls the {@link #renderStart(Writer, Map, Map)} and {@link #renderEnd(Writer)} methods using
     * a StringWriter to collect the result and the first and second parameters to the method. The
     * first is the key and the second is the bundle, which can be left out.
     *
     * @param   arguments The method arguments.
     * @return  The result.
     * @throws  TemplateModelException If the action is null and bundle is not specified.
     */
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 1 || arguments.size() != 2) {
            throw new TemplateModelException("Invalid parameters to the message method. This method " +
                "takes one or two parameters like this: message(key) or message(key, bundle)");
        }

        StringWriter writer = new StringWriter();
        String key = (String) arguments.get(0);
        String bundle = (String) (arguments.size() > 1 ? arguments.get(1) : null);
        Map<String, Object> attributes = mapNV("key", key, "bundle", bundle);
        renderStart(writer, attributes, new HashMap<String, String>());
        renderEnd(writer);
        return writer.toString();
    }

    @Override
    protected String startTemplateName() {
        return null;
    }

    @Override
    protected String endTemplateName() {
        return "message.ftl";
    }
}