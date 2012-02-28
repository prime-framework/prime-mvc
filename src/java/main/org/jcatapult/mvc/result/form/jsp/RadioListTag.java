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
package org.jcatapult.mvc.result.form.jsp;

import org.jcatapult.mvc.result.form.control.RadioList;

/**
 * <p>
 * This class is the JSP taglib for the radio list control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class RadioListTag extends AbstractInputTag<RadioList> {
    /**
     * Retrieves the tag's items attribute
     *
     * @return  The tag's items attribute
     */
    public Object getItems() {
        return attributes.get("items");
    }

    /**
     * Populates the tag's items attribute
     *
     * @param   items The tag's items attribute
     */
    public void setItems(Object items) {
        attributes.put("items", items);
    }

    /**
     * Retrieves the tags valueExpr attribute. This is used to generate the value of the radio tags.
     * This is used inconjunction with the items list. If the items are not specified, then this
     * attribute is ignored.
     *
     * @return	Returns the tags valueExpr attribute.
     */
    public String getValueExpr() {
        return (String) attributes.get("valueExpr");
    }

    /**
     * Populates the tags valueExpr attribute. This is used to generate the value of the radio tags.
     * This is used inconjunction with the items list. If the items are not specified, then this
     * attribute is ignored.
     *
     * @param	valueExpr The value of the tags valueExpr attribute
     */
    public void setValueExpr(String valueExpr) {
        attributes.put("valueExpr", valueExpr);
    }

    /**
     * Retrieves the tags textExpr attribute. This is used to generate the text of the radio tags.
     * This is used inconjunction with the items list. If the items are not specified, then this
     * attribute is ignored. If the items are specified, this attribute should contain an expression
     * that is evaluated to produce the text for the radio.
     *
     * @return	Returns the tags textExpr attribute.
     */
    public String getTextExpr() {
        return (String) attributes.get("textExpr");
    }

    /**
     * Populates the tags textExpr attribute. This is used to generate the text of the radio tags.
     * This is used inconjunction with the items list. If the items are not specified, then this
     * attribute is ignored. If the items are specified, this attribute should contain an expression
     * that is evaluated to produce the text for the radio.
     *
     * @param	textExpr The value of the tags textExpr attribute
     */
    public void setTextExpr(String textExpr) {
        attributes.put("textExpr", textExpr);
    }

    /**
     * Retrieves the tags l10nExpr attribute. This is used to generate the text of the radio tags
     * This is used inconjunction with the items list. If the items are not specified, then this
     * attribute is ignored. If the list is specified, this attribute should contain an expression
     * that is evaluated to produce a localization key. This key is used with the
     * {@link org.jcatapult.l10n.MessageProvider} to look up the text for the radio.
     *
     * @return	Returns the tags l10nExpr attribute.
     */
    public String getL10nExpr() {
        return (String) attributes.get("l10nExpr");
    }

    /**
     * Populates the tags l10nExpr attribute. This is used to generate the text of the radio tags.
     * This is used inconjunction with the items list. If the items are not specified, then this
     * attribute is ignored. If the list is specified, this attribute should contain an expression
     * that is evaluated to produce a localization key. This key is used with the
     * {@link org.jcatapult.l10n.MessageProvider} to look up the text for the radio.
     *
     * @param	l10nExpr The value of the tags l10nExpr attribute
     */
    public void setL10nExpr(String l10nExpr) {
        attributes.put("l10nExpr", l10nExpr);
    }

    /**
     * Retrieves the tags uncheckedValue attribute
     *
     * @return	Returns the tags uncheckedValue attribute
     */
    public String getUncheckedValue() {
        return (String) attributes.get("uncheckedValue");
    }

    /**
     * Populates the tags uncheckedValue attribute
     *
     * @param	uncheckedValue The value of the tags uncheckedValue attribute
     */
    public void setUncheckedValue(String uncheckedValue) {
        attributes.put("uncheckedValue", uncheckedValue);
    }

    /**
     * @return  The {@link RadioList} class.
     */
    protected Class<RadioList> controlClass() {
        return RadioList.class;
    }
}