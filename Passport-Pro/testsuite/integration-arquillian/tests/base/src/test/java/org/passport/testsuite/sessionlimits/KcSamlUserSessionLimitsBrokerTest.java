package org.passport.testsuite.sessionlimits;

import org.passport.testsuite.broker.BrokerConfiguration;
import org.passport.testsuite.broker.KcSamlBrokerConfiguration;

public class KcSamlUserSessionLimitsBrokerTest extends AbstractUserSessionLimitsBrokerTest {
    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return KcSamlBrokerConfiguration.INSTANCE;
    }
}
