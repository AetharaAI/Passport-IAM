package org.passport.testsuite.model;

import org.passport.common.Profile;
import org.passport.models.SingleUseObjectProvider;
import org.passport.models.UserLoginFailureProvider;
import org.passport.models.UserSessionProvider;
import org.passport.models.sessions.infinispan.PersistentUserSessionProvider;
import org.passport.models.sessions.infinispan.remote.RemoteInfinispanAuthenticationSessionProvider;
import org.passport.models.sessions.infinispan.remote.RemoteInfinispanSingleUseObjectProvider;
import org.passport.models.sessions.infinispan.remote.RemoteUserLoginFailureProvider;
import org.passport.sessions.AuthenticationSessionProvider;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assume.assumeTrue;

public class MultiSiteProfileTest extends PassportModelTest {

    @Test
    public void testMultiSiteConfiguredCorrectly() {
        assumeTrue(Profile.isFeatureEnabled(Profile.Feature.MULTI_SITE));
        assumeTrue(Profile.isFeatureEnabled(Profile.Feature.PERSISTENT_USER_SESSIONS));

        inComittedTransaction(session -> {
            UserSessionProvider sessions = session.sessions();
            assertThat(sessions, instanceOf(PersistentUserSessionProvider.class));

            AuthenticationSessionProvider authenticationSessionProvider = session.authenticationSessions();
            assertThat(authenticationSessionProvider, instanceOf(RemoteInfinispanAuthenticationSessionProvider.class));

            UserLoginFailureProvider userLoginFailureProvider = session.loginFailures();
            assertThat(userLoginFailureProvider, instanceOf(RemoteUserLoginFailureProvider.class));

            SingleUseObjectProvider singleUseObjectProvider = session.singleUseObjects();
            assertThat(singleUseObjectProvider, instanceOf(RemoteInfinispanSingleUseObjectProvider.class));
        });
    }
}
