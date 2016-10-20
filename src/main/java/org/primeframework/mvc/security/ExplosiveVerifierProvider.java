/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import java.util.Map;

import org.primeframework.jwt.Verifier;
import org.primeframework.mvc.PrimeException;

import com.google.inject.Provider;

/**
 * This provider will explode nicely if you try to use it. Users of JWT should bind their own Verifier Provider.
 *
 * @author Daniel DeGroff
 */
public class ExplosiveVerifierProvider implements Provider<Map<String, Verifier>> {

  @Override
  public Map<String, Verifier> get() {
    throw new PrimeException("In order to use the JWT authorization scheme you must bind a Verifier Provider.");
  }
}
