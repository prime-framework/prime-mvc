/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.example.action.security.cookiesession;

import java.util.UUID;

import com.google.inject.Inject;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.cookiesession.MockUser;

@Action
@Forward(code = "error", status = 500)
public class DoLoginAction {
    @Inject
    private UserLoginSecurityContext context;
    @Inject
    private MessageStore messageStore;

    public String get() {
        var user = new MockUser("bob");
        user.id = UUID.randomUUID();
        context.login(user);
        return "success";
    }
}
