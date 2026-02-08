package org.passport.test.examples;

import java.util.Optional;

import org.passport.admin.client.Passport;
import org.passport.common.Profile;
import org.passport.representations.info.FeatureRepresentation;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = CustomConfigTest.CustomServerConfig.class)
public class CustomConfigTest {

    @InjectAdminClient
    Passport adminClient;

    @Test
    public void testPasskeyFeatureEnabled() {
        Optional<FeatureRepresentation> passKeysFeature = adminClient.serverInfo().getInfo().getFeatures().stream().filter(f -> f.getName().equals(Profile.Feature.PASSKEYS.name())).findFirst();
        Assertions.assertTrue(passKeysFeature.isPresent());
        Assertions.assertTrue(passKeysFeature.get().isEnabled());
    }

    public static class CustomServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.features(Profile.Feature.PASSKEYS);
        }

    }

}
