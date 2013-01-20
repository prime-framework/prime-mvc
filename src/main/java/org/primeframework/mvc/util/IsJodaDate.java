/*
 * Copyright (c) 2011, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.util;

import java.util.List;

import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * This class determines if a value in FreeMarker is a joda date object.
 *
 * @author Brian Pontarelli
 */
public class IsJodaDate implements TemplateMethodModelEx {
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.isEmpty()) {
      throw new TemplateModelException("You must pass in a single value that is to be checked" +
        " to see if it is a Joda class. Like this: [#if is_joda_date(value)]");
    }

    Object obj = arguments.get(0);
    if (obj instanceof BeanModel) {
      obj = ((BeanModel) obj).getWrappedObject();
      if (obj instanceof ReadableInstant || obj instanceof ReadablePartial) {
        return TemplateBooleanModel.TRUE;
      }
    }

    return TemplateBooleanModel.FALSE;
  }
}
