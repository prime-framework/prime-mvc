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
package org.primeframework.mvc.parameter.convert.converters;

import java.lang.reflect.Type;
import java.util.Currency;
import java.util.Map;

import static net.java.lang.StringTools.*;
import org.primeframework.domain.commerce.Money;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * <p>
 * This converts to and from Money.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@GlobalConverter(forTypes = {Money.class})
@SuppressWarnings("unchecked")
public class MoneyConverter extends AbstractGlobalConverter {
    private boolean emptyIsNull = true;

    @Inject(optional = true)
    public void setEmptyStringIsNull(@Named("jcatapult.mvc.emptyStringIsNull") boolean emptyIsNull) {
        this.emptyIsNull = emptyIsNull;
    }

    protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        if (emptyIsNull && isTrimmedEmpty(value)) {
            return null;
        }

        String code = attributes.get("currencyCode");
        if (isTrimmedEmpty(code)) {
            throw new ConverterStateException("You must provide the currencyCode dynamic attribute. " +
                "If you are using a text field it will look like this: [@jc.text _currencyCode=\"USD\"]");
        }

        Currency currency;
        try {
            currency = Currency.getInstance(code);
        } catch (Exception e) {
            throw new ConverterStateException("Invalid currencyCode [" + code + "]. You must provide a valid " +
                "currency code using the currencyCode dynamic attribute. If you are using a text field " +
                "it will look like this: [@jc.text _currencyCode=\"USD\"]");
        }

        if (value.startsWith(currency.getSymbol())) {
            value = value.substring(1);
        }

        return toMoney(value, code);
    }

    protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        throw new UnsupportedOperationException("You are attempting to map a form field that contains " +
            "multiple parameters to a property on the action class that is of type Money. This isn't " +
            "allowed.");
    }

    protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        return ((Money) value).toNumericString();
    }

    private Money toMoney(String value, String code) {
        try {
            return Money.valueOf(value, Currency.getInstance(code));
        } catch (NumberFormatException e) {
            throw new ConversionException("Invalid Money [" + value + "]", e);
        }
    }
}