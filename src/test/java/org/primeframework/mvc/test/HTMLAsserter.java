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

import java.util.Locale;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;

public class HTMLAsserter {
  public Document document;

  public RequestResult requestResult;

  public HTMLAsserter(RequestResult requestResult) {
    this.requestResult = requestResult;
    document = Jsoup.parse(requestResult.getBodyAsString());
  }

  /**
   * Ensure a single element matches the provided selector.
   *
   * @param selector the DOM selector
   * @param expected the number of expected matches for this selector in the DOM.
   * @return this.
   */
  public HTMLAsserter assertElementCount(String selector, int expected) {
    Elements elements = document.select(selector);
    if (elements.size() != expected) {
      throw new AssertionError("Expected [" + expected + "] elements to match the selector " + selector + " but found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Ensure no elements match the provided selector.
   *
   * @param selector the DOM selector
   * @return this.
   */
  public HTMLAsserter assertElementDoesNotExist(String selector) {
    Elements elements = document.select(selector);
    if (elements.size() > 0) {
      throw new AssertionError("Expected 0 elements to match the selector " + selector + ". Found [" + (elements.size() + "] elements.\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Assert an element does not have an attribute. The value is not checked.
   *
   * @param selector  the DOM selector
   * @param attribute the attribute you expect
   * @return this.
   */
  public HTMLAsserter assertElementDoesNotHaveAttribute(String selector, String attribute) {
    Element element = selectExpectOne(selector);

    if (element.hasAttr(attribute)) {
      throw new AssertionError("Expected the element not to have attribute [" + attribute + "]." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Ensure a single element matches the provided selector.
   *
   * @param selector the DOM selector
   * @return this.
   */
  public HTMLAsserter assertElementExists(String selector) {
    selectExpectOne(selector);
    return this;
  }

  /**
   * Assert an element has an attribute. The value is not checked.
   *
   * @param selector  the DOM selector
   * @param attribute the attribute you expect
   * @return this.
   */
  public HTMLAsserter assertElementHasAttribute(String selector, String attribute) {
    Element element = selectExpectOne(selector);

    if (!element.hasAttr(attribute)) {
      throw new AssertionError("Expected the element to have attribute [" + attribute + "]." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Assert the element has an attribute with a specific value.
   *
   * @param selector  the DOM selector
   * @param attribute the name of the attribute you expect
   * @param value     the value of the attribute you expect
   * @return this.
   */
  public HTMLAsserter assertElementHasAttributeValue(String selector, String attribute, String value) {
    Element element = selectExpectOne(selector);

    if (!element.hasAttr(attribute)) {
      throw new AssertionError("Expected the element to have attribute [" + attribute + "]." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    String actual = element.attr(attribute);
    if (!value.equals(actual)) {
      throw new AssertionError("Attribute [" + attribute + "] value not equal to expected.\nExpected [" + value + "] but found [" + actual + "]" + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Ensure a single element matches the provided selector and has an expected inner HTML.
   *
   * @param selector          the DOM selector
   * @param expectedInnerHTML the expected inner HTML
   * @return this.
   */
  public HTMLAsserter assertElementInnerHTML(String selector, String expectedInnerHTML) {
    Elements elements = document.select(selector);
    if (elements.size() != 1) {
      throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    Element element = elements.get(0);
    if (!expectedInnerHTML.equals(element.html())) {
      throw new AssertionError("Expected a value of [" + expectedInnerHTML + "] to match the selector " + selector + ". Found [" + element.html() + "] instead." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Ensure a single element matches the provided selector and is "checked"
   *
   * @param selector the DOM selector
   * @return this.
   */
  public HTMLAsserter assertElementIsChecked(String selector) {
    Elements elements = document.select(selector);
    if (elements.size() != 1) {
      throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    if (!elements.get(0).hasAttr("checked")) {
      throw new AssertionError("Expected the element to be checked." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Ensure a single element matches the provided selector and is NOT "checked"
   *
   * @param selector the DOM selector
   * @return this.
   */
  public HTMLAsserter assertElementIsNotChecked(String selector) {
    Elements elements = document.select(selector);
    if (elements.size() != 1) {
      throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    if (elements.get(0).hasAttr("checked")) {
      throw new AssertionError("Expected the element NOT to be checked." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Ensure a single element matches the provided selector and equals the provided value.
   *
   * @param selector the DOM selector
   * @param value    the expected value
   * @return this.
   */
  public HTMLAsserter assertElementValue(String selector, Object value) {
    Elements elements = document.select(selector);
    if (elements.size() != 1) {
      throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    Element element = elements.get(0);
    if (!element.val().equals(value.toString())) {
      throw new AssertionError("Using the selector [" + selector + "] expected [" + value + "] but found [" + element.val() + "]. Actual matched element: \n\n" + element + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Allow for custom assertions on an element.
   *
   * @param selector the DOM selector
   * @param consumer a consumer that will take the element found by the selector
   * @return this.
   */
  public HTMLAsserter assertOnElement(String selector, Consumer<Element> consumer) {
    Element element = selectExpectOne(selector);
    consumer.accept(element);
    return this;
  }

  /**
   * Assert that a Select option is not selected.
   *
   * @param selector the DOM selector
   * @return this.
   */
  public HTMLAsserter assertOptionIsNotSelected(String selector) {
    Element element = selectExpectOne(selector);

    if (!element.is("option")) {
      throw new AssertionError("Expected the element not be an [option] but found [" + element.tagName().toLowerCase(Locale.ROOT) + "].\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    if (element.hasAttr("selected")) {
      throw new AssertionError("Expected the element not to be selected." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Assert that an option is selected.
   *
   * @param selector the DOM selector
   * @return this.
   */
  public HTMLAsserter assertOptionIsSelected(String selector) {
    Element element = selectExpectOne(selector);

    if (!element.is("option")) {
      throw new AssertionError("Expected the element not be an [option] but found [" + element.tagName().toLowerCase(Locale.ROOT) + "].\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    if (!element.hasAttr("selected")) {
      throw new AssertionError("Expected the element to be selected." + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return this;
  }

  /**
   * Perform any custom assertions on the parsed HTML document.
   *
   * @param consumer the HTML document consumer
   * @return this.
   */
  public HTMLAsserter custom(ThrowingConsumer<Document> consumer) throws Exception {
    consumer.accept(document);
    return this;
  }

  private Element selectExpectOne(String selector) {
    Elements elements = document.select(selector);
    if (elements.size() != 1) {
      throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
    }

    return elements.get(0);
  }
}
