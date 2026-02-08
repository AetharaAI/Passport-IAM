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

package org.passport.broker.oidc;

import java.io.IOException;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.passport.OAuth2Constants;
import org.passport.OAuthErrorException;
import org.passport.broker.provider.BrokeredIdentityContext;
import org.passport.constants.AdapterConstants;
import org.passport.events.Details;
import org.passport.events.Errors;
import org.passport.events.EventBuilder;
import org.passport.headers.SecurityHeadersProvider;
import org.passport.http.simple.SimpleHttpRequest;
import org.passport.jose.jws.JWSInput;
import org.passport.jose.jws.JWSInputException;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserSessionModel;
import org.passport.representations.adapters.action.AdminAction;
import org.passport.representations.adapters.action.LogoutAction;
import org.passport.services.ErrorResponseException;
import org.passport.services.managers.AuthenticationManager;
import org.passport.util.JsonSerialization;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PassportOIDCIdentityProvider extends OIDCIdentityProvider {

    public PassportOIDCIdentityProvider(PassportSession session, OIDCIdentityProviderConfig config) {
        super(session, config);
        config.setAccessTokenJwt(true); // force access token JWT
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new PassportEndpoint(callback, realm, event, this);
    }

    protected static class PassportEndpoint extends OIDCEndpoint {

        private PassportOIDCIdentityProvider provider;

        public PassportEndpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event,
                PassportOIDCIdentityProvider provider) {
            super(callback, realm, event, provider);
            this.provider = provider;
        }

        @POST
        @Path(AdapterConstants.K_LOGOUT)
        public Response backchannelLogout(String input) {
            JWSInput token = null;
            try {
                token = new JWSInput(input);
            } catch (JWSInputException e) {
                logger.warn("Failed to verify logout request");
                return Response.status(400).build();
            }

            if (!provider.verify(token)) {
                logger.warn("Failed to verify logout request");
                return Response.status(400).build();
            }

            LogoutAction action = null;
            try {
                action = JsonSerialization.readValue(token.getContent(), LogoutAction.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!validateAction(action)) return Response.status(400).build();
            if (action.getPassportSessionIds() != null) {
                for (String sessionId : action.getPassportSessionIds()) {
                    String brokerSessionId = provider.getConfig().getAlias() + "." + sessionId;
                    UserSessionModel userSession = session.sessions().getUserSessionByBrokerSessionId(realm, brokerSessionId);
                    if (userSession != null
                            && userSession.getState() != UserSessionModel.State.LOGGING_OUT
                            && userSession.getState() != UserSessionModel.State.LOGGED_OUT
                            ) {
                        AuthenticationManager.backchannelLogout(session, realm, userSession, session.getContext().getUri(), clientConnection, headers, false);
                    }
                }

            }

            // TODO Empty content with ok makes no sense. Should it display a page? Or use noContent?
            session.getProvider(SecurityHeadersProvider.class).options().allowEmptyContentType();
            return Response.ok().build();
        }

        protected boolean validateAction(AdminAction action)  {
            if (!action.validate()) {
                logger.warn("admin request failed, not validated" + action.getAction());
                return false;
            }
            if (action.isExpired()) {
                logger.warn("admin request failed, expired token");
                return false;
            }
            if (!provider.getConfig().getClientId().equals(action.getResource())) {
                logger.warn("Resource name does not match");
                return false;

            }
            return true;
        }

        @Override
        public SimpleHttpRequest generateTokenRequest(String authorizationCode) {
            return super.generateTokenRequest(authorizationCode)
                    .param(AdapterConstants.CLIENT_SESSION_STATE, "n/a");  // hack to get backchannel logout to work

        }

    }

    @Override
    protected BrokeredIdentityContext exchangeExternalTokenV1Impl(EventBuilder event, MultivaluedMap<String, String> params) {
        String subjectToken = params.getFirst(OAuth2Constants.SUBJECT_TOKEN);
        if (subjectToken == null) {
            event.detail(Details.REASON, OAuth2Constants.SUBJECT_TOKEN + " param unset");
            event.error(Errors.INVALID_TOKEN);
            throw new ErrorResponseException(OAuthErrorException.INVALID_TOKEN, "token not set", Response.Status.BAD_REQUEST);
        }
        String subjectTokenType = params.getFirst(OAuth2Constants.SUBJECT_TOKEN_TYPE);
        if (subjectTokenType == null) {
            subjectTokenType = OAuth2Constants.ACCESS_TOKEN_TYPE;
        }
        return validateJwt(event, subjectToken, subjectTokenType);
    }



}
