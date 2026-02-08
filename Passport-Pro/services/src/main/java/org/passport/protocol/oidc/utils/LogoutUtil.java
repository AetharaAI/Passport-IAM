/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.passport.protocol.oidc.utils;

import java.net.URI;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.PassportSession;
import org.passport.models.utils.SystemClientUtil;
import org.passport.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.services.messages.Messages;
import org.passport.sessions.AuthenticationSessionModel;

/**
 * Utilities for OIDC logout
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LogoutUtil {

    public static Response sendResponseAfterLogoutFinished(PassportSession session, AuthenticationSessionModel logoutSession) {
        String redirectUri = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_REDIRECT_URI);
        URI finalRedirectUri = getRedirectUriWithAttachedState(redirectUri, logoutSession);
        OIDCAdvancedConfigWrapper config = OIDCAdvancedConfigWrapper.fromClientModel(logoutSession.getClient());
        LoginFormsProvider loginFormsProvider = session.getProvider(LoginFormsProvider.class);

        if (finalRedirectUri != null) {
            if (!config.isLogoutConfirmationEnabled()) {
                return Response.status(302).location(finalRedirectUri).build();
            }
            loginFormsProvider.setAttribute("pageRedirectUri", finalRedirectUri.toString());
        }

        SystemClientUtil.checkSkipLink(session, logoutSession);

        return loginFormsProvider
                .setSuccess(Messages.SUCCESS_LOGOUT)
                .setDetachedAuthSession()
                .createInfoPage();
    }


    public static URI getRedirectUriWithAttachedState(String redirectUri, AuthenticationSessionModel logoutSession) {
        if (redirectUri == null) return null;
        String state = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_STATE_PARAM);

        UriBuilder uriBuilder = UriBuilder.fromUri(redirectUri);
        if (state != null) {
            uriBuilder.queryParam(OIDCLoginProtocol.STATE_PARAM, state);
        }
        return uriBuilder.build();
    }
}
