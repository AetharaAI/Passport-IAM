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

package org.passport.authentication.authenticators.broker;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.AuthenticationFlowError;
import org.passport.authentication.AuthenticationFlowException;
import org.passport.authentication.authenticators.broker.util.ExistingUserInfo;
import org.passport.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.passport.broker.provider.BrokeredIdentityContext;
import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.services.ServicesLogger;
import org.passport.services.messages.Messages;
import org.passport.sessions.AuthenticationSessionModel;
import org.passport.sessions.CommonClientSessionModel;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdpConfirmLinkAuthenticator extends AbstractIdpAuthenticator {

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        String existingUserInfo = authSession.getAuthNote(EXISTING_USER_INFO);
        if (existingUserInfo == null) {
            ServicesLogger.LOGGER.noDuplicationDetected();
            context.attempted();
            return;
        }

        // hide the review button if the idp review execution was not successfully executed before
        boolean hideReviewButton = authSession.getExecutionStatus().entrySet().stream()
                .filter(entry -> CommonClientSessionModel.ExecutionStatus.SUCCESS.equals(entry.getValue()))
                .map(entry -> context.getRealm().getAuthenticationExecutionById(entry.getKey()))
                .filter(exec -> IdpReviewProfileAuthenticatorFactory.PROVIDER_ID.equals(exec.getAuthenticator()))
                .findAny()
                .isEmpty();

        ExistingUserInfo duplicationInfo = ExistingUserInfo.deserialize(existingUserInfo);
        Response challenge = context.form()
                .setStatus(Response.Status.OK)
                .setAttribute(LoginFormsProvider.IDENTITY_PROVIDER_BROKER_CONTEXT, brokerContext)
                .setAttribute("hideReviewButton", hideReviewButton ? Boolean.TRUE : null)
                .setError(Messages.FEDERATED_IDENTITY_CONFIRM_LINK_MESSAGE, duplicationInfo.getDuplicateAttributeName(), duplicationInfo.getDuplicateAttributeValue())
                .createIdpLinkConfirmLinkPage();
        context.challenge(challenge);
    }

    @Override
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String action = formData.getFirst("submitAction");
        if (action != null && action.equals("updateProfile")) {
            context.resetFlow(() -> {
                AuthenticationSessionModel authSession = context.getAuthenticationSession();

                serializedCtx.saveToAuthenticationSession(authSession, BROKERED_CONTEXT_NOTE);
                authSession.setAuthNote(ENFORCE_UPDATE_PROFILE, "true");
            });
        } else if (action != null && action.equals("linkAccount")) {
            context.success();
        } else {
            throw new AuthenticationFlowException("Unknown action: " + action,
                    AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(PassportSession session, RealmModel realm, UserModel user) {
        return false;
    }
}
