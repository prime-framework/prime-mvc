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
package org.primeframework.mvc.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.scope.annotation.Flash;

/**
 * <p>
 * This is the flash scope which stores values in the HttpSession inside
 * a Map under the flash key <code>jcatapultFlash</code>. It fetches
 * values from both the HttpServletRequest and HttpSession under that key.
 * This allows for flash objects to be migrated from the session to the request
 * during request handling so that they are not persisted in the session
 * forever. However, it also allows flash values to be retrieved during the
 * initial request from the session.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class FlashScope implements Scope<Flash> {
    public static final String FLASH_KEY = "jcatapultFlash";
    private final HttpServletRequest request;

    @Inject
    public FlashScope(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * {@inheritDoc}
     */
    public Object get(String fieldName, Flash scope) {
        Map<String, Object> flash = (Map<String, Object>) request.getAttribute(FLASH_KEY);

        String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
        if (flash == null || !flash.containsKey(key)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                flash = (Map<String, Object>) session.getAttribute(FLASH_KEY);
            }
        }

        if (flash == null) {
            return null;
        }

        return flash.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public void set(String fieldName, Object value, Flash scope) {
        HttpSession session;
        if (value != null) {
            session = request.getSession(true);
        } else {
            session = request.getSession(false);
        }

        if (session == null) {
            return;
        }
        
        Map<String, Object> flash = (Map<String, Object>) session.getAttribute(FLASH_KEY);

        if (flash == null) {
            flash = new HashMap<String, Object>();
            session.setAttribute(FLASH_KEY, flash);
        }

        String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
        if (value != null) {
            flash.put(key, value);
        } else {
            flash.remove(key);
        }
    }

    /**
     * Moves the flash from the session to the request.
     */
    public void transferFlash() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Map<String, Object> flash = (Map<String, Object>) session.getAttribute(FLASH_KEY);
            if (flash != null) {
                session.removeAttribute(FLASH_KEY);
                request.setAttribute(FLASH_KEY, flash);
            }
        }
    }
}
