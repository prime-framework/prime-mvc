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
package org.primeframework.mvc.control.guice;

import org.primeframework.mvc.control.form.Button;
import org.primeframework.mvc.control.form.Checkbox;
import org.primeframework.mvc.control.form.CheckboxList;
import org.primeframework.mvc.control.form.CountriesSelect;
import org.primeframework.mvc.control.form.File;
import org.primeframework.mvc.control.form.Form;
import org.primeframework.mvc.control.form.Hidden;
import org.primeframework.mvc.control.form.Image;
import org.primeframework.mvc.control.form.MonthsSelect;
import org.primeframework.mvc.control.form.Password;
import org.primeframework.mvc.control.form.RadioList;
import org.primeframework.mvc.control.form.Reset;
import org.primeframework.mvc.control.form.Select;
import org.primeframework.mvc.control.form.StatesSelect;
import org.primeframework.mvc.control.form.Submit;
import org.primeframework.mvc.control.form.Text;
import org.primeframework.mvc.control.form.Textarea;
import org.primeframework.mvc.control.form.YearsSelect;
import org.primeframework.mvc.control.message.ActionMessages;
import org.primeframework.mvc.control.message.FieldMessages;
import org.primeframework.mvc.control.message.Message;

import com.google.inject.AbstractModule;

/**
 * This class is a Guice module that configures the Prime MVC controls.
 *
 * @author Brian Pontarelli
 */
public class ControlModule extends AbstractModule {
  @Override
  protected void configure() {
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
  }
}
