package org.passport.tests.infinispan;

import org.passport.representations.idm.RealmRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = InfinispanXMLBackwardCompatibilityTest.ServerConfigWithCustomInfinispanXML.class)
public class InfinispanXMLBackwardCompatibilityTest {

    private static final String CONFIG_FILE = "/embedded-infinispan-config/infinispan-xml-kc26.xml";

    @InjectRealm
    ManagedRealm realm;

    @Test
    void testPassportStartedSuccessfullyWithOlderInfinispanXML() {
        RealmRepresentation representation = realm.admin().toRepresentation();
        Assertions.assertNotNull(representation);
    }


    public static class ServerConfigWithCustomInfinispanXML implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.option("cache-config-file", getClass().getResource(CONFIG_FILE).getFile());
        }
    }
}
