/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.passport.authentication.requiredactions;


import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.passport.Config;
import org.passport.authentication.CredentialAction;
import org.passport.authentication.InitiatedActionSupport;
import org.passport.authentication.RequiredActionContext;
import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.authentication.authenticators.util.AcrStore;
import org.passport.authentication.requiredactions.util.CredentialDeleteHelper;
import org.passport.credential.CredentialModel;
import org.passport.events.Details;
import org.passport.events.Errors;
import org.passport.events.EventBuilder;
import org.passport.events.EventType;
import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.UserModel;
import org.passport.models.credential.OTPCredentialModel;
import org.passport.sessions.AuthenticationSessionModel;
import org.passport.utils.StringUtil;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DeleteCredentialAction implements RequiredActionProvider, RequiredActionFactory, CredentialAction {

    public static final String PROVIDER_ID = "delete_credential";

    @Override
    public RequiredActionProvider create(PassportSession session) {
        return this;
    }

    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public String getCredentialType(PassportSession session, AuthenticationSessionModel authenticationSession) {
        String credentialId = authenticationSession.getClientNote(Constants.KC_ACTION_PARAMETER);
        if (credentialId == null) {
            return null;
        }

        UserModel user = authenticationSession.getAuthenticatedUser();
        if (user == null) {
            return null;
        }

        CredentialModel credential = user.credentialManager().getStoredCredentialById(credentialId);
        if (credential == null) {
            if (credentialId.endsWith("-id")) {
                return credentialId.substring(0, credentialId.length() - 3);
            } else {
                return null;
            }
        } else {
            return credential.getType();
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        String credentialId = context.getAuthenticationSession().getClientNote(Constants.KC_ACTION_PARAMETER);
        UserModel user = context.getUser();
        if (credentialId == null) {
            context.getEvent()
                    .error(Errors.MISSING_CREDENTIAL_ID);
            context.ignore();
            return;
        }

        String credentialLabel;
        CredentialModel credential = user.credentialManager().getStoredCredentialById(credentialId);
        if (credential == null) {
            // Backwards compatibility with account console 1 - When stored credential is not found, it may be federated credential.
            // In this case, it's ID needs to be something like "otp-id", which is returned by account REST GET endpoint as a placeholder
            // for federated credentials (See CredentialHelper.createUserStorageCredentialRepresentation )
            if (credentialId.endsWith("-id")) {
                credentialLabel = credentialId.substring(0, credentialId.length() - 3);
            } else {
                context.getEvent()
                        .detail(Details.CREDENTIAL_ID, credentialId)
                        .error(Errors.CREDENTIAL_NOT_FOUND);
                context.ignore();
                return;
            }
        } else {
            credentialLabel = StringUtil.isNotBlank(credential.getUserLabel()) ? credential.getUserLabel() : credential.getType();
        }

        Response challenge = context.form()
                .setAttribute("credentialLabel", credentialLabel)
                .createForm("delete-credential.ftl");
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        EventBuilder event = context.getEvent();
        event.event(EventType.REMOVE_CREDENTIAL);

        EventBuilder deprecatedEvent = null;
        String credentialId = context.getAuthenticationSession().getClientNote(Constants.KC_ACTION_PARAMETER);

        CredentialModel credential = context.getUser().credentialManager().getStoredCredentialById(credentialId);
        if (credential != null) {
            event
                    .detail(Details.CREDENTIAL_TYPE, credential.getType())
                    .detail(Details.CREDENTIAL_ID, credential.getId())
                    .detail(Details.CREDENTIAL_USER_LABEL, credential.getUserLabel());
            if (OTPCredentialModel.TYPE.equals(credential.getType())) {
                deprecatedEvent = event.clone().event(EventType.REMOVE_TOTP);
            }
        }

        try {
            CredentialDeleteHelper.removeCredential(context.getSession(), context.getUser(), credentialId, () -> getCurrentLoa(context.getSession(), context.getAuthenticationSession()));
            context.success();
            if (deprecatedEvent != null) {
                deprecatedEvent.success();
            }

        } catch (WebApplicationException wae) {
            Response response = context.getSession().getProvider(LoginFormsProvider.class)
                    .setAuthenticationSession(context.getAuthenticationSession())
                    .setUser(context.getUser())
                    .setError(wae.getMessage())
                    .createErrorPage(Response.Status.BAD_REQUEST);
            event.detail(Details.REASON, wae.getMessage())
                    .error(Errors.DELETE_CREDENTIAL_FAILED);
            if (deprecatedEvent != null) {
                deprecatedEvent.detail(Details.REASON, wae.getMessage())
                        .error(Errors.DELETE_CREDENTIAL_FAILED);
            }
            context.challenge(response);
        }
    }

    private int getCurrentLoa(PassportSession session, AuthenticationSessionModel authSession) {
        return new AcrStore(session, authSession).getLevelOfAuthenticationFromCurrentAuthentication();
    }

    @Override
    public String getDisplayText() {
        return "Delete Credential";
    }

    @Override
    public void close() {

    }
}
