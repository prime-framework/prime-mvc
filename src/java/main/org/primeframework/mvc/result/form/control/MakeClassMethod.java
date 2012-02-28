/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.result.form.control;

import java.util.List;

import org.primeframework.mvc.freemarker.FieldSupportBeansWrapper;

import net.java.lang.ObjectTools;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * <p> This class is a FreeMarker method that can be invoked within the control templates to create a CSS class. It
 * takes any number of parameters and joins them using dashes. If the first value is null, this returns an empty String.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class MakeClassMethod implements TemplateMethodModelEx {
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() == 0) {
      return FieldSupportBeansWrapper.INSTANCE.wrap("");
    }

    Object first = arguments.get(0);
    if (first == null) {
      return FieldSupportBeansWrapper.INSTANCE.wrap("");
    }

    return FieldSupportBeansWrapper.INSTANCE.wrap(ObjectTools.join(arguments, "-"));
  }
}