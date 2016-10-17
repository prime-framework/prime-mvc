/*
 * Copyright (c) 2015-2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security.guice;

import java.util.List;

import org.primeframework.jwt.Verifier;
import org.primeframework.mvc.security.DefaultJWTExtractor;
import org.primeframework.mvc.security.DefaultSavedRequestWorkflow;
import org.primeframework.mvc.security.DefaultSecurityWorkflow;
import org.primeframework.mvc.security.ExplosiveVerifierProvider;
import org.primeframework.mvc.security.JWTExtractor;
import org.primeframework.mvc.security.JWTSecurityScheme;
import org.primeframework.mvc.security.SavedRequestWorkflow;
import org.primeframework.mvc.security.SecurityWorkflow;
import org.primeframework.mvc.security.UserLoginSecurityScheme;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * A Guice modules for the Security classes.
 *
 * @author Brian Pontarelli
 */
public class SecurityModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SecurityWorkflow.class).to(DefaultSecurityWorkflow.class);
    bind(SavedRequestWorkflow.class).to(DefaultSavedRequestWorkflow.class);

    bind(JWTExtractor.class).to(DefaultJWTExtractor.class);
    bind(new TypeLiteral<List<Verifier>>() {}).toProvider(ExplosiveVerifierProvider.class);

    // Binds the user login scheme
    SecuritySchemeFactory.addSecurityScheme(binder(), "user", UserLoginSecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "jwt", JWTSecurityScheme.class);
  }
}
