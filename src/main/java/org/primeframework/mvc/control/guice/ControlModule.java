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
    ControlBinder controlBinder = ControlBinder.newControlBinder(binder());
    controlBinder.add(Button.class).withPrefix("control");
    controlBinder.add(Checkbox.class).withPrefix("control");
    controlBinder.add(CheckboxList.class).withPrefix("control");
    controlBinder.add(CountriesSelect.class).withPrefix("control");
    controlBinder.add(File.class).withPrefix("control");
    controlBinder.add(Form.class).withPrefix("control");
    controlBinder.add(Hidden.class).withPrefix("control");
    controlBinder.add(Image.class).withPrefix("control");
    controlBinder.add(MonthsSelect.class).withPrefix("control");
    controlBinder.add(Password.class).withPrefix("control");
    controlBinder.add(RadioList.class).withPrefix("control");
    controlBinder.add(Reset.class).withPrefix("control");
    controlBinder.add(Select.class).withPrefix("control");
    controlBinder.add(StatesSelect.class).withPrefix("control");
    controlBinder.add(Submit.class).withPrefix("control");
    controlBinder.add(Text.class).withPrefix("control");
    controlBinder.add(Textarea.class).withPrefix("control");
    controlBinder.add(YearsSelect.class).withPrefix("control");
    controlBinder.add(Message.class).withPrefix("control");
  }
}
