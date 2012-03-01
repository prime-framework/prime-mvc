/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.result.form.jsp;

import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.result.form.control.CheckboxList;

/**
 * <p> This class is the JSP taglib for a set of checkbox controls. </p>
 *
 * @author Brian Pontarelli
 */
public class CheckboxListTag extends AbstractInputTag<CheckboxList> {
  /**
   * Retrieves the tag's items attribute
   *
   * @return The tag's items attribute
   */
  public Object getItems() {
    return attributes.get("items");
  }

  /**
   * Populates the tag's items attribute
   *
   * @param items The tag's items attribute
   */
  public void setItems(Object items) {
    attributes.put("items", items);
  }

  /**
   * Retrieves the tags valueExpr attribute. This is used to generate the value of the checkbox tags. This is used
   * inconjunction with the items list. If the items are not specified, then this attribute is ignored.
   *
   * @return Returns the tags valueExpr attribute.
   */
  public String getValueExpr() {
    return (String) attributes.get("valueExpr");
  }

  /**
   * Populates the tags valueExpr attribute. This is used to generate the value of the checkbox tags. This is used
   * inconjunction with the items list. If the items are not specified, then this attribute is ignored.
   *
   * @param valueExpr The value of the tags valueExpr attribute
   */
  public void setValueExpr(String valueExpr) {
    attributes.put("valueExpr", valueExpr);
  }

  /**
   * Retrieves the tags textExpr attribute. This is used to generate the text of the checkbox tags. This is used
   * inconjunction with the items list. If the items are not specified, then this attribute is ignored. If the items are
   * specified, this attribute should contain an expression that is evaluated to produce the text for the checkbox.
   *
   * @return Returns the tags textExpr attribute.
   */
  public String getTextExpr() {
    return (String) attributes.get("textExpr");
  }

  /**
   * Populates the tags textExpr attribute. This is used to generate the text of the checkbox tags. This is used
   * inconjunction with the items list. If the items are not specified, then this attribute is ignored. If the items are
   * specified, this attribute should contain an expression that is evaluated to produce the text for the checkbox.
   *
   * @param textExpr The value of the tags textExpr attribute
   */
  public void setTextExpr(String textExpr) {
    attributes.put("textExpr", textExpr);
  }

  /**
   * Retrieves the tags l10nExpr attribute. This is used to generate the text of the checkbox tags This is used
   * inconjunction with the items list. If the items are not specified, then this attribute is ignored. If the list is
   * specified, this attribute should contain an expression that is evaluated to produce a localization key. This key is
   * used with the {@link MessageProvider} to look up the text for the checkbox.
   *
   * @return Returns the tags l10nExpr attribute.
   */
  public String getL10nExpr() {
    return (String) attributes.get("l10nExpr");
  }

  /**
   * Populates the tags l10nExpr attribute. This is used to generate the text of the checkbox tags. This is used
   * inconjunction with the items list. If the items are not specified, then this attribute is ignored. If the list is
   * specified, this attribute should contain an expression that is evaluated to produce a localization key. This key is
   * used with the {@link MessageProvider} to look up the text for the checkbox.
   *
   * @param l10nExpr The value of the tags l10nExpr attribute
   */
  public void setL10nExpr(String l10nExpr) {
    attributes.put("l10nExpr", l10nExpr);
  }

  /**
   * @return The {@link CheckboxList} class.
   */
  protected Class<CheckboxList> controlClass() {
    return CheckboxList.class;
  }
}