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

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jcatapult.mvc.action.ActionInvocation;
import org.jcatapult.mvc.action.result.annotation.Stream;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;
import net.java.lang.StringTools;

/**
 * <p>
 * This result writes bytes to the response output steam.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class StreamResult extends AbstractResult<Stream> {
    private final HttpServletResponse response;

    @Inject
    public StreamResult(ExpressionEvaluator expressionEvaluator, HttpServletResponse response) {
        super(expressionEvaluator);
        this.response = response;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Stream stream, ActionInvocation invocation) throws IOException, ServletException {
        String property = stream.property();
        String length = expand(stream.length(), invocation.action());
        String name = expand(stream.name(), invocation.action());
        String type = expand(stream.type(), invocation.action());

        Object object = expressionEvaluator.getValue(property, invocation.action());
        if (object == null || !(object instanceof InputStream)) {
            throw new IOException("Invalid property [" + property + "] for Stream result. This " +
                "property returned null or an Object that is not an InputStream.");
        }

        response.setContentType(type);

        if (!StringTools.isTrimmedEmpty(length)) {
            response.setContentLength(Integer.parseInt(length));
        }

        if (!StringTools.isTrimmedEmpty(name)) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
        }

        InputStream is = (InputStream) object;
        ServletOutputStream sos = response.getOutputStream();
        try {
            // Then output the file
            byte[] b = new byte[8192];
            int len;
            while ((len = is.read(b)) != -1) {
                sos.write(b, 0, len);
            }
        } finally {
            sos.flush();
            sos.close();
        }
    }
}