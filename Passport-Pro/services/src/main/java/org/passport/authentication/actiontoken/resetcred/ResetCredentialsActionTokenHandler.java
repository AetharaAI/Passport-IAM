/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.passport.authentication.actiontoken.resetcred;

import jakarta.ws.rs.core.Response;

import org.passport.TokenVerifier.Predicate;
import org.passport.authentication.AuthenticationProcessor;
import org.passport.authentication.actiontoken.AbstractActionTokenHandler;
import org.passport.authentication.actiontoken.ActionTokenContext;
import org.passport.authentication.actiontoken.TokenUtils;
import org.passport.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.passport.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.passport.events.Errors;
import org.passport.events.EventType;
import org.passport.models.UserModel;
import org.passport.services.messages.Messages;
import org.passport.services.resources.LoginActionsService;
import org.passport.services.resources.LoginActionsServiceChecks.IsActionRequired;
import org.passport.sessions.CommonClientSessionModel.Action;

import static org.passport.services.resources.LoginActionsService.RESET_CREDENTIALS_PATH;

/**
 *
 * @author hmlnarik
 */
public class ResetCredentialsActionTokenHandler extends AbstractActionTokenHandler<ResetCredentialsActionToken> {

    public ResetCredentialsActionTokenHandler() {
        super(
          ResetCredentialsActionToken.TOKEN_TYPE,
          ResetCredentialsActionToken.class,
          Messages.RESET_CREDENTIAL_NOT_ALLOWED,
          EventType.RESET_PASSWORD,
          Errors.NOT_ALLOWED
        );

    }

    @Override
    public Predicate<? super ResetCredentialsActionToken>[] getVerifiers(ActionTokenContext<ResetCredentialsActionToken> tokenContext) {
        return TokenUtils.predicates(
            TokenUtils.checkThat(tokenContext.getRealm()::isResetPasswordAllowed, Errors.NOT_ALLOWED, Messages.RESET_CREDENTIAL_NOT_ALLOWED),

            verifyEmail(tokenContext),

            new IsActionRequired(tokenContext, Action.AUTHENTICATE)
        );
    }

    @Override
    public Response handleToken(ResetCredentialsActionToken token, ActionTokenContext tokenContext) {
        AuthenticationProcessor authProcessor = new ResetCredsAuthenticationProcessor();

        return tokenContext.processFlow(
          false,
          RESET_CREDENTIALS_PATH,
          tokenContext.getRealm().getResetCredentialsFlow(),
          null,
          authProcessor
        );
    }

    @Override
    public boolean canUseTokenRepeatedly(ResetCredentialsActionToken token, ActionTokenContext tokenContext) {
        return false;
    }

    public static class ResetCredsAuthenticationProcessor extends AuthenticationProcessor {

        @Override
        protected Response authenticationComplete() {
            boolean firstBrokerLoginInProgress = (authenticationSession.getAuthNote(AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE) != null);
            if (firstBrokerLoginInProgress) {

                SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authenticationSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
                authenticationSession.setAuthNote(AbstractIdpAuthenticator.FIRST_BROKER_LOGIN_SUCCESS, serializedCtx.getIdentityProviderId());

                boolean hasExistingUserInfo = (authenticationSession.getAuthNote(AbstractIdpAuthenticator.EXISTING_USER_INFO) != null);
                String username = "";

                if (hasExistingUserInfo) {
                    UserModel linkingUser = AbstractIdpAuthenticator.getExistingUser(session, realm, authenticationSession);
                    username = linkingUser.getUsername();
                }

                logger.debugf("Forget-password flow finished when authenticated user '%s' after first broker login with identity provider '%s'.",
                        username, serializedCtx.getIdentityProviderId());

                return LoginActionsService.redirectToAfterBrokerLoginEndpoint(session, realm, uriInfo, authenticationSession, true);
            } else {
                return super.authenticationComplete();
            }
        }

    }
}
