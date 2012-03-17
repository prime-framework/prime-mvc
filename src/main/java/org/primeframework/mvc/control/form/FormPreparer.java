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
package org.primeframework.mvc.control.form;

import com.google.inject.ImplementedBy;

/**
 * This interface defines the method that forms can be prepared. Form preparation is generally handled by invoking any
 * no-argument methods in the action class that have been annotated with the {@link
 * org.primeframework.mvc.control.form.annotation.FormPrepareMethod} annotation.
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultFormPreparer.class)
public interface FormPreparer {
  /**
   * Prepare the form using all of the FormPrepareMethod from the action class and super classes.
   */
  void prepare();
}