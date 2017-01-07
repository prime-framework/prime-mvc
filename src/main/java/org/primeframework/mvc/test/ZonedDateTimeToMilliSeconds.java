/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import java.time.ZonedDateTime;
import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

/**
 * Convert a {@link ZonedDateTime} to Epoch Millisconds.
 *
 * @author Daniel DeGroff
 */
public class ZonedDateTimeToMilliSeconds implements TemplateMethodModelEx {

  private static final String ERROR_MESSAGE = "You must pass a ZonedDateTime object like this:\n\n" +
      "  to_milli(zonedDateTime)";

  @Override
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() != 1) {
      throw new TemplateModelException(ERROR_MESSAGE);
    }

    ZonedDateTime zdt = (ZonedDateTime) DeepUnwrap.unwrap((TemplateModel) arguments.get(0));
    return zdt.toInstant().toEpochMilli();
  }
}
