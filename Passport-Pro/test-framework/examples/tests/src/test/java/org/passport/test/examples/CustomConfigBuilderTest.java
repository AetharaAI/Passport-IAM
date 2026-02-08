package org.passport.test.examples;

import java.util.LinkedList;

import org.passport.representations.idm.GroupRepresentation;
import org.passport.testframework.annotations.InjectClient;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ClientConfig;
import org.passport.testframework.realm.ClientConfigBuilder;
import org.passport.testframework.realm.ManagedClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.realm.UserConfig;
import org.passport.testframework.realm.UserConfigBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class CustomConfigBuilderTest {

    @InjectRealm(config = CustomRealmConfig.class)
    ManagedRealm realm;

    @InjectClient(config = CustomClientConfig.class)
    ManagedClient client;

    @InjectUser(config = CustomUserConfig.class)
    ManagedUser user;

    @Test
    public void testRealm() {
        Assertions.assertEquals(1, realm.admin().groups().query("mygroup").size());
    }

    @Test
    public void testClient() {
        Assertions.assertTrue(client.admin().toRepresentation().isBearerOnly());
    }

    @Test
    public void testUser() {
        Assertions.assertFalse(user.admin().toRepresentation().isEnabled());
    }

    public static class CustomRealmConfig implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            return realm.update(r -> {
                if (r.getGroups() == null) {
                    r.setGroups(new LinkedList<>());
                }
                GroupRepresentation group = new GroupRepresentation();
                group.setName("mygroup");
                group.setPath("/mygroup");
                r.getGroups().add(group);
            });
        }
    }

    public static class CustomClientConfig implements ClientConfig {

        @Override
        public ClientConfigBuilder configure(ClientConfigBuilder client) {
            return client.update(u -> u.setBearerOnly(true));
        }
    }

    public static class CustomUserConfig implements UserConfig {

        @Override
        public UserConfigBuilder configure(UserConfigBuilder user) {
            return user.update(u -> u.setEnabled(false));
        }
    }

}
