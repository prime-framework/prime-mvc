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
package org.jcatapult.l10n;

import java.text.MessageFormat;
import java.util.ArrayList;
import static java.util.Arrays.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jcatapult.locale.annotation.CurrentLocale;

import com.google.inject.Inject;

/**
 * <p>
 * This implements the MessageProvider using ResourceBundles inside the
 * web context. It also adds the additional step of looking for multiple
 * bundles if the message isn't initially found. The search method is:
 * </p>
 *
 * <pre>
 * uri = /foo/bar
 * locale = en_US
 *
 * /WEB-INF/message/foo/bar_en_US
 * /WEB-INF/message/foo/bar_en
 * /WEB-INF/message/foo/bar
 * /WEB-INF/message/foo/package_en_US
 * /WEB-INF/message/foo/package_en
 * /WEB-INF/message/foo/package
 * /WEB-INF/message/package_en_US
 * /WEB-INF/message/package_en
 * /WEB-INF/message/package
 * </pre>
 *
 * <p>
 * This stops when /WEB-INF/message is hit.
 * </p>
 *
 * <p>
 * Once the message is found, it is formatted using the {@link java.text.MessageFormat}
 * class. The values are passed in order. The attributes Map is also passed to
 * the format as well. The attributes are always after the values and are in
 * alphabetically order based on the keys for each attribute.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ResourceBundleMessageProvider implements MessageProvider {
    private final Locale locale;
    private final ResourceBundle.Control control;

    @Inject
    public ResourceBundleMessageProvider(@CurrentLocale Locale locale, ResourceBundle.Control control) {
        this.locale = locale;
        this.control = control;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage(String uri, String key, Map<String, String> attributes, Object... values)
    throws MissingMessageException {
        String message = findMessage(uri, key);
        List<Object> params = new ArrayList<Object>(asList(values));
        SortedSet<String> sortedKeys = new TreeSet<String>(attributes.keySet());
        for (String sortedKey : sortedKeys) {
            params.add(attributes.get(sortedKey));
        }

        return MessageFormat.format(message, params.toArray());
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage(String uri, String key, Object... values) throws MissingMessageException {
        String message = findMessage(uri, key);
        return MessageFormat.format(message, values);
    }

    /**
     * Finds the message in a resource bundle using the search method described in the class comment.
     *
     * @param   bundle The bundle to start the search with.
     * @param   key The key of the message.
     * @return  The message or null if it doesn't exist.
     */
    protected String findMessage(String bundle, String key) {
        Queue<String> names = determineBundles(bundle);
        for (String name : names) {
            try {
                ResourceBundle rb = ResourceBundle.getBundle(name, locale, control);
                return rb.getString(key);
            } catch (MissingResourceException e) {
            }
        }

        throw new MissingMessageException("Message could not be found for bundle name [" + bundle +
            "] and key [" + key + "]");
    }

    private Queue<String> determineBundles(String bundle) {
        Queue<String> names = new LinkedList<String>();
        names.offer(bundle);

        int index = bundle.lastIndexOf('/');
        while (index != -1) {
            bundle = bundle.substring(0, index);
            names.offer(bundle + "/package");
            index = bundle.lastIndexOf('/');
        }

        return names;
    }
}