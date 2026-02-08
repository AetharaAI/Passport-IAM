package org.passport.test.examples;

import org.passport.models.PassportSession;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.annotations.TestOnServer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class RunTestOnServerTest {

    @InjectRealm
    ManagedRealm realm;

    @TestOnServer
    public void test(PassportSession session) throws Throwable {
        Assertions.assertNotNull(session);
        Assertions.assertNull(realm);
    }

    @Test
    public void test2() {
        Assertions.assertNotNull(realm);
    }

}
