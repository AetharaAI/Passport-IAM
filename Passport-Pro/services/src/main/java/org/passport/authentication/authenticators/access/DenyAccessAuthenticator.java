/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.authentication.authenticators.access;

import java.util.Optional;

import jakarta.ws.rs.core.Response;

import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.AuthenticationFlowError;
import org.passport.authentication.Authenticator;
import org.passport.events.Errors;
import org.passport.models.AuthenticatorConfigModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.services.messages.Messages;

/**
 * Explicitly deny access to the resources.
 * Useful for example in the conditional flows to be used after satisfying the previous conditions. after satisfying conditions in the conditional flow.
 *
 * @author <a href="mailto:mabartos@redhat.com">Martin Bartos</a>
 */
public class DenyAccessAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String errorMessage = Optional.ofNullable(context.getAuthenticatorConfig())
                .map(AuthenticatorConfigModel::getConfig)
                .map(f -> f.get(DenyAccessAuthenticatorFactory.ERROR_MESSAGE))
                .orElse(Messages.ACCESS_DENIED);

        context.getEvent().error(Errors.ACCESS_DENIED);
        Response challenge = context.form()
                .setError(errorMessage)
                .createErrorPage(Response.Status.UNAUTHORIZED);
        context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(PassportSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(PassportSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
