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
 *
 */
package org.primeframework.test.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

/**
 * <p>
 * This is a mock request dispatcher.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class MockRequestDispatcher implements RequestDispatcher {
    protected String uri;
    protected boolean forwarded;
    protected boolean included;

    public MockRequestDispatcher(String uri) {
        this.uri = uri;
    }

    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        forwarded = true;
    }

    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        included = true;
    }

    public String getUri() {
        return uri;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public boolean isIncluded() {
        return included;
    }
}