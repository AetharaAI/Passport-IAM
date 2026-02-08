/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.passport.authentication.authenticators.browser;

import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;

import org.passport.WebAuthnConstants;
import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.authentication.requiredactions.WebAuthnPasswordlessRegisterFactory;
import org.passport.credential.CredentialProvider;
import org.passport.credential.WebAuthnPasswordlessCredentialProvider;
import org.passport.credential.WebAuthnPasswordlessCredentialProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.WebAuthnPolicy;
import org.passport.models.credential.WebAuthnCredentialModel;
import org.passport.services.managers.AuthenticationManager;
import org.passport.sessions.AuthenticationSessionModel;
import org.passport.utils.StringUtil;

/**
 * Authenticator for WebAuthn authentication with passwordless credential. This class is temporary and will be likely
 * removed in the future during future improvements in authentication SPI
 */
public class WebAuthnPasswordlessAuthenticator extends WebAuthnAuthenticator {

    public WebAuthnPasswordlessAuthenticator(PassportSession session) {
        super(session);
    }

    @Override
    protected WebAuthnPolicy getWebAuthnPolicy(AuthenticationFlowContext context) {
        return context.getRealm().getWebAuthnPolicyPasswordless();
    }

    @Override
    protected String getCredentialType() {
        return WebAuthnCredentialModel.TYPE_PASSWORDLESS;
    }

    @Override
    protected boolean shouldDisplayAuthenticators(AuthenticationFlowContext context){
        return false;
    }

    @Override
    public void setRequiredActions(PassportSession session, RealmModel realm, UserModel user) {
        // ask the user to do required action to register webauthn authenticator
        AuthenticationSessionModel authenticationSession = session.getContext().getAuthenticationSession();
        if (!authenticationSession.getRequiredActions().contains(WebAuthnPasswordlessRegisterFactory.PROVIDER_ID)) {
            authenticationSession.addRequiredAction(WebAuthnPasswordlessRegisterFactory.PROVIDER_ID);
        }
    }

    @Override
    public List<RequiredActionFactory> getRequiredActions(PassportSession session) {
        return Collections.singletonList((WebAuthnPasswordlessRegisterFactory)session.getPassportSessionFactory().getProviderFactory(RequiredActionProvider.class, WebAuthnPasswordlessRegisterFactory.PROVIDER_ID));
    }


    @Override
    public WebAuthnPasswordlessCredentialProvider getCredentialProvider(PassportSession session) {
        return (WebAuthnPasswordlessCredentialProvider)session.getProvider(CredentialProvider.class, WebAuthnPasswordlessCredentialProviderFactory.PROVIDER_ID);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

        String username = formData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (StringUtil.isNotBlank(username)) {
            // user entered a username directly, check if user exists
            boolean validUsername = validateUsername(context, formData, username);
            if (!validUsername) {
                context.attempted();
                return;
            }
        } else if (!formData.containsKey(WebAuthnConstants.USER_HANDLE)) {
            // user submitted an empty form without webauthn credential selection
            context.attempted();
            return;
        }

        // user selected a webauthn credential, proceed with webauthn authentication
        super.action(context);
    }

    protected boolean validateUsername(AuthenticationFlowContext context, MultivaluedMap<String, String> formData, String username) {
        return new UsernameForm().validateUser(context, formData);
    }

}
