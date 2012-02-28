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
import java.io.ByteArrayInputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jcatapult.mvc.action.ActionInvocation;
import org.jcatapult.mvc.action.result.annotation.XMLStream;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * <p>
 * This result writes bytes which represent xml to the response output
 * stream and sets the content type to "application/xhtml+xml"
 * </p>
 *
 * @author  jhumphrey
 */
public class XMLStreamResult extends AbstractResult<XMLStream> {
    private final HttpServletResponse response;

    @Inject
    public XMLStreamResult(ExpressionEvaluator expressionEvaluator, HttpServletResponse response) {
        super(expressionEvaluator);
        this.response = response;
    }

    public void execute(XMLStream xmlStream, ActionInvocation invocation) throws IOException, ServletException {
        String xml = xmlStream.property();

        Object object = expressionEvaluator.getValue(xml, invocation.action());
        if (object == null || !(object instanceof String)) {
            throw new IOException("Invalid property [" + xml + "] for XMLStream result. This " +
                "property returned null or an Object that is not a String.");
        }

        byte[] xmlBytes = ((String) object).getBytes("UTF-8");

        response.setStatus(xmlStream.status());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");
        response.setContentLength(xmlBytes.length);

        InputStream is = new ByteArrayInputStream(xmlBytes);
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
