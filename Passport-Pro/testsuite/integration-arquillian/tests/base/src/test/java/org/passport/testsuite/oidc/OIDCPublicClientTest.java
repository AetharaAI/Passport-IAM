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
 *
 */

package org.passport.testsuite.oidc;

import java.util.List;

import org.passport.admin.client.resource.ClientResource;
import org.passport.authentication.authenticators.client.JWTClientAuthenticator;
import org.passport.events.Details;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.EventRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.AbstractPassportTest;
import org.passport.testsuite.AssertEvents;
import org.passport.testsuite.admin.ApiUtil;
import org.passport.testsuite.util.ClientManager;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.passport.testsuite.AbstractAdminTest.loadJson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OIDCPublicClientTest extends AbstractPassportTest {

    @Rule
    public AssertEvents events = new AssertEvents(this);


    @Override
    public void beforeAbstractPassportTest() throws Exception {
        super.beforeAbstractPassportTest();
    }

    @Before
    public void clientConfiguration() {
        ClientManager.realm(adminClient.realm("test")).clientId("test-app").directAccessGrant(true);
        /*
         * Configure the default client ID. Seems like OAuthClient is keeping the state of clientID
         * For example: If some test case configure oauth.clientId("sample-public-client"), other tests
         * will faile and the clientID will always be "sample-public-client
         * @see AccessTokenTest#testAuthorizationNegotiateHeaderIgnored()
         */
        oauth.clientId("test-app");
    }

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        RealmRepresentation realm = loadJson(getClass().getResourceAsStream("/testrealm.json"), RealmRepresentation.class);
        testRealms.add(realm);
    }


    // PASSPORT-18258
    @Test
    public void accessTokenRequest() throws Exception {
        // Update client to use custom client authenticator
        ClientResource clientResource = ApiUtil.findClientByClientId(adminClient.realms().realm("test"), "test-app");
        ClientRepresentation clientRep = clientResource.toRepresentation();
        clientRep.setClientAuthenticatorType(JWTClientAuthenticator.PROVIDER_ID);
        clientResource.update(clientRep);

        // Switch client to public client now
        clientRep = clientResource.toRepresentation();
        Assert.assertEquals(JWTClientAuthenticator.PROVIDER_ID, clientRep.getClientAuthenticatorType());
        clientRep.setPublicClient(true);
        clientResource.update(clientRep);

        // It should be possible to authenticate
        oauth.doLogin("test-user@localhost", "password");

        EventRepresentation loginEvent = events.expectLogin().assertEvent();

        String sessionId = loginEvent.getSessionId();
        String codeId = loginEvent.getDetails().get(Details.CODE_ID);

        String code = oauth.parseLoginResponse().getCode();
        AccessTokenResponse response = oauth.doAccessTokenRequest(code);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getAccessToken());
        EventRepresentation event = events.expectCodeToToken(codeId, sessionId).assertEvent();
    }

}
