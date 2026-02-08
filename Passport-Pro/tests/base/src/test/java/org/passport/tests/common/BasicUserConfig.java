package org.passport.tests.common;

import org.passport.testframework.realm.UserConfig;
import org.passport.testframework.realm.UserConfigBuilder;

public class BasicUserConfig implements UserConfig {

    @Override
    public UserConfigBuilder configure(UserConfigBuilder user) {
        return user.username("basic-user").password("password").email("basic@localhost").name("First", "Last");
    }

}
