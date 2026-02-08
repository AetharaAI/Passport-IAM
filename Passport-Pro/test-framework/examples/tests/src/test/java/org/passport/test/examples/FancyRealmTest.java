package org.passport.test.examples;

import org.passport.testframework.annotations.InjectClient;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
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
public class FancyRealmTest {

    @InjectRealm(lifecycle = LifeCycle.CLASS, config = MyRealm.class)
    ManagedRealm realm;

    @InjectClient(config = MyClient.class)
    ManagedClient client;

    @InjectUser(config = MyUser.class)
    ManagedUser user;

    @Test
    public void testCreatedRealm() {
        Assertions.assertEquals("default", realm.getName());

        Assertions.assertNotNull(realm.admin().roles().get("role-1").toRepresentation().getName());
    }

    @Test
    public void testCreatedClient() {
        Assertions.assertEquals("the-client", client.getClientId());
        Assertions.assertEquals("the-client", realm.admin().clients().get(client.getId()).toRepresentation().getClientId());
    }

    @Test
    public void testCreatedUser() {
        Assertions.assertEquals("bobthemob", user.getUsername());
        Assertions.assertEquals("bobthemob", realm.admin().users().get(user.getId()).toRepresentation().getUsername());
    }

    static class MyRealm implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            return realm.roles("role-1", "role-2")
                    .groups("group-1", "group-2");
        }
    }

    static class MyClient implements ClientConfig {

        @Override
        public ClientConfigBuilder configure(ClientConfigBuilder client) {
            return client.clientId("the-client")
                    .redirectUris("http://127.0.0.1", "http://test");
        }
    }

    static class MyUser implements UserConfig {

        @Override
        public UserConfigBuilder configure(UserConfigBuilder user) {
            return user.username("bobthemob")
                    .name("Bob", "Mob")
                    .email("bob@mob")
                    .password("password")
                    .roles("role-1", "role-2") // TODO Adding role mappings when creating user is not supported!
                    .groups("/group-1");
        }
    }

}
