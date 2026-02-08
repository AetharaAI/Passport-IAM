package org.passport.tests.client.authentication.external;

import java.io.IOException;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.passport.admin.client.resource.IdentityProvidersResource;
import org.passport.broker.spiffe.SpiffeIdentityProviderConfig;
import org.passport.broker.spiffe.SpiffeIdentityProviderFactory;
import org.passport.http.simple.SimpleHttp;
import org.passport.representations.idm.IdentityProviderRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectSimpleHttp;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.testframework.ui.annotations.InjectPage;
import org.passport.testframework.ui.page.LoginPage;
import org.passport.tests.common.BasicUserConfig;
import org.passport.testsuite.util.IdentityProviderBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.NoSuchElementException;

@PassportIntegrationTest(config = SpiffeClientAuthTest.SpiffeServerConfig.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class SpiffeConfigTest {

    @InjectRealm
    ManagedRealm realm;

    @InjectUser(config = BasicUserConfig.class)
    ManagedUser user;

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    @InjectSimpleHttp
    SimpleHttp simpleHttp;

    @InjectOAuthClient
    OAuthClient oAuthClient;

    @InjectPage
    LoginPage loginPage;

    @Test
    public void testConfig() throws IOException {
        IdentityProvidersResource idps = realm.admin().identityProviders();
        IdentityProviderRepresentation rep = createConfig("testConfig", "spiffe://test", "https://localhost");
        Assertions.assertEquals(201, idps.create(rep).getStatus());

        IdentityProviderRepresentation createdRep = realm.admin().identityProviders().get(rep.getAlias()).toRepresentation();

        Assertions.assertTrue(createdRep.isEnabled());
        MatcherAssert.assertThat(createdRep.getConfig(), Matchers.equalTo(Map.of("bundleEndpoint", "https://localhost", "trustDomain", "spiffe://test")));

        Assertions.assertNull(createdRep.getUpdateProfileFirstLoginMode());
        Assertions.assertNull(createdRep.getFirstBrokerLoginFlowAlias());
        Assertions.assertNull(createdRep.getPostBrokerLoginFlowAlias());
        Assertions.assertNull(createdRep.getOrganizationId());
        Assertions.assertNull(createdRep.isAddReadTokenRoleOnCreate());
        Assertions.assertNull(createdRep.isAuthenticateByDefault());
        Assertions.assertNull(createdRep.isHideOnLogin());
        Assertions.assertNull(createdRep.isLinkOnly());
        Assertions.assertNull(createdRep.isTrustEmail());
        Assertions.assertNull(createdRep.isStoreToken());

        checkNotDisplayOnLoginPages("testConfig");
        checkNoIdpsInAccountConsole();
    }

    @Test
    public void testInvalidConfig() {
        testInvalidConfig("testInvalidConfig1", "with-port:8080", "https://localhost");
        testInvalidConfig("testInvalidConfig2", "without-spiffe-scheme", "https://localhost");
        testInvalidConfig("testInvalidConfig3", "spiffe://valid", "invalid-url");
    }

    private void checkNotDisplayOnLoginPages(String alias) {
        oAuthClient.openLoginForm();
        Assertions.assertThrows(NoSuchElementException.class, () -> loginPage.findSocialButton(alias));
    }

    private void checkNoIdpsInAccountConsole() throws IOException {
        String accessToken = oAuthClient.passwordGrantRequest(user.getUsername(), user.getPassword()).send().getAccessToken();
        String accountUrl = realm.getBaseUrl() + "/account//linked-accounts";
        JsonNode json = simpleHttp.doGet(accountUrl).auth(accessToken).asJson();
        Assertions.assertEquals(0, json.size());
    }

    private void testInvalidConfig(String alias, String trustDomain, String bundleEndpoint) {
        IdentityProviderRepresentation idp = createConfig(alias, trustDomain, bundleEndpoint);
        try (Response r = realm.admin().identityProviders().create(idp)) {
            Assertions.assertEquals(400, r.getStatus());
        }
    }

    private IdentityProviderRepresentation createConfig(String alias, String trustDomain, String bundleEndpoint) {
        return IdentityProviderBuilder.create().providerId(SpiffeIdentityProviderFactory.PROVIDER_ID)
                .alias(alias)
                .setAttribute(SpiffeIdentityProviderConfig.TRUST_DOMAIN_KEY, trustDomain)
                .setAttribute(SpiffeIdentityProviderConfig.BUNDLE_ENDPOINT_KEY, bundleEndpoint).build();
    }

}
