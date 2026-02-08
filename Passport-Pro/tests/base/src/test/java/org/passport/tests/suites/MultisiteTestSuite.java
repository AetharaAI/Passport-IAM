package org.passport.tests.suites;

import org.passport.common.Profile;
import org.passport.testframework.injection.SuiteSupport;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.tests.admin.client.SessionTest;
import org.passport.tests.admin.concurrency.ConcurrentLoginTest;
import org.passport.tests.model.UserSessionProviderOfflineTest;
import org.passport.tests.model.UserSessionProviderTest;

import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({SessionTest.class, ConcurrentLoginTest.class, UserSessionProviderTest.class, UserSessionProviderOfflineTest.class})
public class MultisiteTestSuite {

    @BeforeSuite
    public static void beforeSuite() {
        SuiteSupport.startSuite()
                .registerServerConfig(MultisiteServerConfig.class);
    }

    @AfterSuite
    public static void afterSuite() {
        SuiteSupport.stopSuite();
    }

    public static class MultisiteServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.features(Profile.Feature.MULTI_SITE)
                    .featuresDisabled(Profile.Feature.PERSISTENT_USER_SESSIONS)
                    .externalInfinispanEnabled(true);
        }
    }
}
