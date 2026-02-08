package org.passport.test.examples;

import org.passport.representations.adapters.action.PushNotBeforeAction;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.TestApp;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.oauth.annotations.InjectTestApp;
import org.passport.testframework.realm.ManagedRealm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class TestAppTest {

    @InjectOAuthClient(kcAdmin = true)
    OAuthClient oauth;

    @InjectTestApp
    TestApp testApp;

    @InjectRealm
    ManagedRealm managedRealm;

    @Test
    public void testPushNotBefore() throws InterruptedException {
        String clientUuid = managedRealm.admin().clients().findByClientId("test-app").stream().findFirst().get().getId();
        managedRealm.admin().clients().get(clientUuid).pushRevocation();

        PushNotBeforeAction adminPushNotBefore = testApp.kcAdmin().getAdminPushNotBefore();
        Assertions.assertNotNull(adminPushNotBefore);
    }

}
