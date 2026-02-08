package org.passport.testsuite.sessionlimits;

import org.passport.testsuite.broker.BrokerConfiguration;
import org.passport.testsuite.broker.KcOidcBrokerConfiguration;

public class KcOidcUserSessionLimitsBrokerTest extends AbstractUserSessionLimitsBrokerTest {
    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return KcOidcBrokerConfiguration.INSTANCE;
    }
}
