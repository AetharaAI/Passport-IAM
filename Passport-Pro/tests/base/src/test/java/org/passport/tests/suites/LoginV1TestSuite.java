package org.passport.tests.suites;

import org.passport.tests.i18n.LoginPageTest;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        LoginPageTest.class
})
public class LoginV1TestSuite {
}
