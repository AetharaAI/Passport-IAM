/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.passport.authentication.authenticators.browser;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.CredentialValidator;
import org.passport.credential.CredentialProvider;
import org.passport.credential.PasswordCredentialProvider;
import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.services.messages.Messages;

public class PasswordForm extends UsernamePasswordForm implements CredentialValidator<PasswordCredentialProvider> {

    public PasswordForm(PassportSession session) {
        super(session);
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validatePassword(context, context.getUser(), formData, false);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (alreadyAuthenticatedUsingPasswordlessCredential(context)) {
            context.success();
            return;
        }

        // setup webauthn data when passkeys enabled
        if (isConditionalPasskeysEnabled(context.getUser())) {
            webauthnAuth.fillContextForm(context);
        }

        Response challengeResponse = context.form().createLoginPassword();
        context.challenge(challengeResponse);
    }

    @Override
    public boolean configuredFor(PassportSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().isConfiguredFor(getCredentialProvider(session).getType())
                || (isConditionalPasskeysEnabled(user))
                || alreadyAuthenticatedUsingPasswordlessCredential(session.getContext().getAuthenticationSession());
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginPassword();
    }

    @Override
    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        return Messages.INVALID_PASSWORD;
    }

    @Override
    public PasswordCredentialProvider getCredentialProvider(PassportSession session) {
        return (PasswordCredentialProvider)session.getProvider(CredentialProvider.class, "passport-password");
    }
}
