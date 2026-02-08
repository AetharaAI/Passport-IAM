package org.passport.tests.client.authentication.external;

import org.passport.authentication.authenticators.client.FederatedJWTClientAuthenticator;
import org.passport.broker.spiffe.SpiffeConstants;
import org.passport.broker.spiffe.SpiffeIdentityProviderConfig;
import org.passport.broker.spiffe.SpiffeIdentityProviderFactory;
import org.passport.common.Profile;
import org.passport.common.util.Time;
import org.passport.representations.JsonWebToken;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.oauth.OAuthIdentityProvider;
import org.passport.testframework.oauth.OAuthIdentityProviderConfig;
import org.passport.testframework.oauth.OAuthIdentityProviderConfigBuilder;
import org.passport.testframework.oauth.annotations.InjectOAuthIdentityProvider;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.remote.timeoffset.InjectTimeOffSet;
import org.passport.testframework.remote.timeoffset.TimeOffSet;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testsuite.util.IdentityProviderBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@PassportIntegrationTest(config = SpiffeClientAuthTest.SpiffeServerConfig.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class SpiffeClientAuthTest extends AbstractBaseClientAuthTest {

    static final String INTERNAL_CLIENT_ID = "myclient";
    static final String EXTERNAL_CLIENT_ID = "spiffe://mytrust-domain/myclient";
    static final String IDP_ALIAS = "spiffe-idp";
    static final String TRUST_DOMAIN = "spiffe://mytrust-domain";
    static final String BUNDLE_ENDPOINT = "http://127.0.0.1:8500/idp/jwks";

    @InjectRealm(config = ExernalClientAuthRealmConfig.class)
    protected ManagedRealm realm;

    @InjectOAuthIdentityProvider(config = SpiffeIdpConfig.class)
    OAuthIdentityProvider identityProvider;

    @InjectTimeOffSet
    TimeOffSet timeOffSet;

    public SpiffeClientAuthTest() {
        super(null, INTERNAL_CLIENT_ID, EXTERNAL_CLIENT_ID);
    }

    @Test
    public void testKeysCached() {
        int initialKeyRequests = identityProvider.getKeysRequestCount();
        Assertions.assertTrue(doClientGrant(createDefaultToken()).isSuccess());
        Assertions.assertTrue(doClientGrant(createDefaultToken()).isSuccess());
        Assertions.assertEquals(initialKeyRequests + 1, identityProvider.getKeysRequestCount());

        timeOffSet.set(350);

        Assertions.assertTrue(doClientGrant(createDefaultToken()).isSuccess());
        Assertions.assertTrue(doClientGrant(createDefaultToken()).isSuccess());
        Assertions.assertEquals(initialKeyRequests + 2, identityProvider.getKeysRequestCount());
    }

    @Test
    public void testInvalidTrustDomain() {
        realm.updateIdentityProvider(IDP_ALIAS, rep -> {
            rep.getConfig().put(SpiffeIdentityProviderConfig.TRUST_DOMAIN_KEY, "spiffe://different-domain");
        });

        JsonWebToken jwt = createDefaultToken();
        assertFailure(doClientGrant(jwt));
        assertFailure(null, null, jwt.getSubject(), jwt.getId(), "client_not_found", events.poll());
    }

    @Test
    public void testWithIssClaim() {
        JsonWebToken jwt = createDefaultToken();
        jwt.issuer("https://nosuch");
        assertSuccess(INTERNAL_CLIENT_ID, doClientGrant(jwt));
        assertSuccess(INTERNAL_CLIENT_ID, jwt.getId(), "https://nosuch", EXTERNAL_CLIENT_ID, events.poll());
    }

    @Test
    public void testReuse() {
        JsonWebToken jwt = createDefaultToken();
        assertSuccess(INTERNAL_CLIENT_ID, doClientGrant(jwt));
        assertSuccess(INTERNAL_CLIENT_ID, jwt.getId(), null, EXTERNAL_CLIENT_ID, events.poll());
        assertSuccess(INTERNAL_CLIENT_ID, doClientGrant(jwt));
        assertSuccess(INTERNAL_CLIENT_ID, jwt.getId(), null, EXTERNAL_CLIENT_ID, events.poll());
    }

    @Override
    protected OAuthIdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    @Override
    protected JsonWebToken createDefaultToken() {
        JsonWebToken token = new JsonWebToken();
        token.id(null);
        token.audience(oAuthClient.getEndpoints().getIssuer());
        token.exp((long) (Time.currentTime() + 300));
        token.subject(EXTERNAL_CLIENT_ID);
        return token;
    }

    @Override
    protected String getClientAssertionType() {
        return SpiffeConstants.CLIENT_ASSERTION_TYPE;
    }

    public static class SpiffeServerConfig extends ClientAuthIdpServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return super.configure(config).features(Profile.Feature.SPIFFE);
        }
    }

    public static class SpiffeIdpConfig implements OAuthIdentityProviderConfig {

        @Override
        public OAuthIdentityProviderConfigBuilder configure(OAuthIdentityProviderConfigBuilder config) {
            return config.spiffe();
        }
    }

    public static class ExernalClientAuthRealmConfig implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            realm.identityProvider(
                    IdentityProviderBuilder.create()
                            .providerId(SpiffeIdentityProviderFactory.PROVIDER_ID)
                            .alias(IDP_ALIAS)
                            .setAttribute(SpiffeIdentityProviderConfig.TRUST_DOMAIN_KEY, TRUST_DOMAIN)
                            .setAttribute(SpiffeIdentityProviderConfig.BUNDLE_ENDPOINT_KEY, BUNDLE_ENDPOINT)
                            .build());

            realm.addClient(INTERNAL_CLIENT_ID)
                    .serviceAccountsEnabled(true)
                    .authenticatorType(FederatedJWTClientAuthenticator.PROVIDER_ID)
                    .attribute(FederatedJWTClientAuthenticator.JWT_CREDENTIAL_ISSUER_KEY, IDP_ALIAS)
                    .attribute(FederatedJWTClientAuthenticator.JWT_CREDENTIAL_SUBJECT_KEY, EXTERNAL_CLIENT_ID);

            return realm;
        }
    }

}
