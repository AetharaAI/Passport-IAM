/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.protocol.oidc.grants;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.OAuth2Constants;
import org.passport.OAuthErrorException;
import org.passport.authentication.AuthenticationProcessor;
import org.passport.events.Details;
import org.passport.events.Errors;
import org.passport.events.EventType;
import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.AuthenticationFlowModel;
import org.passport.models.ClientSessionContext;
import org.passport.models.Constants;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.models.utils.AuthenticationFlowResolver;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.TokenManager;
import org.passport.representations.AccessTokenResponse;
import org.passport.services.CorsErrorResponseException;
import org.passport.services.Urls;
import org.passport.services.clientpolicy.ClientPolicyException;
import org.passport.services.clientpolicy.context.ResourceOwnerPasswordCredentialsContext;
import org.passport.services.clientpolicy.context.ResourceOwnerPasswordCredentialsResponseContext;
import org.passport.services.managers.AuthenticationManager;
import org.passport.services.managers.AuthenticationSessionManager;
import org.passport.sessions.AuthenticationSessionModel;
import org.passport.sessions.RootAuthenticationSessionModel;
import org.passport.util.TokenUtil;

import org.jboss.logging.Logger;

/**
 * OAuth 2.0 Resource Owner Password Credentials Grant
 * https://datatracker.ietf.org/doc/html/rfc6749#section-4.3
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a> (et al.)
 */
public class ResourceOwnerPasswordCredentialsGrantType extends OAuth2GrantTypeBase {

    private static final Logger logger = Logger.getLogger(ResourceOwnerPasswordCredentialsGrantType.class);

    @Override
    public Response process(Context context) {
        setContext(context);

        event.detail(Details.AUTH_METHOD, "oauth_credentials");

        if (!client.isDirectAccessGrantsEnabled()) {
            String errorMessage = "Client not allowed for direct access grants";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.NOT_ALLOWED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.UNAUTHORIZED_CLIENT, errorMessage, Response.Status.BAD_REQUEST);
        }

        if (client.isConsentRequired()) {
            String errorMessage = "Client requires user consent";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.CONSENT_DENIED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_CLIENT, errorMessage, Response.Status.BAD_REQUEST);
        }

        try {
            session.clientPolicy().triggerOnEvent(new ResourceOwnerPasswordCredentialsContext(formParams));
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw new CorsErrorResponseException(cors, cpe.getError(), cpe.getErrorDetail(), cpe.getErrorStatus());
        }

        String scope = getRequestedScopes();

        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, false);
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);

        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authSession.setAction(AuthenticatedClientSessionModel.Action.AUTHENTICATE.name());
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
        authSession.setClientNote(OIDCLoginProtocol.SCOPE_PARAM, scope);

        AuthenticationFlowModel flow = AuthenticationFlowResolver.resolveDirectGrantFlow(authSession);
        String flowId = flow.getId();
        AuthenticationProcessor processor = new AuthenticationProcessor();
        processor.setAuthenticationSession(authSession)
                .setFlowId(flowId)
                .setFlowPath("token")
                .setConnection(clientConnection)
                .setEventBuilder(event)
                .setRealm(realm)
                .setSession(session)
                .setUriInfo(session.getContext().getUri())
                .setRequest(request);
        Response challenge = processor.authenticateOnly();
        if (challenge != null) {
            // Remove authentication session as "Resource Owner Password Credentials Grant" is single-request scoped authentication
            new AuthenticationSessionManager(session).removeAuthenticationSession(realm, authSession, false);
            cors.add();
            return challenge;
        }
        processor.evaluateRequiredActionTriggers();
        UserModel user = authSession.getAuthenticatedUser();
        if (user.getRequiredActionsStream().count() > 0 || authSession.getRequiredActions().size() > 0) {
            // Remove authentication session as "Resource Owner Password Credentials Grant" is single-request scoped authentication
            new AuthenticationSessionManager(session).removeAuthenticationSession(realm, authSession, false);
            String errorMessage = "Account is not fully set up";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.RESOLVE_REQUIRED_ACTIONS);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_GRANT, errorMessage, Response.Status.BAD_REQUEST);

        }

        AuthenticationManager.setClientScopesInSession(session, authSession);

        ClientSessionContext clientSessionCtx = processor.attachSession();
        clientSessionCtx.setAttribute(Constants.GRANT_TYPE, context.getGrantType());
        UserSessionModel userSession = processor.getUserSession();
        updateUserSessionFromClientAuth(userSession);

        TokenManager.AccessTokenResponseBuilder responseBuilder = tokenManager
            .responseBuilder(realm, client, event, session, userSession, clientSessionCtx).generateAccessToken();
        boolean useRefreshToken = clientConfig.isUseRefreshToken();
        if (useRefreshToken) {
            responseBuilder.generateRefreshToken();
            if (TokenUtil.TOKEN_TYPE_OFFLINE.equals(responseBuilder.getRefreshToken().getType())) {
                // for direct access grants the online session can be removed
                session.sessions().removeUserSession(realm, userSession);
            }
        }

        String scopeParam = clientSessionCtx.getClientSession().getNote(OAuth2Constants.SCOPE);
        if (TokenUtil.isOIDCRequest(scopeParam)) {
            responseBuilder.generateIDToken().generateAccessTokenHash();
        }

        checkAndBindMtlsHoKToken(responseBuilder, useRefreshToken);

        try {
            session.clientPolicy().triggerOnEvent(new ResourceOwnerPasswordCredentialsResponseContext(formParams, clientSessionCtx, responseBuilder));
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw new CorsErrorResponseException(cors, cpe.getError(), cpe.getErrorDetail(), cpe.getErrorStatus());
        }

        AccessTokenResponse res = responseBuilder.build();

        event.success();
        AuthenticationManager.logSuccess(session, authSession);

        return cors.add(Response.ok(res, MediaType.APPLICATION_JSON_TYPE));
    }

    @Override
    public EventType getEventType() {
        return EventType.LOGIN;
    }

}
