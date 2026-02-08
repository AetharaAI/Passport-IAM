package org.passport.test.examples;

import java.util.List;

import org.passport.admin.client.Passport;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.PassportIntegrationTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class DefaultConfig1Test {

    @InjectAdminClient
    Passport adminClient;

    @Test
    public void testAdminClient() {
        List<RealmRepresentation> realms = adminClient.realms().findAll();
        Assertions.assertFalse(realms.isEmpty());
    }

}
