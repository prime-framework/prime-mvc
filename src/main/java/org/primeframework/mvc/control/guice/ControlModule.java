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
import org.primeframework.mvc.control.form.CountrySelect;
import org.primeframework.mvc.control.form.DefaultFormPreparer;
import org.primeframework.mvc.control.form.File;
import org.primeframework.mvc.control.form.Form;
import org.primeframework.mvc.control.form.FormPreparer;
import org.primeframework.mvc.control.form.Hidden;
import org.primeframework.mvc.control.form.Image;
import org.primeframework.mvc.control.form.MonthSelect;
import org.primeframework.mvc.control.form.Password;
import org.primeframework.mvc.control.form.RadioList;
import org.primeframework.mvc.control.form.Reset;
import org.primeframework.mvc.control.form.Select;
import org.primeframework.mvc.control.form.StateSelect;
import org.primeframework.mvc.control.form.Submit;
import org.primeframework.mvc.control.form.Text;
import org.primeframework.mvc.control.form.Textarea;
import org.primeframework.mvc.control.form.YearSelect;
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
    bind(ControlFactory.class);
    ControlFactory.addControl(binder(), "control", "button", Button.class);
    ControlFactory.addControl(binder(), "control", "checkbox", Checkbox.class);
    ControlFactory.addControl(binder(), "control", "checkbox_list", CheckboxList.class);
    ControlFactory.addControl(binder(), "control", "country_select", CountrySelect.class);
    ControlFactory.addControl(binder(), "control", "file", File.class);
    ControlFactory.addControl(binder(), "control", "form", Form.class);
    ControlFactory.addControl(binder(), "control", "hidden", Hidden.class);
    ControlFactory.addControl(binder(), "control", "image", Image.class);
    ControlFactory.addControl(binder(), "control", "month_select", MonthSelect.class);
    ControlFactory.addControl(binder(), "control", "password", Password.class);
    ControlFactory.addControl(binder(), "control", "radio_list", RadioList.class);
    ControlFactory.addControl(binder(), "control", "reset", Reset.class);
    ControlFactory.addControl(binder(), "control", "select", Select.class);
    ControlFactory.addControl(binder(), "control", "state_select", StateSelect.class);
    ControlFactory.addControl(binder(), "control", "submit", Submit.class);
    ControlFactory.addControl(binder(), "control", "text", Text.class);
    ControlFactory.addControl(binder(), "control", "textarea", Textarea.class);
    ControlFactory.addControl(binder(), "control", "year_select", YearSelect.class);
    ControlFactory.addControl(binder(), "control", "message", Message.class);

    bindFormPreparer();
  }

  protected void bindFormPreparer() {
    bind(FormPreparer.class).to(DefaultFormPreparer.class);
  }
}
