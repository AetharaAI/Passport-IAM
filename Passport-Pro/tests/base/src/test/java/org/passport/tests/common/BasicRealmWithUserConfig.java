package org.passport.tests.common;

import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;

public class BasicRealmWithUserConfig implements RealmConfig {

    public static final String USERNAME =  "basic-user";
    public static final String PASSWORD = "password";

    @Override
    public RealmConfigBuilder configure(RealmConfigBuilder realm) {
        realm.addUser("basic-user").password("password").email("basic@localhost").name("First", "Last");
        return realm;
    }

}
