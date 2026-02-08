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
public class ManagedResourcesTest {

    @InjectRealm(lifecycle = LifeCycle.CLASS)
    ManagedRealm realm;

    @InjectClient
    ManagedClient client;

    @InjectUser
    ManagedUser user;

    @Test
    public void testCreatedRealm() {
        Assertions.assertEquals(realm.admin().toRepresentation().getId(), realm.getId());
        Assertions.assertEquals("default", realm.getName());
    }

    @Test
    public void testCreatedClient() {
        Assertions.assertEquals("default", client.getClientId());
        Assertions.assertEquals("default", realm.admin().clients().get(client.getId()).toRepresentation().getClientId());
    }

    @Test
    public void testCreatedUser() {
        Assertions.assertEquals("default", user.getUsername());
        Assertions.assertEquals("default", realm.admin().users().get(user.getId()).toRepresentation().getUsername());
    }

}
