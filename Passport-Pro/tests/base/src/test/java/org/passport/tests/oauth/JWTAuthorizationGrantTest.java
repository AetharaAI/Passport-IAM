package org.passport.tests.oauth;

import org.passport.broker.jwtauthorizationgrant.JWTAuthorizationGrantIdentityProviderFactory;
import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.models.IdentityProviderModel;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testsuite.util.IdentityProviderBuilder;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

@PassportIntegrationTest(config = JWTAuthorizationGrantTest.JWTAuthorizationGrantServerConfig.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class JWTAuthorizationGrantTest extends AbstractJWTAuthorizationGrantTest {

    @InjectRealm(config = JWTAuthorizationGrantTest.JWTAuthorizationGrantRealmConfig.class)
    protected ManagedRealm realm;

    public static class JWTAuthorizationGrantRealmConfig extends AbstractJWTAuthorizationGrantTest.JWTAuthorizationGrantRealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            super.configure(realm);
            realm.identityProvider(IdentityProviderBuilder.create()
                    .providerId(JWTAuthorizationGrantIdentityProviderFactory.PROVIDER_ID)
                    .alias(IDP_ALIAS)
                    .setAttribute(IdentityProviderModel.ISSUER, IDP_ISSUER)
                    .setAttribute(OIDCIdentityProviderConfig.USE_JWKS_URL, Boolean.TRUE.toString())
                    .setAttribute(OIDCIdentityProviderConfig.JWKS_URL, "http://127.0.0.1:8500/idp/jwks")
                    .build());
            return realm;
        }
    }
}
