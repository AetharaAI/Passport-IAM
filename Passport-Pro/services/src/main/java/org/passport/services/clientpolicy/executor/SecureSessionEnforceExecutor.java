/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.passport.services.clientpolicy.executor;

import org.passport.OAuthErrorException;
import org.passport.models.PassportSession;
import org.passport.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.passport.protocol.oidc.utils.OIDCResponseType;
import org.passport.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.passport.services.clientpolicy.ClientPolicyContext;
import org.passport.services.clientpolicy.ClientPolicyException;
import org.passport.services.clientpolicy.context.AuthorizationRequestContext;
import org.passport.util.TokenUtil;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureSessionEnforceExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

    private static final Logger logger = Logger.getLogger(SecureSessionEnforceExecutor.class);

    private final PassportSession session;

    public SecureSessionEnforceExecutor(PassportSession session) {
        this.session = session;
    }

    @Override
    public String getProviderId() {
        return SecureSessionEnforceExecutorFactory.PROVIDER_ID;
    }

    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case AUTHORIZATION_REQUEST:
                AuthorizationRequestContext authorizationRequestContext = (AuthorizationRequestContext)context;
                executeOnAuthorizationRequest(authorizationRequestContext.getparsedResponseType(),
                    authorizationRequestContext.getAuthorizationEndpointRequest(),
                    authorizationRequestContext.getRedirectUri());
                return;
            default:
                return;
        }
    }

    private void executeOnAuthorizationRequest(
            OIDCResponseType parsedResponseType,
            AuthorizationEndpointRequest request,
            String redirectUri) throws ClientPolicyException {
        logger.trace("Authz Endpoint - authz request");
        if (TokenUtil.isOIDCRequest(request.getScope())) {
            if(request.getNonce() == null) {
                logger.trace("Missing parameter: nonce");
                throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Missing parameter: nonce");
            }
        } else {
            if(request.getState() == null) {
                logger.trace("Missing parameter: state");
                throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Missing parameter: state");
            }
        }
        logger.trace("Passed.");
    }

}
