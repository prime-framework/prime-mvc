/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;
import org.primeframework.mvc.util.ThrowingRunnable;

public class DOMHelper {
  public String body;

  public Document document;

  public DOMHelper(String body, Document document) {
    this.body = body;
    this.document = document;
  }

  /**
   * Perform any custom modifications to the HTML document
   *
   * @return this.
   */
  public DOMHelper custom(ThrowingRunnable runnable) throws Exception {
    runnable.run();
    return this;
  }

  /**
   * Perform any custom modifications to the HTML document
   *
   * @param consumer the HTML document consumer
   * @return this.
   */
  public DOMHelper custom(ThrowingConsumer<Document> consumer) throws Exception {
    consumer.accept(document);
    return this;
  }

  /**
   * Remove an attribute from a DOM element.
   *
   * @param selector the DOM selector
   * @param name     the name of the attribute to remove
   * @return this.
   */
  public DOMHelper removeAttribute(String selector, String name) {
    Element element = document.selectFirst(selector);
    if (element == null) {
      throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
    }

    element.removeAttr(name);

    return this;
  }

  /**
   * Set an attribute w/ value on a DOM element.
   *
   * @param selector the DOM selector
   * @param name     the name of the attribute
   * @param value    the value of the attribute
   * @return this.
   */
  public DOMHelper setAttribute(String selector, String name, String value) {
    Element element = document.selectFirst(selector);
    if (element == null) {
      throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
    }

    element.attr(name, value);

    return this;
  }

  public DOMHelper setChecked(String selector, boolean value) {
    Element element = document.selectFirst(selector);
    if (element == null) {
      throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
    }

    if (element.is("input[type=radio]") && value) {
      Elements elements = document.select(element.tagName().toLowerCase() + "[type=radio][name=" + element.attr("name") + "]");
      for (Element e : elements) {
        e.attr("checked", false);
      }
    }

    element.attr("checked", value);
    return this;
  }

  public DOMHelper setValue(String selector, Object value) {
    if (value != null) {
      Element element = document.selectFirst(selector);
      if (element == null) {
        throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
      }

      // Handle a select element
      if (element.is("select")) {
        // Remove the selected attribute for each option, add it to the one that matches the requested value.
        for (Element option : element.getElementsByTag("option")) {
          if (option.attr("value").equals(value.toString())) {
            option.attr("selected", "selected");
          } else {
            option.removeAttr("selected");
          }
        }
      } else {
        element.val(value.toString());
      }
    }

    return this;
  }
}
