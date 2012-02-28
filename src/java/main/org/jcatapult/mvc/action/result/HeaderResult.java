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
package org.jcatapult.mvc.action.result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.google.inject.Inject;
import org.jcatapult.mvc.action.ActionInvocation;
import org.jcatapult.mvc.action.result.annotation.Header;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;

/**
 * <p>
 * This result returns a header only response.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class HeaderResult extends AbstractResult<Header> {
    private final HttpServletResponse response;

    @Inject
    public HeaderResult(ExpressionEvaluator expressionEvaluator, HttpServletResponse response) {
        super(expressionEvaluator);
        this.response = response;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Header header, ActionInvocation invocation) throws IOException, ServletException {
        setStatus(header.status(), header.statusStr(), invocation.action(), response);
    }
}
