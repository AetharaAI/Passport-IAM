package org.passport.testsuite.actions;

import org.passport.locale.LocaleSelectorProvider;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testsuite.AbstractTestRealmPassportTest;
import org.passport.testsuite.Assert;
import org.passport.testsuite.admin.ApiUtil;
import org.passport.testsuite.pages.AppPage;
import org.passport.testsuite.pages.RegisterPage;

import org.jboss.arquillian.graphene.page.Page;
import org.junit.Before;
import org.junit.Test;

public class AppInitiatedRegistrationTest extends AbstractTestRealmPassportTest {

    @Page
    protected AppPage appPage;

    @Page
    protected RegisterPage registerPage;

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
    }

    @Before
    public void before() {
        ApiUtil.removeUserByUsername(testRealm(), "test-user@localhost");
    }

    @Test
    public void ensureLocaleParameterIsPropagatedDuringAppInitiatedRegistration() {

        oauth.registrationForm()
                .param(LocaleSelectorProvider.KC_LOCALE_PARAM, "en")
                .open();

        registerPage.assertCurrent();
        registerPage.register("first", "last", "test-user@localhost", "test-user", "test","test");

        appPage.assertCurrent();

        UserRepresentation user = testRealm().users().searchByEmail("test-user@localhost", true).get(0);
        // ensure that the locale was set on the user
        Assert.assertEquals("en", user.getAttributes().get("locale").get(0));
    }
}
