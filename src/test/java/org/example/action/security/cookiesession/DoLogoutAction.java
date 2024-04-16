/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.example.action.security.cookiesession;

import com.google.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.security.UserLoginSecurityContext;

@Action
public class DoLogoutAction {
    @Inject
    private UserLoginSecurityContext context;

    public String get() {
        var currentUser = context.getCurrentUser();
        context.logout(currentUser);
        return "success";
    }
}
