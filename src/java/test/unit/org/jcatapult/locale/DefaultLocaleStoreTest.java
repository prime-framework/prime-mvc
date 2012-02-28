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
package org.jcatapult.locale;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>
 * This tests the default locale store.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultLocaleStoreTest {
    @Test
    public void testStoreSession() {
        HttpSession session = EasyMock.createStrictMock(HttpSession.class);
        session.setAttribute(DefaultLocaleStore.LOCALE_KEY, Locale.GERMANY);
        EasyMock.replay(session);

        HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
        EasyMock.expect(request.getSession(false)).andReturn(session);
        request.setAttribute("javax.servlet.jsp.jstl.fmt.locale", Locale.GERMANY);
        EasyMock.replay(request);

        DefaultLocaleStore provider = new DefaultLocaleStore(request);
        provider.set(Locale.GERMANY);

        EasyMock.verify(request, session);
    }

    @Test
    public void testStoreRequest() {
        HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
        EasyMock.expect(request.getSession(false)).andReturn(null);
        request.setAttribute(DefaultLocaleStore.LOCALE_KEY, Locale.GERMANY);
        request.setAttribute("javax.servlet.jsp.jstl.fmt.locale", Locale.GERMANY);
        EasyMock.replay(request);

        DefaultLocaleStore provider = new DefaultLocaleStore(request);
        provider.set(Locale.GERMANY);

        EasyMock.verify(request);
    }

    @Test
    public void testLookupSession() {
        HttpSession session = EasyMock.createStrictMock(HttpSession.class);
        EasyMock.expect(session.getAttribute(DefaultLocaleStore.LOCALE_KEY)).andReturn(Locale.CANADA);
        EasyMock.replay(session);

        HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
        EasyMock.expect(request.getSession(false)).andReturn(session);
        request.setAttribute("javax.servlet.jsp.jstl.fmt.locale", Locale.CANADA);
        EasyMock.replay(request);

        DefaultLocaleStore provider = new DefaultLocaleStore(request);
        assertEquals(Locale.CANADA, provider.get());

        EasyMock.verify(session);
    }

    @Test
    public void testLookupRequest() {
        HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
        EasyMock.expect(request.getSession(false)).andReturn(null);
        EasyMock.expect(request.getAttribute(DefaultLocaleStore.LOCALE_KEY)).andReturn(Locale.CANADA);
        request.setAttribute("javax.servlet.jsp.jstl.fmt.locale", Locale.CANADA);
        EasyMock.replay(request);

        DefaultLocaleStore provider = new DefaultLocaleStore(request);
        assertEquals(Locale.CANADA, provider.get());

        EasyMock.verify(request);
    }

    @Test
    public void testLookupClient() {
        HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
        EasyMock.expect(request.getSession(false)).andReturn(null);
        EasyMock.expect(request.getAttribute(DefaultLocaleStore.LOCALE_KEY)).andReturn(null);
        EasyMock.expect(request.getLocale()).andReturn(Locale.CANADA);
        request.setAttribute("javax.servlet.jsp.jstl.fmt.locale", Locale.CANADA);
        EasyMock.replay(request);

        DefaultLocaleStore provider = new DefaultLocaleStore(request);
        assertEquals(Locale.CANADA, provider.get());

        EasyMock.verify(request);
    }
}