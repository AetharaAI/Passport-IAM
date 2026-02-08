package org.passport.test.examples;

import java.util.List;

import org.passport.representations.idm.ClientRepresentation;
import org.passport.testframework.annotations.InjectClient;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.realm.ManagedClient;
import org.passport.testframework.realm.ManagedRealm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class ManagedResources2Test {

    @InjectRealm(lifecycle = LifeCycle.CLASS)
    ManagedRealm realm;

    @InjectClient
    ManagedClient client;

    @Test
    public void testCreatedRealm() {
        Assertions.assertEquals("http://localhost:8080/realms/default", realm.getBaseUrl());
        Assertions.assertEquals("default", realm.getName());
        Assertions.assertEquals("default", realm.admin().toRepresentation().getRealm());
    }

    @Test
    public void testCreatedClient() {
        Assertions.assertEquals("default", client.getClientId());

        List<ClientRepresentation> clients = realm.admin().clients().findByClientId("default");
        Assertions.assertEquals(1, clients.size());
    }

}
