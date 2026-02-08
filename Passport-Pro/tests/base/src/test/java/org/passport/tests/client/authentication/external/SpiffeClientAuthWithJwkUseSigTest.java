package org.passport.tests.client.authentication.external;

import org.passport.broker.spiffe.SpiffeConstants;
import org.passport.common.util.Time;
import org.passport.representations.JsonWebToken;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.oauth.OAuthIdentityProvider;
import org.passport.testframework.oauth.OAuthIdentityProviderConfig;
import org.passport.testframework.oauth.OAuthIdentityProviderConfigBuilder;
import org.passport.testframework.oauth.annotations.InjectOAuthIdentityProvider;
import org.passport.testframework.realm.ManagedRealm;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@PassportIntegrationTest(config = SpiffeClientAuthTest.SpiffeServerConfig.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class SpiffeClientAuthWithJwkUseSigTest extends AbstractClientAuthTest {

    @InjectRealm(config = SpiffeClientAuthTest.ExernalClientAuthRealmConfig.class)
    protected ManagedRealm realm;

    @InjectOAuthIdentityProvider(config = SpiffeWithOidcIdpConfig.class)
    OAuthIdentityProvider identityProvider;

    public SpiffeClientAuthWithJwkUseSigTest() {
        super(null, SpiffeClientAuthTest.INTERNAL_CLIENT_ID, SpiffeClientAuthTest.EXTERNAL_CLIENT_ID);
    }

    @Test
    public void testWithIssClaimAndSigUseOnJwk() {
        JsonWebToken jwt = createDefaultToken();
        assertSuccess(SpiffeClientAuthTest.INTERNAL_CLIENT_ID, doClientGrant(createDefaultToken()));
        assertSuccess(SpiffeClientAuthTest.INTERNAL_CLIENT_ID, jwt.getId(), "https://myissuer", SpiffeClientAuthTest.EXTERNAL_CLIENT_ID, events.poll());
    }

    @Override
    protected String getClientAssertionType() {
        return SpiffeConstants.CLIENT_ASSERTION_TYPE;
    }

    @Override
    protected OAuthIdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    @Override
    protected JsonWebToken createDefaultToken() {
        JsonWebToken token = new JsonWebToken();
        token.id(null);
        token.issuer("https://myissuer");
        token.audience(oAuthClient.getEndpoints().getIssuer());
        token.exp((long) (Time.currentTime() + 300));
        token.subject(SpiffeClientAuthTest.EXTERNAL_CLIENT_ID);
        return token;
    }

    public static class SpiffeWithOidcIdpConfig implements OAuthIdentityProviderConfig {

        @Override
        public OAuthIdentityProviderConfigBuilder configure(OAuthIdentityProviderConfigBuilder config) {
            return config.jwkUse(true);
        }
    }

}
