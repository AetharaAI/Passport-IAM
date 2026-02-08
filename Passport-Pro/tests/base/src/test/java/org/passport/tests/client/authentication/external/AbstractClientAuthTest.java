package org.passport.tests.client.authentication.external;

import org.passport.OAuth2Constants;
import org.passport.events.EventType;
import org.passport.representations.AccessToken;
import org.passport.representations.JsonWebToken;
import org.passport.representations.idm.EventRepresentation;
import org.passport.testframework.annotations.InjectEvents;
import org.passport.testframework.events.EventAssertion;
import org.passport.testframework.events.Events;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.OAuthIdentityProvider;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.junit.jupiter.api.Assertions;

public abstract class AbstractClientAuthTest {

    final String expectedTokenIssuer;
    final String internalClientId;
    final String externalClientId;

    @InjectOAuthClient
    OAuthClient oAuthClient;

    @InjectEvents
    Events events;

    public AbstractClientAuthTest(String expectedTokenIssuer, String internalClientId, String externalClientId) {
        this.expectedTokenIssuer = expectedTokenIssuer;
        this.internalClientId = internalClientId;
        this.externalClientId = externalClientId;
    }

    protected abstract OAuthIdentityProvider getIdentityProvider();

    protected abstract JsonWebToken createDefaultToken();

    protected AccessTokenResponse doClientGrant(JsonWebToken token) {
        String jws = getIdentityProvider().encodeToken(token);
        return doClientGrant(jws);
    }

    protected AccessTokenResponse doClientGrant(String jws) {
        AccessTokenResponse response = oAuthClient.clientCredentialsGrantRequest().clientJwt(jws, getClientAssertionType()).send();
        return response;
    }

    protected void assertSuccess(String expectedClientId, AccessTokenResponse response) {
        Assertions.assertTrue(response.isSuccess());
        AccessToken accessToken = oAuthClient.parseToken(response.getAccessToken(), AccessToken.class);
        Assertions.assertEquals(expectedClientId, accessToken.getIssuedFor());
    }

    protected void assertSuccess(String expectedClientId, String expectedAssertionId, String expectedAssertionIssuer, String expectedAssertionSub, EventRepresentation event) {
        EventAssertion.assertSuccess(event)
                .type(EventType.CLIENT_LOGIN)
                .clientId(expectedClientId)
                .details("client_assertion_id", expectedAssertionId)
                .details("client_assertion_issuer", expectedAssertionIssuer)
                .details("client_assertion_sub", expectedAssertionSub)
                .details("client_auth_method", "federated-jwt")
                .details("grant_type", "client_credentials")
                .details("username", "service-account-" + expectedClientId);
    }

    protected void assertFailure(AccessTokenResponse response) {
        assertFailure("Invalid client or Invalid client credentials", response);
    }

    protected void assertFailure(String expectedErrorDescription, AccessTokenResponse response) {
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertEquals("invalid_client", response.getError());
        Assertions.assertEquals(expectedErrorDescription, response.getErrorDescription());
    }

    protected void assertFailure(String expectedClientId, String expectedAssertionIssuer, String expectedAssertionSub, String expectedAssertionId, EventRepresentation event) {
        assertFailure(expectedClientId, expectedAssertionIssuer, expectedAssertionSub, expectedAssertionId, "invalid_client_credentials", event);
    }

    protected void assertFailure(String expectedClientId, String expectedAssertionIssuer, String expectedAssertionSub, String expectedAssertionId, String expectedError, EventRepresentation event) {
        EventAssertion.assertError(event)
                .type(EventType.CLIENT_LOGIN_ERROR)
                .clientId(expectedClientId)
                .error(expectedError)
                .details("client_assertion_id", expectedAssertionId)
                .details("client_assertion_issuer", expectedAssertionIssuer)
                .details("client_assertion_sub", expectedAssertionSub)
                .details("grant_type", "client_credentials");
    }

    protected String getClientAssertionType() {
        return OAuth2Constants.CLIENT_ASSERTION_TYPE_JWT;
    }

}
