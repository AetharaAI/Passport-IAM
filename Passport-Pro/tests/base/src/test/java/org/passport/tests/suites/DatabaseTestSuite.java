package org.passport.tests.suites;

import org.passport.tests.keys.GeneratedRsaKeyProviderTest;
import org.passport.tests.transactions.TransactionsTest;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "org.passport.tests.admin",
        "org.passport.tests.db"
})
@SelectClasses({
        GeneratedRsaKeyProviderTest.class,
        TransactionsTest.class
})
public class DatabaseTestSuite {
}
