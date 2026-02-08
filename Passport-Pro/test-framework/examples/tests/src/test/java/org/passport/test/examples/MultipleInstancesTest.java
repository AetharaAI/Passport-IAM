package org.passport.test.examples;

import org.passport.admin.client.Passport;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class MultipleInstancesTest {

    private final String REALM_A_REF = "realm";
    private final String USER_A_REF = "user";

    @InjectAdminClient
    Passport adminClient;

    @InjectRealm
    ManagedRealm realmDef;

    @InjectRealm(ref = REALM_A_REF, config = CustomRealmConfig.class)
    ManagedRealm realmA;

    @InjectUser
    ManagedUser userDef;

    @InjectUser(ref = USER_A_REF, realmRef = REALM_A_REF)
    ManagedUser userA;

    @Test
    public void testMultipleInstances() {
        Assertions.assertEquals("default", realmDef.getName());
        Assertions.assertEquals(REALM_A_REF, realmA.getName());
    }

    @Test
    public void testRealmRef() {
        var realmDefUsers = adminClient.realm("default").users().search("default");
        var realmAUsers = adminClient.realm(REALM_A_REF).users().search(USER_A_REF);
        Assertions.assertEquals(1, realmDefUsers.size());
        Assertions.assertEquals(1, realmAUsers.size());

        Assertions.assertEquals("default", realmDefUsers.get(0).getUsername());
        Assertions.assertEquals(USER_A_REF, realmAUsers.get(0).getUsername());
    }


    public static class CustomRealmConfig implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            return realm;
        }

    }

}
