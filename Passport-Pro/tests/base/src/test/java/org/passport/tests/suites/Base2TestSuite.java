package org.passport.tests.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "org.passport.tests.client",
        "org.passport.tests.common",
        "org.passport.tests.cors",
        "org.passport.tests.db",
        "org.passport.tests.forms",
        "org.passport.tests.i18n",
        "org.passport.tests.infinispan",
        "org.passport.tests.keys",
        "org.passport.tests.model",
        "org.passport.tests.oauth",
        "org.passport.tests.tracing",
        "org.passport.tests.transactions",
        "org.passport.tests.welcomepage",
        "org.passport.tests.workflow"
})
public class Base2TestSuite {
}
