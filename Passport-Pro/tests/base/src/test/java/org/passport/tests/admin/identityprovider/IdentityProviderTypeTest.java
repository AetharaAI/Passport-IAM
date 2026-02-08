package org.passport.tests.admin.identityprovider;

import java.util.List;

import org.passport.broker.oidc.OIDCIdentityProviderFactory;
import org.passport.broker.saml.SAMLIdentityProviderFactory;
import org.passport.broker.spiffe.SpiffeIdentityProviderConfig;
import org.passport.broker.spiffe.SpiffeIdentityProviderFactory;
import org.passport.models.IdentityProviderCapability;
import org.passport.models.IdentityProviderModel;
import org.passport.models.IdentityProviderType;
import org.passport.representations.idm.IdentityProviderRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.tests.client.authentication.external.SpiffeClientAuthTest;
import org.passport.testsuite.util.IdentityProviderBuilder;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = SpiffeClientAuthTest.SpiffeServerConfig.class)
public class IdentityProviderTypeTest {

    @InjectRealm(config = MyRealm.class)
    ManagedRealm realm;

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    @Test
    public void testFilterByType() {
        MatcherAssert.assertThat(getIdps(null, null), Matchers.containsInAnyOrder("myoidc", "myspiffe", "mysaml"));
        MatcherAssert.assertThat(getIdps(IdentityProviderType.ANY, null), Matchers.containsInAnyOrder("myoidc", "myspiffe", "mysaml"));
        MatcherAssert.assertThat(getIdps(IdentityProviderType.USER_AUTHENTICATION, null), Matchers.containsInAnyOrder("myoidc", "mysaml"));
        MatcherAssert.assertThat(getIdps(IdentityProviderType.CLIENT_ASSERTION, null), Matchers.containsInAnyOrder("myoidc", "myspiffe"));
    }

    @Test
    public void testFilterByCapability() {
        MatcherAssert.assertThat(getIdps(null, IdentityProviderCapability.USER_LINKING), Matchers.containsInAnyOrder("myoidc", "mysaml"));
    }

    @Test
    public void testDefaultsToUserAuthenticationProviders() {
        runOnServer.run(s -> {
            List<String> idps = s.identityProviders().getAllStream().map(IdentityProviderModel::getAlias).toList();
            MatcherAssert.assertThat(idps, Matchers.containsInAnyOrder("myoidc", "mysaml"));
        });
    }

    private List<String> getIdps(IdentityProviderType type, IdentityProviderCapability capability) {
        return realm.admin().identityProviders()
                .find(type != null ? type.name() : null, capability != null ? capability.name() : null, null, null, 0, 100)
                .stream().map(IdentityProviderRepresentation::getAlias).toList();
    }

    public static class MyRealm implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            return realm
                    .identityProvider(IdentityProviderBuilder.create()
                        .providerId(SpiffeIdentityProviderFactory.PROVIDER_ID)
                        .alias("myspiffe")
                        .setAttribute(SpiffeIdentityProviderConfig.TRUST_DOMAIN_KEY, "spiffe://mytrust")
                        .setAttribute(SpiffeIdentityProviderConfig.BUNDLE_ENDPOINT_KEY, "https://myendpoint")
                        .build())
                    .identityProvider(IdentityProviderBuilder.create()
                        .providerId(OIDCIdentityProviderFactory.PROVIDER_ID)
                        .alias("myoidc")
                        .build())
                    .identityProvider(IdentityProviderBuilder.create()
                        .providerId(SAMLIdentityProviderFactory.PROVIDER_ID)
                        .alias("mysaml")
                        .build());
        }
    }

}
