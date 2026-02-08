package org.passport.tests.client.authentication.external;

import java.util.List;

import org.passport.authentication.authenticators.client.FederatedJWTClientAuthenticator;
import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.broker.oidc.OIDCIdentityProviderFactory;
import org.passport.representations.idm.ProtocolMapperRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ClientConfig;
import org.passport.testframework.realm.ClientConfigBuilder;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testsuite.util.IdentityProviderBuilder;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = ClientAuthIdpServerConfig.class)
public class FederatedClientAuthFromPassportTest {

    private static final String IDP_ALIAS = "passport-idp";

    @InjectRealm(config = InternalRealmConfig.class)
    ManagedRealm internalRealm;

    @InjectRealm(ref = "external")
    ManagedRealm externalRealm;

    @InjectOAuthClient
    OAuthClient internalOAuthClient;

    @InjectOAuthClient(ref = "external", realmRef = "external", config = ExternalClientConfig.class)
    OAuthClient externalOAuthClient;

    @Test
    public void testValidToken() {
        String externalClientAssertion = externalOAuthClient.doClientCredentialsGrantAccessTokenRequest().getAccessToken();

        AccessTokenResponse send = internalOAuthClient.clientCredentialsGrantRequest().clientJwt(externalClientAssertion).send();
        Assertions.assertTrue(send.isSuccess());
    }

    public static class InternalRealmConfig implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            realm.identityProvider(
                    IdentityProviderBuilder.create()
                            .providerId(OIDCIdentityProviderFactory.PROVIDER_ID)
                            .alias(IDP_ALIAS)
                            .setAttribute("issuer", "http://localhost:8080/realms/external")
                            .setAttribute(OIDCIdentityProviderConfig.SUPPORTS_CLIENT_ASSERTIONS, "true")
                            .setAttribute(OIDCIdentityProviderConfig.USE_JWKS_URL, "true")
                            .setAttribute(OIDCIdentityProviderConfig.JWKS_URL, "http://localhost:8080/realms/external/protocol/openid-connect/certs")
                            .setAttribute(OIDCIdentityProviderConfig.VALIDATE_SIGNATURE, "true")
                            .build());

            realm.addClient("myclient")
                    .serviceAccountsEnabled(true)
                    .authenticatorType(FederatedJWTClientAuthenticator.PROVIDER_ID)
                    .attribute(FederatedJWTClientAuthenticator.JWT_CREDENTIAL_ISSUER_KEY, IDP_ALIAS)
                    .attribute(FederatedJWTClientAuthenticator.JWT_CREDENTIAL_SUBJECT_KEY, "myclient");

            return realm;
        }
    }

    public static class ExternalClientConfig implements ClientConfig {

        @Override
        public ClientConfigBuilder configure(ClientConfigBuilder client) {
            ProtocolMapperRepresentation subMapper = new ProtocolMapperRepresentation();
            subMapper.setName("fixed-sub");
            subMapper.setProtocol("openid-connect");
            subMapper.setProtocolMapper("oidc-hardcoded-claim-mapper");
            subMapper.getConfig().put("claim.name", "sub");
            subMapper.getConfig().put("claim.value", "myclient");
            subMapper.getConfig().put("access.token.claim", "true");

            ProtocolMapperRepresentation audMapper = new ProtocolMapperRepresentation();
            audMapper.setName("fixed-audience");
            audMapper.setProtocol("openid-connect");
            audMapper.setProtocolMapper("oidc-audience-mapper");
            audMapper.getConfig().put("included.custom.audience", "http://localhost:8080/realms/default");
            audMapper.getConfig().put("access.token.claim", "true");

            return client.clientId("myclient")
                    .defaultClientScopes()
                    .serviceAccountsEnabled(true)
                    .secret("mysecret")
                    .protocolMappers(List.of(subMapper, audMapper));
        }
    }

}
