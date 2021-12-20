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
package org.primeframework.mvc.control.form;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * This class is a FreeMarker method that can be invoked within the control templates to join multiple strings together.
 * It takes any number of parameters and joins them using dashes. If the first value is null, this returns an empty
 * String.
 *
 * @author Brian Pontarelli
 */
public class JoinMethod implements TemplateMethodModelEx {
  private final ObjectWrapper objectWrapper;

  public JoinMethod(ObjectWrapper objectWrapper) {
    this.objectWrapper = objectWrapper;
  }

  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() == 0) {
      return objectWrapper.wrap("");
    }

    Object first = arguments.get(0);
    if (first == null) {
      return objectWrapper.wrap("");
    }

    @SuppressWarnings("unchecked")
    Object result = arguments.subList(1, arguments.size())
                             .stream()
                             .map(Objects::toString)
                             .collect(Collectors.joining(first.toString()));
    return objectWrapper.wrap(result);
  }
}