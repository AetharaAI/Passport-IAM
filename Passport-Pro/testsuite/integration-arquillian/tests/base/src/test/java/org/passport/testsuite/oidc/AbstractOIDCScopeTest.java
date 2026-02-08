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

package org.passport.testsuite.oidc;

import java.util.Arrays;
import java.util.Collection;

import org.passport.events.Details;
import org.passport.representations.AccessToken;
import org.passport.representations.IDToken;
import org.passport.representations.idm.EventRepresentation;
import org.passport.testsuite.AbstractTestRealmPassportTest;
import org.passport.testsuite.Assert;
import org.passport.testsuite.AssertEvents;
import org.passport.testsuite.pages.AppPage;
import org.passport.testsuite.pages.ErrorPage;
import org.passport.testsuite.pages.LoginPage;
import org.passport.testsuite.pages.OAuthGrantPage;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.jboss.arquillian.graphene.page.Page;
import org.junit.Rule;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractOIDCScopeTest extends AbstractTestRealmPassportTest {

    @Rule
    public AssertEvents events = new AssertEvents(this);

    @Page
    protected AppPage appPage;

    @Page
    protected LoginPage loginPage;

    @Page
    protected OAuthGrantPage grantPage;

    @Page
    protected ErrorPage errorPage;


    protected AbstractOIDCScopeTest.Tokens sendTokenRequest(EventRepresentation loginEvent, String userId, String expectedScope, String clientId) {
        String sessionId = loginEvent.getSessionId();
        String codeId = loginEvent.getDetails().get(Details.CODE_ID);

        String code = oauth.parseLoginResponse().getCode();
        AccessTokenResponse response = oauth.client(clientId, "password").doAccessTokenRequest(code);
        Assert.assertEquals(200, response.getStatusCode());

        // Test scopes
        log.info("expectedScopes = " + expectedScope);
        log.info("responseScopes = " + response.getScope());
        assertScopes(expectedScope, response.getScope());

        IDToken idToken = oauth.verifyIDToken(response.getIdToken());
        AccessToken accessToken = oauth.verifyToken(response.getAccessToken());

        // Test scope in the access token
        assertScopes(expectedScope, accessToken.getScope());

        EventRepresentation codeToTokenEvent = events.expectCodeToToken(codeId, sessionId)
                .user(userId)
                .client(clientId)
                .assertEvent();

        // Test scope in the event
        assertScopes(expectedScope, codeToTokenEvent.getDetails().get(Details.SCOPE));

        return new AbstractOIDCScopeTest.Tokens(idToken, accessToken, response.getRefreshToken());
    }

    public static void assertScopes(String expectedScope, String receivedScope) {
        Collection<String> expectedScopes = Arrays.asList(expectedScope.split(" "));
        Collection<String> receivedScopes = Arrays.asList(receivedScope.split(" "));
        Assert.assertTrue("Not matched. expectedScope: " + expectedScope + ", receivedScope: " + receivedScope,
                expectedScopes.containsAll(receivedScopes) && receivedScopes.containsAll(expectedScopes));
    }

    static class Tokens {
        final IDToken idToken;
        final AccessToken accessToken;
        final String refreshToken;

        private Tokens(IDToken idToken, AccessToken accessToken, String refreshToken) {
            this.idToken = idToken;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
