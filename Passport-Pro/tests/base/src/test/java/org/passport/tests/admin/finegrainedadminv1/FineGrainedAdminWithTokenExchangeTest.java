package org.passport.tests.admin.finegrainedadminv1;

import org.passport.admin.client.Passport;
import org.passport.common.Profile;
import org.passport.models.Constants;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = FineGrainedAdminWithTokenExchangeTest.FineGrainedWithTokenExchangeServerConf.class)
public class FineGrainedAdminWithTokenExchangeTest extends AbstractFineGrainedAdminTest {

    /**
     * PASSPORT-7406
     */
    @Test
    public void testWithTokenExchange() {
        String exchanged = checkTokenExchange(true);
        try (Passport client = adminClientFactory.create()
                .realm("master").authorization(exchanged).clientId(Constants.ADMIN_CLI_CLIENT_ID).build()) {
            Assertions.assertNotNull(client.realm("master").roles().get("offline_access"));
        }
    }

    public static class FineGrainedWithTokenExchangeServerConf implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            config.features(Profile.Feature.TOKEN_EXCHANGE, Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);

            return config;
        }
    }
}
