/*
 * Copyright (c) 2001-2020, Inversoft Inc., All Rights Reserved
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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.inject.Inject;
import freemarker.core.TemplateMarkupOutputModel;
import freemarker.template.TemplateModelException;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This class is an abstract input that is used for any input that uses a list or items. This includes select boxes and
 * checkbox groups.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractListInput extends AbstractInput {
  protected ExpressionEvaluator expressionEvaluator;

  protected AbstractListInput(boolean labeled) {
    super(labeled);
  }

  @Inject
  public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
    this.expressionEvaluator = expressionEvaluator;
  }

  /**
   * Handles the items and value. Here's the skinny:
   * <p>
   * <ul> <li>If items is null, just inserts an empty Map in the attributes under <code>options</code></li> <li>If
   * items
   * is a Collection, loops over it and creates options. The selected state of the options are based on whether or not
   * the value is a Collection or an array or null or just a plain Object. In the collection/array case, if the current
   * items value is in the collection the option is selected. In the plain object case, if the current items value is
   * equal it is selected. Otherwise, it isn't selected. Also, this handles the text and key using the expression
   * attributes or the current items value.</li> <li>If items is a Map, loops over it and creates options. The selected
   * state of the options are based on whether or not the value is a Collection or an array or null or just a plain
   * Object. In the collection/array case, if the current items value is in the collection the option is selected. In
   * the plain object case, if the current items value is equal it is selected. Otherwise, it isn't selected. Also,
   * this
   * handles the text and key using the key from the items Map, the expression attributes or the current items
   * value.</li> </ul>
   */
  @Override
  protected Map<String, Object> makeParameters() {
    Map<String, Object> parameters = super.makeParameters();
    Map<String, Option> options = new LinkedHashMap<>();

    // Grab the value
    Object beanValue = currentAction() != null ? expressionEvaluator.getValue((String) attributes.get("name"), currentAction()) : null;

    // Handle the header option
    String headerValue = (String) attributes.remove("headerValue");
    String headerL10n = (String) attributes.remove("headerL10n");
    if (headerValue != null) {
      String message = "";
      if (headerL10n != null) {
        message = messageProvider.getMessage(headerL10n);
      }

      options.put(headerValue, new Option(message, headerValue.equals("") && beanValue == null, null));
    }

    // Next, let's handle the items here. I'll create a Map that contains a simple inner class
    // that determines if the option is selected or not. This will allow me to get the text
    // as well
    String valueExpr = (String) attributes.remove("valueExpr");
    String textExpr = (String) attributes.remove("textExpr");
    String l10nExpr = (String) attributes.remove("l10nExpr");
    Object items = attributes.remove("items");
    if (items != null) {
      if (items instanceof Collection<?> c) {
        for (Object o : c) {
          Object value = makeValue(o, null, valueExpr);
          options.put(value.toString(), makeOption(o, value, beanValue, textExpr, l10nExpr));
        }
      } else if (items instanceof Map<?, ?> m) {
        for (Map.Entry entry : m.entrySet()) {
          Object value = makeValue(entry.getValue(), entry.getKey(), valueExpr);
          Option option = makeOption(entry.getValue(), value, beanValue, textExpr, l10nExpr);
          options.put(value.toString(), option);
        }
      } else if (items.getClass().isArray()) {
        int length = Array.getLength(items);
        for (int i = 0; i < length; i++) {
          Object itemsValue = Array.get(items, i);
          Object value = makeValue(itemsValue, null, valueExpr);
          Option option = makeOption(itemsValue, value, beanValue, textExpr, l10nExpr);
          options.put(value.toString(), option);
        }
      }
    }

    parameters.put("options", options);
    return parameters;
  }

  /**
   * Makes an option. If the attributes contains a <code>l10nExpr</code>, it is used with the Object to get a message
   * from the {@link MessageProvider}. If that doesn't exist and a <code>textExpr</code> does, it is used to get the
   * text for the option from the object. Otherwise, the object is converted to a String for the text. Also, if the
   * object exists in the given Collection the option is set to selected.
   *
   * @param item      The current value from the items collection/array/map.
   * @param value     The value of the option. This could have been from the items Map or the valueExpr evaluation.
   * @param beanValue The value from the bean, used to determine selected state.
   * @param textExpr  The textExpr attribute.
   * @param l10nExpr  The l10nExpr attribute.
   * @return The option and never null.
   */
  private Option makeOption(Object item, Object value, Object beanValue, String textExpr, String l10nExpr) {
    if (item == null) {
      return new Option("", false, null);
    }

    String text = null;
    if (l10nExpr != null) {
      Object l10nKey = expressionEvaluator.getValue(l10nExpr, item);
      if (l10nKey != null) {
        text = messageProvider.getMessage(l10nKey.toString());
      }
    }

    if (text == null && textExpr != null) {
      text = expressionEvaluator.getValue(textExpr, item).toString();
    }

    if (text == null && item instanceof Enum) {
      // Try to use a fully qualified enum name first.
      try {
        text = messageProvider.getMessage(item.getClass().getSimpleName() + "." + item);
      } catch (MissingMessageException e) {
        try {
          text = messageProvider.getMessage(item.toString());
        } catch (MissingMessageException e1) {
          // Smother this and continue to use the toString() for the text
        }
      }
    }

    // Retrieve the wrapped value of the markup output model
    if (text == null && item instanceof TemplateMarkupOutputModel<?>) {
      try {
        //noinspection rawtypes,unchecked
        text = ((TemplateMarkupOutputModel) item).getOutputFormat().getMarkupString((TemplateMarkupOutputModel) item);
      } catch (TemplateModelException e) {
        throw new PrimeException(e);
      }
    }

    if (text == null) {
      text = item.toString();
    }

    if (beanValue == null) {
      return new Option(text, false, item);
    }

    if (beanValue instanceof Collection) {
      return new Option(text, ((Collection<?>) beanValue).contains(value), item);
    }

    if (beanValue.getClass().isArray()) {
      int length = Array.getLength(beanValue);
      for (int i = 0; i < length; i++) {
        Object arrayValue = Array.get(beanValue, i);
        if (arrayValue != null && arrayValue.equals(value)) {
          return new Option(text, true, item);
        }
      }
    }

    // FreeMarker wraps all maps into Map<String, Object> and does it incorrectly at that.
    // This is a hack to ensure that when keys are compared, it works for Maps. Most of the time
    // the class of the value from the bean and the value for the ListInput will be the same,
    // such as Integer.
    boolean equal;
    if (value != null) {
      if (beanValue.getClass().isInstance(value)) {
        equal = beanValue.equals(value);
      } else {
        equal = beanValue.toString().equals(value.toString());
      }
    } else {
      if (beanValue.getClass().isInstance(item)) {
        equal = beanValue.equals(item);
      } else {
        equal = beanValue.toString().equals(item.toString());
      }
    }

    return new Option(text, equal, item);
  }

  /**
   * Determines the key. If the attribute contains a <code>keyExpr</code>, it is used against the object to get the key.
   * Otherwise, the object is just converted to a String.
   *
   * @param itemsValue The current value from the items collection/array/map used to determine the key.
   * @param key        The key from a items Map or null if items is not a Map.
   * @param valueExpr  The valueExpr attribute.
   * @return The key and never null. If the Object is null, this returns an empty String.
   */
  private Object makeValue(Object itemsValue, Object key, String valueExpr) {
    if (itemsValue == null) {
      return "";
    }

    if (valueExpr != null) {
      Object value = expressionEvaluator.getValue(valueExpr, itemsValue);
      if (value != null) {
        return value;
      }
    }

    if (key != null) {
      return key;
    }

    return itemsValue;
  }

  public static class Option {
    private final Object object;

    private final boolean selected;

    private final String text;

    public Option(String text, boolean selected, Object object) {
      this.text = text;
      this.selected = selected;
      this.object = object;
    }

    public Object getObject() {
      return object;
    }

    public String getText() {
      return text;
    }

    public boolean isSelected() {
      return selected;
    }
  }
}