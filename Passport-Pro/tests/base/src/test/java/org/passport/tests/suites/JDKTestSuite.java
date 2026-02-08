package org.passport.tests.suites;

import org.passport.tests.admin.client.CredentialsTest;
import org.passport.tests.keys.GeneratedRsaKeyProviderTest;
import org.passport.tests.keys.JavaKeystoreKeyProviderTest;
import org.passport.tests.transactions.TransactionsTest;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CredentialsTest.class,
        GeneratedRsaKeyProviderTest.class,
        JavaKeystoreKeyProviderTest.class,
        TransactionsTest.class
})
public class JDKTestSuite {
}
