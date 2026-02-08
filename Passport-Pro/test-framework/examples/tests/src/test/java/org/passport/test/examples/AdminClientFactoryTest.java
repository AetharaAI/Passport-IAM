package org.passport.test.examples;

import org.passport.admin.client.Passport;
import org.passport.testframework.admin.AdminClientBuilder;
import org.passport.testframework.admin.AdminClientFactory;
import org.passport.testframework.annotations.InjectAdminClientFactory;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.realm.ManagedRealm;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class AdminClientFactoryTest {

    @InjectRealm(config = RealmSpecificAdminClientTest.RealmWithClientAndUser.class)
    ManagedRealm realm;

    @InjectAdminClientFactory(lifecycle = LifeCycle.METHOD)
    AdminClientFactory adminClientFactory;

    static Passport AUTO_CLOSE_INSTANCE;

    @AfterAll
    public static void checkClosed() {
        Assertions.assertThrows(IllegalStateException.class, () -> AUTO_CLOSE_INSTANCE.realms().findAll());
    }

    @Test
    public void testAdminClientFactory() {
        try (Passport passport = createBuilder().build()) {
            Assertions.assertNotNull(passport.realm(realm.getName()).toRepresentation());
        }
        AUTO_CLOSE_INSTANCE = createBuilder().autoClose().build();
    }

    private AdminClientBuilder createBuilder() {
        return adminClientFactory.create()
                .realm(realm.getName())
                .clientId("myclient")
                .clientSecret("mysecret")
                .username("myadmin")
                .password("mypassword");
    }

}
