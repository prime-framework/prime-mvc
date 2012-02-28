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
package org.primeframework.mvc.guice;

import org.primeframework.mvc.action.result.ForwardResult;
import org.primeframework.mvc.action.result.freemarker.FreeMarkerMap;
import org.primeframework.mvc.parameter.convert.DefaultConverterProvider;
import org.primeframework.mvc.parameter.convert.converters.BooleanConverter;
import org.primeframework.mvc.parameter.convert.converters.CharacterConverter;
import org.primeframework.mvc.parameter.convert.converters.CollectionConverter;
import org.primeframework.mvc.parameter.convert.converters.DateTimeConverter;
import org.primeframework.mvc.parameter.convert.converters.EnumConverter;
import org.primeframework.mvc.parameter.convert.converters.FileConverter;
import org.primeframework.mvc.parameter.convert.converters.LocalDateConverter;
import org.primeframework.mvc.parameter.convert.converters.LocaleConverter;
import org.primeframework.mvc.parameter.convert.converters.MoneyConverter;
import org.primeframework.mvc.parameter.convert.converters.NumberConverter;
import org.primeframework.mvc.parameter.convert.converters.StringConverter;
import org.primeframework.mvc.result.form.control.Button;
import org.primeframework.mvc.result.form.control.Checkbox;
import org.primeframework.mvc.result.form.control.CheckboxList;
import org.primeframework.mvc.result.form.control.CountriesSelect;
import org.primeframework.mvc.result.form.control.File;
import org.primeframework.mvc.result.form.control.Form;
import org.primeframework.mvc.result.form.control.Hidden;
import org.primeframework.mvc.result.form.control.Image;
import org.primeframework.mvc.result.form.control.MonthsSelect;
import org.primeframework.mvc.result.form.control.Password;
import org.primeframework.mvc.result.form.control.RadioList;
import org.primeframework.mvc.result.form.control.Reset;
import org.primeframework.mvc.result.form.control.Select;
import org.primeframework.mvc.result.form.control.StatesSelect;
import org.primeframework.mvc.result.form.control.Submit;
import org.primeframework.mvc.result.form.control.Text;
import org.primeframework.mvc.result.form.control.Textarea;
import org.primeframework.mvc.result.form.control.YearsSelect;
import org.primeframework.mvc.result.message.control.ActionMessages;
import org.primeframework.mvc.result.message.control.FieldMessages;
import org.primeframework.mvc.result.message.control.Message;

import com.google.inject.AbstractModule;

/**
 * This class is the main Guice Module that sets up the JCatapult MVC.
 *
 * @author Brian Pontarelli
 */
public class MVCModule extends AbstractModule {
  protected void configure() {
    configureConverters();
    configureModels();
    configureFreeMarker();
  }

  /**
   * Binds all of the default type converters.
   */
  protected void configureConverters() {
    bind(BooleanConverter.class);
    bind(CharacterConverter.class);
    bind(CollectionConverter.class);
    bind(DateTimeConverter.class);
    bind(EnumConverter.class);
    bind(FileConverter.class);
    bind(LocalDateConverter.class);
    bind(LocaleConverter.class);
    bind(MoneyConverter.class);
    bind(NumberConverter.class);
    bind(StringConverter.class);

    // Inject the registry so that the Class to Class mapping is setup
    requestStaticInjection(DefaultConverterProvider.class);
  }

  /**
   * This binds the models so that they can be resolved by the FreeMarker result handling and dynamically added to the
   * Map as directives under the jc key and using the class name as the directive name.
   */
  protected void configureModels() {
    bind(ActionMessages.class);
    bind(Button.class);
    bind(Checkbox.class);
    bind(CheckboxList.class);
    bind(CountriesSelect.class);
    bind(FieldMessages.class);
    bind(File.class);
    bind(Form.class);
    bind(Hidden.class);
    bind(Image.class);
    bind(Message.class);
    bind(MonthsSelect.class);
    bind(Password.class);
    bind(RadioList.class);
    bind(Reset.class);
    bind(Select.class);
    bind(StatesSelect.class);
    bind(Submit.class);
    bind(Text.class);
    bind(Textarea.class);
    bind(YearsSelect.class);

    requestStaticInjection(ForwardResult.class);
  }

  /**
   * Sets up the JspTaglib handling and ServletContext for the FreeMarkerMap.
   */
  protected void configureFreeMarker() {
    requestStaticInjection(FreeMarkerMap.class);
  }
}