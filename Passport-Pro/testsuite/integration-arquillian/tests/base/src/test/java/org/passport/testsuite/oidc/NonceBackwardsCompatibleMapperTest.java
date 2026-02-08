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

package org.passport.testsuite.oidc;

import java.io.IOException;

import jakarta.ws.rs.core.Response;

import org.passport.OAuth2Constants;
import org.passport.admin.client.resource.ClientResource;
import org.passport.events.Details;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.models.utils.ModelToRepresentation;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.mappers.NonceBackwardsCompatibleMapper;
import org.passport.protocol.oidc.utils.OIDCResponseMode;
import org.passport.protocol.oidc.utils.OIDCResponseType;
import org.passport.representations.AccessToken;
import org.passport.representations.AuthorizationResponseToken;
import org.passport.representations.IDToken;
import org.passport.representations.RefreshToken;
import org.passport.representations.idm.EventRepresentation;
import org.passport.representations.idm.ProtocolMapperRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.AbstractTestRealmPassportTest;
import org.passport.testsuite.AssertEvents;
import org.passport.testsuite.admin.ApiUtil;
import org.passport.testsuite.updaters.ClientAttributeUpdater;
import org.passport.testsuite.util.oauth.AccessTokenResponse;
import org.passport.testsuite.util.oauth.AuthorizationEndpointResponse;
import org.passport.util.TokenUtil;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author rmartinc
 */
public class NonceBackwardsCompatibleMapperTest extends AbstractTestRealmPassportTest {

    @Rule
    public AssertEvents events = new AssertEvents(this);

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
    }

    @Test
    public void testNonceWithoutMapper() throws IOException {
        testNonce(false, false);
    }

    @Test
    public void testNonceWithMapper() throws IOException {
        ClientResource testApp = ApiUtil.findClientByClientId(testRealm(), "test-app");
        String mapperId = createNonceMapper(testApp);
        try {
            testNonce(true, false);
        } finally {
            testApp.getProtocolMappers().delete(mapperId);
        }
    }

    @Test
    public void testOfflineSessionNonceWithMapper() throws IOException {
        ClientResource testApp = ApiUtil.findClientByClientId(testRealm(), "test-app");
        String mapperId = createNonceMapper(testApp);
        try {
            testNonce(true, true);
        } finally {
            testApp.getProtocolMappers().delete(mapperId);
        }
    }

    @Test
    public void testImplicitFlowWithoutMapper() throws Exception {
        try (ClientAttributeUpdater client = ClientAttributeUpdater.forClient(adminClient, TEST_REALM_NAME, "test-app")
                .setImplicitFlowEnabled(true)
                .update()) {
            testNonceImplicit(false);
        }
    }

    @Test
    public void testImplicitFlowWithMapper() throws Exception {
        ClientResource testApp = ApiUtil.findClientByClientId(testRealm(), "test-app");
        String mapperId = createNonceMapper(testApp);
        try (ClientAttributeUpdater client = ClientAttributeUpdater.forClient(adminClient, TEST_REALM_NAME, "test-app")
                .setImplicitFlowEnabled(true)
                .update()) {
            testNonceImplicit(true);
        } finally {
            testApp.getProtocolMappers().delete(mapperId);
        }
    }

    private String createNonceMapper(ClientResource testApp) {
        ProtocolMapperModel nonceMapper = NonceBackwardsCompatibleMapper.create("nonce");
        ProtocolMapperRepresentation nonceMapperRep = ModelToRepresentation.toRepresentation(nonceMapper);
        try (Response res = testApp.getProtocolMappers().createMapper(nonceMapperRep)) {
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
            return ApiUtil.getCreatedId(res);
        }
    }

    private void checkNonce(String expectedNonce, String nonce, boolean expected) {
        if (expected) {
            Assert.assertEquals(expectedNonce, nonce);
        } else {
            Assert.assertNull(nonce);
        }
    }

    private void testIntrospection(String accessToken, String expectedNonce, boolean expected) throws IOException {
        JsonNode nonce = oauth.client("test-app", "password").doIntrospectionAccessTokenRequest(accessToken).asJsonNode().get(OIDCLoginProtocol.NONCE_PARAM);
        checkNonce(expectedNonce, nonce != null? nonce.asText() : null, expected);
    }

    private void testNonceImplicit(boolean mapper) throws IOException {
        String nonce = PassportModelUtils.generateId();
        oauth.responseMode(OIDCResponseMode.JWT.value());
        oauth.responseType(OIDCResponseType.TOKEN + " " + OIDCResponseType.ID_TOKEN);
        AuthorizationEndpointResponse response = oauth.loginForm().nonce(nonce).doLogin("test-user@localhost", "password");

        Assert.assertTrue(response.isRedirected());
        AuthorizationResponseToken responseToken = oauth.verifyAuthorizationResponseToken(response.getResponse());

        String accessTokenString = (String) responseToken.getOtherClaims().get("access_token");
        AccessToken token = oauth.verifyToken(accessTokenString);
        checkNonce(nonce, token.getNonce(), mapper);
        String idTokenString = (String) responseToken.getOtherClaims().get("id_token");
        IDToken idToken = oauth.verifyToken(idTokenString, IDToken.class);
        checkNonce(nonce, idToken.getNonce(), true);

        testIntrospection(accessTokenString, nonce, mapper);
        testIntrospection(idTokenString, nonce, true);
    }

    private void testNonce(boolean mapper, boolean offlineSession) throws IOException {
        String nonce = PassportModelUtils.generateId();
        if (offlineSession) {
            oauth.scope(OAuth2Constants.OFFLINE_ACCESS);
        }
        oauth.loginForm().nonce(nonce).doLogin("test-user@localhost", "password");
        EventRepresentation loginEvent = events.expectLogin().assertEvent();

        String code = oauth.parseLoginResponse().getCode();
        AccessTokenResponse response = oauth.doAccessTokenRequest(code);

        AccessToken token = oauth.verifyToken(response.getAccessToken());
        checkNonce(nonce, token.getNonce(), mapper);
        IDToken idToken = oauth.verifyToken(response.getIdToken(), IDToken.class);
        checkNonce(nonce, idToken.getNonce(), true);
        RefreshToken refreshToken = oauth.parseRefreshToken(response.getRefreshToken());
        checkNonce(nonce, refreshToken.getNonce(), mapper);

        EventRepresentation tokenEvent = events.expectCodeToToken(loginEvent.getDetails().get(Details.CODE_ID), loginEvent.getSessionId())
                .detail(Details.REFRESH_TOKEN_TYPE, offlineSession? TokenUtil.TOKEN_TYPE_OFFLINE : TokenUtil.TOKEN_TYPE_REFRESH)
                .assertEvent();

        response = oauth.doRefreshTokenRequest(response.getRefreshToken());
        events.expectRefresh(tokenEvent.getDetails().get(Details.REFRESH_TOKEN_ID), loginEvent.getSessionId())
                .detail(Details.REFRESH_TOKEN_TYPE, offlineSession? TokenUtil.TOKEN_TYPE_OFFLINE : TokenUtil.TOKEN_TYPE_REFRESH)
                .assertEvent();

        token = oauth.verifyToken(response.getAccessToken());
        checkNonce(nonce, token.getNonce(), mapper);
        idToken = oauth.verifyToken(response.getIdToken(), IDToken.class);
        checkNonce(nonce, idToken.getNonce(), mapper);
        refreshToken = oauth.parseRefreshToken(response.getRefreshToken());
        checkNonce(nonce, refreshToken.getNonce(), mapper);

        testIntrospection(response.getAccessToken(), nonce, mapper);
    }
}
