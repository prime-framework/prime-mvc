/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.example.action.security.cookiesession;

import java.util.UUID;

import com.google.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.cookiesession.MockUser;

@Action
public class DoLoginAction {
    @Inject
    private UserLoginSecurityContext context;

    public String get() {
        var user = new MockUser("bob");
        user.id = UUID.randomUUID();
        context.login(user);
        return "success";
    }
}
