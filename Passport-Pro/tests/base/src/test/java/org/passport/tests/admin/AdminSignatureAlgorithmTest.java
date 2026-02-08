package org.passport.tests.admin;

import org.passport.TokenVerifier;
import org.passport.admin.client.Passport;
import org.passport.crypto.Algorithm;
import org.passport.representations.AccessToken;
import org.passport.representations.AccessTokenResponse;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PassportIntegrationTest
public class AdminSignatureAlgorithmTest {

    @InjectAdminClient
    Passport admin;

    @InjectRealm(attachTo = "master")
    ManagedRealm masterRealm;

    @Test
    public void changeRealmTokenAlgorithm() throws Exception {
        masterRealm.updateWithCleanup(r -> r.defaultSignatureAlgorithm(Algorithm.ES256));

        admin.tokenManager().invalidate(admin.tokenManager().getAccessTokenString());
        AccessTokenResponse accessToken = admin.tokenManager().getAccessToken();
        TokenVerifier<AccessToken> verifier = TokenVerifier.create(accessToken.getToken(), AccessToken.class);
        assertEquals(Algorithm.ES256, verifier.getHeader().getAlgorithm().name());
    }
}
