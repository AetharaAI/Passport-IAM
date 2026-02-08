package org.passport.test.examples;

import org.passport.testframework.annotations.InjectClient;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.realm.ManagedClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class GlobalManagedResourcesTest {

    @InjectRealm(lifecycle = LifeCycle.GLOBAL)
    ManagedRealm realm;

    @InjectClient(lifecycle = LifeCycle.GLOBAL)
    ManagedClient client;

    @InjectUser(lifecycle = LifeCycle.GLOBAL)
    ManagedUser user;

    @Test
    public void testCreatedRealm() {
        Assertions.assertEquals("default", realm.getName());
    }

    @Test
    public void testCreatedClient() {
        Assertions.assertEquals("default", client.getClientId());
    }

    @Test
    public void testCreatedUser() {
        Assertions.assertEquals("default", user.getUsername());
    }

}
