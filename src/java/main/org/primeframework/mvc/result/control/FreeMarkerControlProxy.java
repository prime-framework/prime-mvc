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
package org.primeframework.mvc.result.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.primeframework.freemarker.FieldSupportBeansWrapper;

/**
 * <p>
 * This class is a proxy between FreeMarker and the JCatapult MVC controls.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class FreeMarkerControlProxy implements TemplateDirectiveModel {
    private final Control control;

    public FreeMarkerControlProxy(Control control) {
        this.control = control;
    }

    /**
     * Chains to the {@link Control#renderStart(java.io.Writer , java.util.Map , java.util.Map)}
     * method and the {@link Control#renderEnd(java.io.Writer)}
     *
     * @param   env The FreeMarker environment.
     * @param   params The parameters passed to this control in the FTL file.
     * @param   loopVars Loop variables (not really used).
     * @param   body The body of the directive.
     */
    @SuppressWarnings("unchecked")
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
    throws IOException, TemplateException {
        Map<String, String> dynamicAttributes = makeDynamicAttributes(params);
        Map<String, Object> attributes = makeAttributes(params);

        control.renderStart(env.getOut(), attributes, dynamicAttributes);
        control.renderBody(env.getOut(), new FreeMarkerBodyProxy(body));
        control.renderEnd(env.getOut());
    }

    /**
     * Executes the FreeMarker directive body if it is not null. Sub-classes can overwrite this method
     * to change the body handling.
     *
     * @param   env The environment that can be used to get the writer. This default implementation
     *          uses this writer.
     * @param   body The body.
     * @throws  TemplateException If the body fails to render.
     * @throws  IOException If the render can't write to the writer.
     */
    protected void executeBody(Environment env, TemplateDirectiveBody body) throws IOException, TemplateException {
        if (body != null) {
            body.render(env.getOut());
        }
    }

    /**
     * Creates the list of dynamic attributes from the given Map of parameters passed ot the control.
     *
     * @param   params The parameters passed to this control in the FTL file.
     * @return  The dynamic attributes.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, String> makeDynamicAttributes(Map params) {
        Map<String, String> dynamicAttributes = new HashMap<String, String>();
        for (Iterator<String> i = params.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (key.startsWith("_")) {
                Object value = params.get(key);
                dynamicAttributes.put(key.substring(1), value.toString());
                i.remove();
            }
        }
        return dynamicAttributes;
    }

    /**
     * Makes the attributes from the parameters passed to the control by unwrapping the FreeMarker
     * models.
     *
     * @param   params The parameters passed to this control in the FTL file.
     * @return  The attributes.
     * @throws freemarker.template.TemplateModelException If the unwrapping fails.
     */
    protected Map<String, Object> makeAttributes(Map params) throws TemplateModelException {
        Map<String, Object> attributes = new HashMap<String, Object>(params.size());
        for (Object o : params.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object value = entry.getValue();
            if (value != null) {
                attributes.put((String) entry.getKey(), FieldSupportBeansWrapper.INSTANCE.unwrap((TemplateModel) value));
            }
        }

        return attributes;
    }
}