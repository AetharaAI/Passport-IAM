package org.passport.testsuite.oauth;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.passport.models.utils.PassportModelUtils;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.mappers.HardcodedClaim;
import org.passport.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.passport.representations.idm.ProtocolMapperRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.AbstractPassportTest;
import org.passport.testsuite.AssertEvents;
import org.passport.testsuite.util.ClientManager;
import org.passport.testsuite.util.UserBuilder;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.passport.testsuite.AbstractAdminTest.loadJson;

import static org.junit.Assert.assertEquals;

public class AccessTokenResponseTest extends AbstractPassportTest {

    @Rule
    public AssertEvents events = new AssertEvents(this);

    @Before
    public void clientConfiguration() {
        ClientManager.realm(adminClient.realm("test")).clientId("test-app").directAccessGrant(true);
        /*
         * Configure the default client ID. Seems like OAuthClient is keeping the state of clientID
         * For example: If some test case configure oauth.clientId("sample-public-client"), other tests
         * will fail and the clientID will always be "sample-public-client
         * @see AccessTokenTest#testAuthorizationNegotiateHeaderIgnored()
         */
        oauth.clientId("test-app");
    }

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        RealmRepresentation realm = loadJson(getClass().getResourceAsStream("/testrealm.json"), RealmRepresentation.class);

        UserBuilder user = UserBuilder.create()
                .id(PassportModelUtils.generateId())
                .username("no-permissions")
                .addRoles("user")
                .password("password");
        realm.getUsers().add(user.build());

        ProtocolMapperRepresentation customClaimHardcodedMapper = new ProtocolMapperRepresentation();
        customClaimHardcodedMapper.setName("custom-claim-hardcoded-mapper");
        customClaimHardcodedMapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        customClaimHardcodedMapper.setProtocolMapper(HardcodedClaim.PROVIDER_ID);
        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, "custom_hardcoded_claim");
        config.put(HardcodedClaim.CLAIM_VALUE, "custom_claim");
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN_RESPONSE, "true");
        customClaimHardcodedMapper.setConfig(config);

        realm.getClients().stream().filter(clientRepresentation -> "test-app".equals(clientRepresentation.getClientId()))
                .forEach(clientRepresentation -> {
                    clientRepresentation.setProtocolMappers(Collections.singletonList(customClaimHardcodedMapper));
                    clientRepresentation.setFullScopeAllowed(false);
                });

        testRealms.add(realm);
    }

    @Test
    public void accessTokenRequest() {
        oauth.doLogin("test-user@localhost", "password");

        String code = oauth.parseLoginResponse().getCode();
        AccessTokenResponse response = oauth.doAccessTokenRequest(code);

        assertEquals(200, response.getStatusCode());

        assertEquals("custom_claim", response.getOtherClaims().get("custom_hardcoded_claim"));
    }
}
